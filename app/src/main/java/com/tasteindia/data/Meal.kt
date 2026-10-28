package com.tasteindia.data

data class Meal(
    val id: String,
    val name: String,
    val thumbnail: String,
    val fields: Map<String, String> = emptyMap(),
    val ingredients: List<Ingredient> = emptyList(),
    val tags: List<String> = emptyList(),
) {
    val category: String get() = fields["Category"].orEmpty()
    val area: String get() = fields["Area"].orEmpty()
}

data class Ingredient(val name: String, val measure: String)

fun normalizeIngredients(dto: MealDto): List<Ingredient> =
    (1..20).mapNotNull { index ->
        val name = dto.ingredients[index - 1].orEmpty().trim()
        val measure = dto.measures[index - 1].orEmpty().trim()
        name.takeIf(String::isNotEmpty)?.let { Ingredient(it, measure) }
    }

fun MealDto.toDomain(): Meal {
    val details = linkedMapOf<String, String>()
    fun add(label: String, value: String?) {
        value?.trim()?.takeIf(String::isNotEmpty)?.let { details[label] = it }
    }
    add("Category", category)
    add("Area", area)
    add("Alternate name", alternateName)
    add("Instructions", instructions)
    add("Source", source)
    add("Video", youtube)
    add("Image source", imageSource)
    add("Creative Commons confirmed", creativeCommonsConfirmed)
    add("Date modified", dateModified)
    return Meal(
        id = id.orEmpty(),
        name = name?.trim().orEmpty().ifEmpty { "Untitled recipe" },
        thumbnail = thumbnail?.trim().orEmpty(),
        fields = details,
        ingredients = normalizeIngredients(this),
        tags = tags.orEmpty().split(',').map(String::trim).filter(String::isNotEmpty),
    )
}
