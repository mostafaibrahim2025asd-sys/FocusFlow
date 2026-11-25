package com.example.focusflow

object SessionManager {

    const val WEIGHT_CARD = 34
    const val WEIGHT_COLOR = 33
    const val WEIGHT_PATTERN = 33

    val points = IntArray(3)

    fun reset() {
        points.fill(0)
    }

    fun total(): Int = points.sum()
}