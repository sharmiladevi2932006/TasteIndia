package com.tasteindia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasteindia.data.FavoritesStore
import com.tasteindia.data.MealRepository
import com.tasteindia.ui.TasteIndiaApp
import com.tasteindia.ui.FavoritesViewModel
import com.tasteindia.ui.FavoritesViewModelFactory
import com.tasteindia.ui.RecipeDetailsViewModel
import com.tasteindia.ui.RecipeDetailsViewModelFactory
import com.tasteindia.ui.TasteIndiaViewModel
import com.tasteindia.ui.TasteIndiaViewModelFactory

class MainActivity : ComponentActivity() {
    private val mealsRepository by lazy { MealRepository(applicationContext) }
    private val favoritesRepository by lazy { FavoritesStore(applicationContext) }
    private val recipesFactory by lazy { TasteIndiaViewModelFactory(mealsRepository) }
    private val detailsFactory by lazy { RecipeDetailsViewModelFactory(mealsRepository) }
    private val favoritesFactory by lazy { FavoritesViewModelFactory(favoritesRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val recipesViewModel: TasteIndiaViewModel = viewModel(factory = recipesFactory)
            val detailsViewModel: RecipeDetailsViewModel = viewModel(factory = detailsFactory)
            val favoritesViewModel: FavoritesViewModel = viewModel(factory = favoritesFactory)
            TasteIndiaApp(recipesViewModel, detailsViewModel, favoritesViewModel)
        }
    }
}
