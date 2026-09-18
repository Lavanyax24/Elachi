package com.elachi.app.util

import kotlin.math.roundToInt


object ServingScaler {
    fun scale(baseQuantity: Double, baseServings: Int, currentServings: Int): Double {
        if (baseServings <= 0) return baseQuantity
        val scaled = baseQuantity * currentServings / baseServings
        return (scaled * 100).roundToInt() / 100.0
    }
}