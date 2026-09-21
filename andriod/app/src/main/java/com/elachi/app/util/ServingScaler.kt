package com.elachi.app.util

import kotlin.math.roundToInt

// This class handle the serving scaling such that if the user wants to increase the serving quantity this is the
// class that handles the calculations for it

object ServingScaler {
    fun scale(baseQuantity: Double, baseServings: Int, currentServings: Int): Double {
        if (baseServings <= 0) return baseQuantity
        val scaled = baseQuantity * currentServings / baseServings
        return (scaled * 100).roundToInt() / 100.0
    }
}