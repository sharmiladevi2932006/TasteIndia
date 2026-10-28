# TasteIndia

TasteIndia is a native Android recipe-discovery app built with Kotlin, Jetpack Compose, and Material 3.

## Run the app

Open this folder in Android Studio, use JDK 17, install Android SDK 35, sync Gradle, and run the `app` configuration on an emulator or device. The app's minimum SDK is 26 and target SDK is 35. From a terminal, run `gradlew.bat assembleDebug` on Windows or `./gradlew assembleDebug` on macOS/Linux.

## Architecture

- `MealApi` defines the documented V1 endpoints; Retrofit, Gson, and OkHttp provide typed responses, safe query encoding, explicit HTTP status handling, and connect/read/call timeouts.
- `MealsRepository` converts API DTOs into domain meals, normalizes ingredient/measure pairs, intersects filter IDs, caches detail/filter responses, and bounds parallel detail requests.
- `TasteIndiaViewModel`, `RecipeDetailsViewModel`, and `FavoritesViewModel` own list/filter, detail, and saved-recipe state separately. They expose immutable `StateFlow`; filter jobs are cancelled when superseded, and constructor factories permit deterministic test doubles.
- `FavoritesStore` persists IDs and compact meal snapshots in Preferences DataStore, allowing the saved list to remain visible offline.
- Navigation uses centralized recipes, favourites, and meal-ID detail routes. The recipes destination retains its saveable lazy-list position and ViewModel-owned filter context when details are opened.
- Four focused unit tests cover DTO/ingredient normalization, filter intersection, latest-filter state, and offline favourite state.

## TheMealDB source and use

Recipe content is provided by [TheMealDB](https://www.themealdb.com/). The app uses only its documented V1 public test key `1` and public endpoints, including `filter.php?a=Indian`, category/ingredient/area `list.php` options, category/ingredient `filter.php` calls, and `lookup.php?i={id}`. Name search is local over the loaded Indian collection. Filter IDs are always intersected with that collection, preserving the Indian-area boundary.

The app attributes TheMealDB in its UI. Review [TheMealDB's current API documentation and terms](https://www.themealdb.com/api.php) before distribution: its documentation permits key `1` for development and educational use and says public app-store releases require supporter access. This assignment build is for development/educational review, not public store release.

Details display non-empty category, area, alternate name, instructions, ordered ingredients, tags, source/video actions, image source, Creative Commons status, and modification date. IDs are used for routing and are not presented as recipe content. Empty values are omitted; source and video actions accept only HTTP(S) URLs with a valid host.

Checked-in fixtures keep browse/detail flows and offline favourites reviewable when the public service is unavailable. Fixture IDs start with `fixture-` and are local preview data, not TheMealDB IDs.
