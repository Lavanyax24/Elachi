package com.elachi.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ServingScalerTest {
    @Test fun doubling_servings_doubles_quantity() {
        assertEquals(4.0, ServingScaler.scale(2.0, 4, 8), 0.001)
    }
    @Test fun halving_servings_halves_quantity() {
        assertEquals(1.0, ServingScaler.scale(2.0, 4, 2), 0.001)
    }
    @Test fun same_servings_returns_unchanged_quantity() {
        assertEquals(2.0, ServingScaler.scale(2.0, 4, 4), 0.001)
    }
    @Test fun result_rounds_to_two_decimal_places() {
        assertEquals(0.33, ServingScaler.scale(1.0, 3, 1), 0.001)
    }
    @Test fun zero_base_servings_returns_quantity_unchanged() {
        assertEquals(5.0, ServingScaler.scale(5.0, 0, 4), 0.001)
    }
    @Test fun zero_current_servings_returns_zero() {
        assertEquals(0.0, ServingScaler.scale(5.0, 4, 0), 0.001)
    }
}