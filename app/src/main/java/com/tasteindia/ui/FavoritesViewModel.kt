package com.tasteindia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasteindia.data.FavoriteSnapshot
import com.tasteindia.data.FavoritesRepository
import com.tasteindia.data.Meal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(FavoriteSnapshot(emptySet(), emptyList()))
    val state: StateFlow<FavoriteSnapshot> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.favorites.collect { mutableState.value = it }
        }
    }

    fun toggle(id: String, meal: Meal?) {
        viewModelScope.launch {
            try {
                repository.toggle(id, meal)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = mutableState.value.copy(
                    error = error.message ?: "Favourite could not be saved.",
                )
            }
        }
    }
}

class FavoritesViewModelFactory(
    private val repository: FavoritesRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FavoritesViewModel::class.java))
        return FavoritesViewModel(repository) as T
    }
}
