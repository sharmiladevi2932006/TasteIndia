package com.tasteindia.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tasteindia_preferences")

interface FavoritesRepository {
    val favorites: Flow<FavoriteSnapshot>
    suspend fun toggle(id: String, meal: Meal?)
}

data class FavoriteSnapshot(
    val ids: Set<String>,
    val meals: List<Meal>,
    val error: String? = null,
)

class FavoritesStore(private val context: Context) : FavoritesRepository {
    private val key = stringSetPreferencesKey("favorite_meal_ids")
    private val snapshotsKey = stringPreferencesKey("favorite_meal_snapshots")
    private val gson = Gson()

    override val favorites: Flow<FavoriteSnapshot> = context.dataStore.data.map { preferences ->
        val ids = preferences[key] ?: emptySet()
        val snapshots = decodeSnapshots(preferences[snapshotsKey])
        FavoriteSnapshot(ids, ids.mapNotNull(snapshots::get))
    }

    override suspend fun toggle(id: String, meal: Meal?) {
        context.dataStore.edit { preferences ->
            val favorites = preferences[key].orEmpty().toMutableSet()
            val snapshots = decodeSnapshots(preferences[snapshotsKey]).toMutableMap()
            if (!favorites.add(id)) {
                favorites.remove(id)
                snapshots.remove(id)
            } else if (meal != null) {
                snapshots[id] = meal
            }
            preferences[key] = favorites
            preferences[snapshotsKey] = gson.toJson(snapshots)
        }
    }

    private fun decodeSnapshots(value: String?): Map<String, Meal> {
        if (value.isNullOrBlank()) return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Meal>>() {}.type
            gson.fromJson<Map<String, Meal>>(value, type).orEmpty()
        } catch (_: JsonParseException) {
            emptyMap()
        }
    }
}
