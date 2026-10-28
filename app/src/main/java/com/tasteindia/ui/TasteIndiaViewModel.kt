package com.tasteindia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasteindia.data.Meal
import com.tasteindia.data.MealsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TasteIndiaState(
    val meals: List<Meal> = emptyList(),
    val categories: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val areas: List<String> = emptyList(),
    val search: String = "",
    val category: String? = null,
    val ingredient: String? = null,
    val favoritesOnly: Boolean = false,
    val sortAscending: Boolean = true,
    val filteredIds: Set<String>? = null,
    val filterLoading: Boolean = false,
    val loading: Boolean = true,
    val offlineFixture: Boolean = false,
    val error: String? = null,
    val filterError: String? = null,
)

class TasteIndiaViewModel(
    private val repository: MealsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(TasteIndiaState())
    val state: StateFlow<TasteIndiaState> = mutableState.asStateFlow()
    private var filterJob: Job? = null
    private var optionsJob: Job? = null

    init {
        loadRecipes()
        loadOptions()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, error = null)
            try {
                val meals = repository.indianMeals()
                mutableState.value = mutableState.value.copy(
                    meals = meals,
                    loading = false,
                    offlineFixture = meals.any { it.id.startsWith("fixture-") },
                    error = null,
                )
                refreshFilter()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    error = error.message ?: "Recipes could not be loaded.",
                )
            }

        }
    }

    fun retryFilters() {
        mutableState.value = mutableState.value.copy(filterError = null)
        loadOptions()
        refreshFilter()
    }

    fun setSearch(value: String) {
        mutableState.value = mutableState.value.copy(search = value)
    }

    fun setFavoritesOnly(value: Boolean) {
        mutableState.value = mutableState.value.copy(favoritesOnly = value)
    }

    fun setSortAscending(value: Boolean) {
        mutableState.value = mutableState.value.copy(sortAscending = value)
    }

    fun setCategory(value: String?) {
        mutableState.value = mutableState.value.copy(category = value, filterError = null)
        refreshFilter()
    }

    fun setIngredient(value: String?) {
        mutableState.value = mutableState.value.copy(ingredient = value, filterError = null)
        refreshFilter()
    }

    fun clearFilters() {
        filterJob?.cancel()
        mutableState.value = mutableState.value.copy(
            search = "",
            category = null,
            ingredient = null,
            favoritesOnly = false,
            sortAscending = true,
            filteredIds = null,
            filterLoading = false,
            filterError = null,
        )
    }

    private fun loadOptions() {
        optionsJob?.cancel()
        optionsJob = viewModelScope.launch {
            try {
                val categories = repository.categories()
                val ingredients = repository.ingredients()
                val areas = repository.areas()
                mutableState.value = mutableState.value.copy(
                    categories = categories,
                    ingredients = ingredients,
                    areas = areas,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = mutableState.value.copy(
                    filterError = error.message ?: "Filter options could not be loaded.",
                )
            }
        }
    }

    private fun refreshFilter() {
        filterJob?.cancel()
        val selected = mutableState.value
        if (selected.category == null && selected.ingredient == null) {
            mutableState.value = mutableState.value.copy(
                filteredIds = null,
                filterLoading = false,
                filterError = null,
            )
            return
        }
        mutableState.value = mutableState.value.copy(filteredIds = null, filterLoading = true)
        filterJob = viewModelScope.launch {
            try {
                val ids = repository.filterIds(selected.category, selected.ingredient)
                if (
                    mutableState.value.category == selected.category &&
                    mutableState.value.ingredient == selected.ingredient
                ) {
                    mutableState.value = mutableState.value.copy(
                        filteredIds = ids,
                        filterLoading = false,
                        filterError = null,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (
                    mutableState.value.category == selected.category &&
                    mutableState.value.ingredient == selected.ingredient
                ) {
                    mutableState.value = mutableState.value.copy(
                        filteredIds = emptySet(),
                        filterLoading = false,
                        filterError = error.message ?: "That filter could not be loaded.",
                    )
                }
            }
        }
    }
}

class TasteIndiaViewModelFactory(
    private val repository: MealsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TasteIndiaViewModel::class.java))
        return TasteIndiaViewModel(repository) as T
    }
}
