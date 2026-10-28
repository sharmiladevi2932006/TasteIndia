package com.tasteindia.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.SubcomposeAsyncImage
import com.tasteindia.data.Meal

object TasteIndiaRoutes {
    const val Recipes = "recipes"
    const val Favorites = "favorites"
    const val DetailPattern = "details/{mealId}"
    fun detail(mealId: String) = "details/${Uri.encode(mealId)}"
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TasteIndiaApp(
    recipesViewModel: TasteIndiaViewModel,
    detailsViewModel: RecipeDetailsViewModel,
    favoritesViewModel: FavoritesViewModel,
) {
    val navController = rememberNavController()
    val state by recipesViewModel.state.collectAsStateWithLifecycle()
    val details by detailsViewModel.state.collectAsStateWithLifecycle()
    val favorites by favoritesViewModel.state.collectAsStateWithLifecycle()
    val backStack by navController.currentBackStackEntryAsState()
    val onDetails = { id: String -> navController.navigate(TasteIndiaRoutes.detail(id)) }
    val darkTheme = isSystemInDarkTheme()
    val leaf = if (darkTheme) androidx.compose.ui.graphics.Color(0xFFB3D0B4)
    else androidx.compose.ui.graphics.Color(0xFF476B52)
    val cream = if (darkTheme) androidx.compose.ui.graphics.Color(0xFF171D19)
    else androidx.compose.ui.graphics.Color(0xFFFAF7F0)
    val ink = if (darkTheme) androidx.compose.ui.graphics.Color(0xFFE4EAE4)
    else androidx.compose.ui.graphics.Color(0xFF24332D)

    MaterialTheme(
        colorScheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
            primary = leaf,
            onPrimary = if (darkTheme) androidx.compose.ui.graphics.Color(0xFF171D19)
            else androidx.compose.ui.graphics.Color.White,
            background = cream,
            surface = cream,
            onSurface = ink,
            error = if (darkTheme) androidx.compose.ui.graphics.Color(0xFFFFB4AB)
            else androidx.compose.ui.graphics.Color(0xFFB3261E),
        ),
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("TasteIndia", fontWeight = FontWeight.Bold)
                            Text(
                                if (backStack?.destination?.route?.startsWith("details/") == true) {
                                    "Recipe details"
                                } else {
                                    "A little taste of India"
                                },
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    },
                    navigationIcon = {
                        if (backStack?.destination?.route?.startsWith("details/") == true) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (backStack?.destination?.route == TasteIndiaRoutes.Recipes) {
                            androidx.compose.material3.TextButton(
                                onClick = { navController.navigate(TasteIndiaRoutes.Favorites) },
                            ) { Text("Favourites") }
                        } else if (backStack?.destination?.route == TasteIndiaRoutes.Favorites) {
                            androidx.compose.material3.TextButton(
                                onClick = { navController.popBackStack() },
                            ) { Text("Recipes") }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = TasteIndiaRoutes.Recipes,
                modifier = Modifier.padding(padding),
            ) {
                composable(TasteIndiaRoutes.Recipes) {
                    RecipesScreen(
                        state = state,
                        favoriteIds = favorites.ids,
                        favoritesError = favorites.error,
                        details = details.meals,
                        viewModel = recipesViewModel,
                        favoritesViewModel = favoritesViewModel,
                        onMealClick = onDetails,
                    )
                }
                composable(TasteIndiaRoutes.Favorites) {
                    FavoritesScreen(state, favorites, favoritesViewModel, onDetails)
                }
                composable(
                    route = TasteIndiaRoutes.DetailPattern,
                    arguments = listOf(navArgument("mealId") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("mealId").orEmpty()
                    RecipeDetailsScreen(
                        id = id,
                        listState = state,
                        detailsState = details,
                        favorites = favorites,
                        detailsViewModel = detailsViewModel,
                        favoritesViewModel = favoritesViewModel,
                        onRetry = { detailsViewModel.retry(id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipesScreen(
    state: TasteIndiaState,
    favoriteIds: Set<String>,
    favoritesError: String?,
    details: Map<String, Meal>,
    viewModel: TasteIndiaViewModel,
    favoritesViewModel: FavoritesViewModel,
    onMealClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val meals = remember(state, favoriteIds) { visibleMeals(state, favoriteIds, emptyList(), false) }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            SearchAndFilters(state, viewModel)
        }
        favoritesError?.let { message -> item { StateNotice(message) } }
        if (state.offlineFixture) {
            item { OfflineNotice(onRetry = viewModel::loadRecipes) }
        }
        if (state.loading) {
            item { LoadingContent("Finding Indian recipes…") }
        } else if (state.error != null && state.meals.isEmpty()) {
            item { ErrorContent(state.error, viewModel::loadRecipes) }
        } else {
            item {
                ResultsSummary(
                    count = meals.size,
                    filterError = state.filterError,
                    filterLoading = state.filterLoading,
                    onClear = viewModel::clearFilters,
                    onRetry = viewModel::retryFilters,
                )
            }
            if (state.filterLoading) {
                item { LoadingContent("Applying selected filters…") }
            } else if (meals.isEmpty()) {
                item { EmptyContent("No recipes match these filters.", viewModel::clearFilters) }
            } else {
                items(meals, key = Meal::id) { meal ->
                    MealCard(
                        meal = meal,
                        favorite = meal.id in favoriteIds,
                        category = details[meal.id]?.category ?: meal.category,
                        onFavorite = { favoritesViewModel.toggle(meal.id, meal) },
                        onClick = { onMealClick(meal.id) },
                    )
                }
            }
        }
        item { AttributionFooter() }
    }
}

@Composable
private fun FavoritesScreen(
    state: TasteIndiaState,
    favorites: com.tasteindia.data.FavoriteSnapshot,
    favoritesViewModel: FavoritesViewModel,
    onMealClick: (String) -> Unit,
) {
    val meals = remember(state.meals, favorites) {
        visibleMeals(state, favorites.ids, favorites.meals, favoritesScreen = true)
    }
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("Your favourites", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${meals.size} saved recipes", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }
        favorites.error?.let { message -> item { StateNotice(message) } }
        if (state.loading && meals.isEmpty()) {
            item { LoadingContent("Loading saved recipes…") }
        } else if (meals.isEmpty()) {
            item { EmptyContent("No favourites yet. Tap the heart on a recipe to save it.") }
        } else {
            items(meals, key = Meal::id) { meal ->
                MealCard(
                    meal = meal,
                    favorite = meal.id in favorites.ids,
                    category = meal.category,
                    onFavorite = { favoritesViewModel.toggle(meal.id, meal) },
                    onClick = { onMealClick(meal.id) },
                )
            }
        }
        item { AttributionFooter() }
    }
}

@Composable
private fun AttributionFooter() {
    val context = LocalContext.current
    val uri = Uri.parse("https://www.themealdb.com/api.php")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    val canOpen = intent.resolveActivity(context.packageManager) != null
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Recipe data: TheMealDB", style = MaterialTheme.typography.labelMedium)
        TextButton(
            onClick = { if (canOpen) context.startActivity(intent) },
            enabled = canOpen,
        ) { Text("API and terms") }
    }
}

private fun visibleMeals(
    state: TasteIndiaState,
    favoriteIds: Set<String>,
    favoriteMeals: List<Meal>,
    favoritesScreen: Boolean,
): List<Meal> {
    val source = if (favoritesScreen || state.favoritesOnly) state.meals + favoriteMeals else state.meals
    val visible = source
        .asSequence()
        .distinctBy(Meal::id)
        .filter { if (favoritesScreen || state.favoritesOnly) it.id in favoriteIds else true }
        .filter {
            favoritesScreen ||
                (state.category == null && state.ingredient == null) ||
                (state.filteredIds != null && it.id in state.filteredIds)
        }
        .filter {
            favoritesScreen || state.search.isBlank() ||
                it.name.contains(state.search.trim(), ignoreCase = true)
        }
        .toList()
    return if (state.sortAscending) {
        visible.sortedBy { it.name.lowercase() }
    } else {
        visible.sortedByDescending { it.name.lowercase() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilters(state: TasteIndiaState, viewModel: TasteIndiaViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = state.search,
            onValueChange = viewModel::setSearch,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search Indian recipes") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterDropdown(
                label = "Category",
                selected = state.category,
                options = state.categories,
                onSelected = viewModel::setCategory,
                modifier = Modifier.weight(1f),
            )
            FilterDropdown(
                label = "Ingredient",
                selected = state.ingredient,
                options = state.ingredients,
                onSelected = viewModel::setIngredient,
                modifier = Modifier.weight(1f),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = state.favoritesOnly,
                onClick = { viewModel.setFavoritesOnly(!state.favoritesOnly) },
                label = { Text("Favourites only") },
            )
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.TextButton(
                onClick = { viewModel.setSortAscending(!state.sortAscending) },
            ) {
                Text(if (state.sortAscending) "Name A–Z" else "Name Z–A")
            }
        }
        val activeFilters = buildList {
            if (state.search.isNotBlank()) add("Search: ${state.search}")
            state.category?.let { add("Category: $it") }
            state.ingredient?.let { add("Ingredient: $it") }
            if (state.favoritesOnly) add("Favourites only")
            if (!state.sortAscending) add("Name Z–A")
        }
        if (activeFilters.isNotEmpty()) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                activeFilters.forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    ) {
                        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
                androidx.compose.material3.TextButton(
                    onClick = viewModel::clearFilters,
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) { Text("Clear all") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    selected: String?,
    options: List<String>,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected ?: label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(14.dp),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Any $label") },
                onClick = { onSelected(null); expanded = false },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ResultsSummary(
    count: Int,
    filterError: String?,
    filterLoading: Boolean,
    onClear: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (filterLoading) "Applying filters…" else "$count recipes", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.TextButton(onClick = onClear) { Text("Clear all") }
        }
        if (filterError != null) {
            Text(filterError, color = MaterialTheme.colorScheme.error)
            androidx.compose.material3.TextButton(onClick = onRetry) { Text("Retry filters") }
        }
    }
}

@Composable
private fun MealCard(
    meal: Meal,
    favorite: Boolean,
    category: String,
    onFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            RecipeImage(meal.thumbnail, Modifier.size(92.dp).clip(RoundedCornerShape(14.dp)))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(meal.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (category.isNotBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Text(category, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
            }
            IconButton(
                onClick = onFavorite,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (favorite) "Remove ${meal.name} from favourites" else "Add ${meal.name} to favourites",
                    tint = if (favorite) androidx.compose.ui.graphics.Color(0xFFB34336)
                    else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun RecipeImage(url: String, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = url.takeIf(String::isNotBlank),
        contentDescription = "Recipe photograph",
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = { ImagePlaceholder() },
        error = { ImagePlaceholder() },
    )
}

@Composable
private fun ImagePlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxSize(),
        ) {}
        Text("Recipe image unavailable", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RecipeDetailsScreen(
    id: String,
    listState: TasteIndiaState,
    detailsState: RecipeDetailsState,
    favorites: com.tasteindia.data.FavoriteSnapshot,
    detailsViewModel: RecipeDetailsViewModel,
    favoritesViewModel: FavoritesViewModel,
    onRetry: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(id) { detailsViewModel.load(id) }
    val meal = detailsState.meals[id]
        ?: listState.meals.firstOrNull { it.id == id }
        ?: favorites.meals.firstOrNull { it.id == id }
    if (meal == null) {
        val error = detailsState.errors[id]
        if (error != null) ErrorContent(error, onRetry)
        else LoadingContent("Loading recipe…")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            RecipeImage(meal.thumbnail, Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(22.dp)))
        }
        item {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(meal.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    val validatedArea = meal.area.takeIf {
                        it.isNotBlank() && (listState.areas.isEmpty() || it in listState.areas)
                    }.orEmpty()
                    val metadata = listOf(meal.category, validatedArea).filter(String::isNotBlank)
                    if (metadata.isNotEmpty()) {
                        Text(metadata.joinToString(" • "), color = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = { favoritesViewModel.toggle(id, meal) }) {
                    val favorite = id in favorites.ids
                    Icon(
                        if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (favorite) "Remove ${meal.name} from favourites" else "Add ${meal.name} to favourites",
                        tint = if (favorite) androidx.compose.ui.graphics.Color(0xFFB34336)
                        else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        detailsState.errors[id]?.let { message ->
            item {
                ErrorContent(message, onRetry)
            }
        }
        favorites.error?.let { message ->
            item { StateNotice(message) }
        }
        if (id in detailsState.loadingIds) {
            item { LoadingContent("Loading complete recipe details…") }
        }
        if (meal.ingredients.isNotEmpty()) {
            item { SectionTitle("Ingredients") }
            items(meal.ingredients, key = { "${it.name}-${it.measure}" }) { ingredient ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(ingredient.name)
                    if (ingredient.measure.isNotBlank()) {
                        Text(ingredient.measure, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        meal.tags.takeIf(List<String>::isNotEmpty)?.let { tags ->
            item {
                SectionTitle("Tags")
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
        meal.fields["Instructions"]?.let { instructions ->
            item {
                SectionTitle("Instructions")
                Text(instructions, style = MaterialTheme.typography.bodyLarge)
            }
        }
        val handled = setOf("Category", "Area", "Instructions", "Source", "Video")
        val extras = meal.fields.filterKeys { it !in handled }
        if (extras.isNotEmpty()) {
            item { SectionTitle("More about this recipe") }
            items(extras.entries.toList(), key = { it.key }) { (key, value) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(key, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        listOf("Source" to meal.fields["Source"], "Video" to meal.fields["Video"])
            .forEach { (label, value) ->
                val uri = value?.let(::safeWebUri)
                if (uri != null) {
                    item {
                        val context = LocalContext.current
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        val canOpen = intent.resolveActivity(context.packageManager) != null
                        TextButton(
                            onClick = { if (canOpen) context.startActivity(intent) },
                            enabled = canOpen,
                        ) {
                            Text(if (label == "Source") "Open recipe source" else "Watch recipe video")
                        }
                    }
                }
            }
    }
}

private fun safeWebUri(value: String): Uri? {
    val uri = Uri.parse(value.trim())
    return uri.takeIf {
        (it.scheme.equals("https", ignoreCase = true) ||
            it.scheme.equals("http", ignoreCase = true)) &&
            !it.host.isNullOrBlank() &&
            it.userInfo == null
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(message)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun EmptyContent(message: String, onClear: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message)
        if (onClear != null) Button(onClick = onClear) { Text("Clear filters") }
    }
}

@Composable
private fun OfflineNotice(onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Offline sample recipes are shown.",
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(onClick = onRetry) { Text("Retry live data") }
        }
    }
}

@Composable
private fun StateNotice(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            message,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
