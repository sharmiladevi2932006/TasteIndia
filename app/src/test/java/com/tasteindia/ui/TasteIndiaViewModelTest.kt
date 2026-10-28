package com.tasteindia.ui

import com.tasteindia.data.FavoritesRepository
import com.tasteindia.data.FavoriteSnapshot
import com.tasteindia.data.Ingredient
import com.tasteindia.data.Meal
import com.tasteindia.data.MealsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasteIndiaViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onlyLatestFilterResultIsAppliedAndSearchStateIsRetained() = runTest(dispatcher) {
        val repository = FakeMealsRepository()
        val viewModel = TasteIndiaViewModel(repository)
        runCurrent()

        viewModel.setCategory("Slow")
        runCurrent()
        viewModel.setCategory("Fast")
        advanceUntilIdle()

        assertEquals(setOf("fast"), viewModel.state.value.filteredIds)
        viewModel.setSearch(" dosa ")
        assertEquals(" dosa ", viewModel.state.value.search)
        assertEquals("Fast", viewModel.state.value.category)
    }

    @Test
    fun favoriteIdsAreObservedAndCanBeToggledWithoutNetwork() = runTest(dispatcher) {
        val favorites = FakeFavoritesRepository()
        val viewModel = FavoritesViewModel(favorites)
        runCurrent()

        viewModel.toggle("meal-1", Meal("meal-1", "Dosa", ""))
        advanceUntilIdle()
        assertTrue("meal-1" in viewModel.state.value.ids)

        viewModel.toggle("meal-1", Meal("meal-1", "Dosa", ""))
        advanceUntilIdle()
        assertTrue("meal-1" !in viewModel.state.value.ids)
    }
}

private class FakeMealsRepository : MealsRepository {
    override suspend fun indianMeals() = listOf(
        Meal("meal-1", "Dosa", "", ingredients = listOf(Ingredient("Rice", ""))),
    )
    override suspend fun categories() = listOf("Slow", "Fast")
    override suspend fun ingredients() = listOf("Rice")
    override suspend fun areas() = listOf("Indian")
    override suspend fun filterIds(category: String?, ingredient: String?): Set<String> {
        if (category == "Slow") delay(1_000)
        return if (category == "Fast") setOf("fast") else setOf("slow")
    }
    override suspend fun detail(id: String) = Meal(id, "Test meal", "")
}

private class FakeFavoritesRepository : FavoritesRepository {
    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    override val favorites: Flow<FavoriteSnapshot> =
        favoriteIds.map { FavoriteSnapshot(it, emptyList()) }

    override suspend fun toggle(id: String, meal: Meal?) {
        favoriteIds.value = favoriteIds.value.toMutableSet().also {
            if (!it.add(id)) it.remove(id)
        }
    }
}
