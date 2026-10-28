package com.tasteindia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasteindia.data.Meal
import com.tasteindia.data.MealsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeDetailsState(
    val meals: Map<String, Meal> = emptyMap(),
    val loadingIds: Set<String> = emptySet(),
    val errors: Map<String, String> = emptyMap(),
)

class RecipeDetailsViewModel(private val repository: MealsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(RecipeDetailsState())
    val state: StateFlow<RecipeDetailsState> = mutableState.asStateFlow()

    fun load(id: String) {
        if (id in mutableState.value.meals || id in mutableState.value.loadingIds) return
        mutableState.value = mutableState.value.copy(
            loadingIds = mutableState.value.loadingIds + id,
            errors = mutableState.value.errors - id,
        )
        viewModelScope.launch {
            try {
                val meal = repository.detail(id)
                mutableState.value = mutableState.value.copy(
                    meals = mutableState.value.meals + (id to meal),
                    loadingIds = mutableState.value.loadingIds - id,
                    errors = mutableState.value.errors - id,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = mutableState.value.copy(
                    loadingIds = mutableState.value.loadingIds - id,
                    errors = mutableState.value.errors + (
                        id to (error.message ?: "Recipe details could not be loaded.")
                    ),
                )
            }
        }
    }

    fun retry(id: String) {
        mutableState.value = mutableState.value.copy(errors = mutableState.value.errors - id)
        load(id)
    }
}

class RecipeDetailsViewModelFactory(
    private val repository: MealsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecipeDetailsViewModel::class.java))
        return RecipeDetailsViewModel(repository) as T
    }
}
