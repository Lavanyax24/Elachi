package com.elachi.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeTextParserTest {

    // Checks that a normal recipe is parsed correctly.
    @Test fun parses_a_simple_recipe_into_title_ingredients_and_steps() {
        val result = RecipeTextParser.parse(listOf(
            "Pancakes", "Ingredients", "2 cups flour", "1 egg",
            "Steps", "1. Mix everything together", "2. Cook on a hot pan",
        ))
        assertEquals("Pancakes", result.title)
        assertEquals(2, result.ingredients.size)
        assertEquals(2, result.steps.size)
    }

    // Checks that mixed fractions are handled correctly.
    @Test fun unicode_mixed_fraction_parses_correctly_not_as_ten_point_five() {
        val result = RecipeTextParser.parse(listOf("Test Recipe", "Ingredients", "1 ½ cups sugar"))
        assertTrue(result.ingredients[0].quantity.toDouble() < 2.0)
    }

    // Checks that normal fractions are parsed correctly.
    @Test fun plain_text_fraction_parses_correctly() {
        val result = RecipeTextParser.parse(listOf("Test Recipe", "Ingredients", "1/2 cup milk"))
        assertEquals(0.5, result.ingredients[0].quantity.toDouble(), 0.001)
    }

    // Makes sure numbered instructions are treated as steps.
    @Test fun numbered_line_is_treated_as_a_step_not_a_quantity_led_ingredient() {
        val result = RecipeTextParser.parse(listOf("Test Recipe", "Steps", "1. Heat the oil in a large pan."))
        assertEquals(1, result.steps.size)
        assertTrue(result.ingredients.isEmpty() || result.ingredients[0].name.isBlank())
    }

    // Checks that empty input does not crash the parser.
    @Test fun empty_input_returns_empty_recipe_without_crashing() {
        val result = RecipeTextParser.parse(emptyList())
        assertEquals("", result.title)
    }
}