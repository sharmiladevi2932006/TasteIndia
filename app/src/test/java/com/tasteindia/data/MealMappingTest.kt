package com.tasteindia.data

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class MealMappingTest {
    @Test
    fun normalizesTrimmedNonEmptyIngredientsAndTags() {
        val dto = Gson().fromJson(
            """{
                "idMeal":"1",
                "strMeal":"Example",
                "strIngredient1":"  rice ",
                "strMeasure1":" 1 cup ",
                "strIngredient2":" ",
                "strMeasure2":"ignore",
                "strIngredient3":"salt",
                "strMeasure3":"  ",
                "strTags":" spicy, , vegetarian "
            }""",
            MealDto::class.java,
        )

        assertEquals(
            listOf(Ingredient("rice", "1 cup"), Ingredient("salt", "")),
            normalizeIngredients(dto),
        )
        assertEquals(listOf("spicy", "vegetarian"), dto.toDomain().tags)
    }

    @Test
    fun intersectsFilterIdsAcrossSelectedFilters() {
        assertEquals(
            setOf("2"),
            intersectMealIds(listOf(setOf("1", "2"), setOf("2", "3"))),
        )
        assertEquals(emptySet<String>(), intersectMealIds(emptyList()))
    }

    @Test
    fun usesFixturesWhenLiveCollectionIsEmpty() {
        val fixture = Meal(id = "fixture-1", name = "Sample recipe", thumbnail = "")

        assertEquals(listOf(fixture), mealsOrFixtures(emptyList(), listOf(fixture)))
        assertEquals(listOf(fixture), mealsOrFixtures(listOf(fixture), emptyList()))
    }
}
