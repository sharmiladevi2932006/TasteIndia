package com.tasteindia.data

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

interface MealsRepository {
    suspend fun indianMeals(): List<Meal>
    suspend fun categories(): List<String>
    suspend fun ingredients(): List<String>
    suspend fun areas(): List<String>
    suspend fun filterIds(category: String?, ingredient: String?): Set<String>
    suspend fun detail(id: String): Meal
}

class MealRepository(
    private val context: Context,
    private val api: MealApi = createMealApi(),
) : MealsRepository {
    private val gson = Gson()
    private val detailsCache = ConcurrentHashMap<String, Meal>()
    private val filterCache = ConcurrentHashMap<String, Set<String>>()
    private val detailRequestLimit = Semaphore(4)

    override suspend fun indianMeals(): List<Meal> = withContext(Dispatchers.IO) {
        try {
            checked(api.indianMeals()).meals.orEmpty().map(MealDto::toDomain)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            val fixtures = readFixture("indian_meals.json")
            if (fixtures.isEmpty()) throw error
            fixtures
        }
    }

    override suspend fun categories(): List<String> =
        checked(api.categories()).meals.orEmpty().mapNotNull { it.category.clean() }

    override suspend fun ingredients(): List<String> =
        checked(api.ingredients()).meals.orEmpty().mapNotNull { it.optionIngredient.clean() }

    override suspend fun areas(): List<String> =
        checked(api.areas()).meals.orEmpty().mapNotNull { it.area.clean() }

    override suspend fun filterIds(category: String?, ingredient: String?): Set<String> =
        withContext(Dispatchers.IO) {
            val filters = buildList {
                category.clean()?.let { add("c:$it") }
                ingredient.clean()?.let { add("i:$it") }
            }
            if (filters.isEmpty()) return@withContext emptySet()
            val resultSets = filters.map { filter ->
                filterCache[filter] ?: run {
                    val ids = when {
                        filter.startsWith("c:") ->
                            checked(api.filterByCategory(filter.removePrefix("c:")))
                                .meals.orEmpty().mapNotNull { it.id.clean() }.toSet()
                        else ->
                            checked(api.filterByIngredient(filter.removePrefix("i:")))
                                .meals.orEmpty().mapNotNull { it.id.clean() }.toSet()
                    }
                    filterCache[filter] = ids
                    ids
                }
            }
            intersectMealIds(resultSets)
        }

    override suspend fun detail(id: String): Meal = withContext(Dispatchers.IO) {
        detailsCache[id]?.let { return@withContext it }
        if (id.startsWith("fixture-")) {
            readFixture("meal_details.json").firstOrNull { it.id == id }?.let {
                detailsCache[id] = it
                return@withContext it
            }
        }
        detailRequestLimit.withPermit {
            detailsCache[id] ?: run {
                val resolved = try {
                    checked(api.detail(id)).meals.orEmpty().firstOrNull()?.toDomain()
                        ?: throw IOException("The recipe was not found.")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    readFixture("meal_details.json").firstOrNull { it.id == id } ?: throw error
                }
                detailsCache[id] = resolved
                resolved
            }
        }
    }

    private fun <T> checked(response: Response<T>): T {
        if (response.isSuccessful) return response.body()
            ?: throw IOException("TheMealDB returned an empty response.")
        throw IOException(
            when (response.code()) {
                429 -> "TheMealDB is receiving too many requests. Please try again shortly."
                in 500..599 -> "TheMealDB is temporarily unavailable. Please try again."
                else -> "TheMealDB request failed (HTTP ${response.code()})."
            },
        )
    }

    private fun readFixture(fileName: String): List<Meal> = try {
        context.assets.open("fixtures/$fileName").bufferedReader().use { reader ->
            gson.fromJson(reader, MealResponse::class.java).meals.orEmpty().map(MealDto::toDomain)
        }
    } catch (_: IOException) {
        emptyList()
    }

    private fun String?.clean(): String? = this?.trim()?.takeIf(String::isNotEmpty)

    companion object {
        private fun createMealApi(): MealApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .callTimeout(20, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/api/json/v1/1/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MealApi::class.java)
        }
    }
}

fun intersectMealIds(idSets: List<Set<String>>): Set<String> =
    idSets.reduceOrNull { result, next -> result intersect next }.orEmpty()
