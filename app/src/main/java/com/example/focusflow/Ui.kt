package com.example.focusflow

import android.content.Context
import android.graphics.drawable.GradientDrawable

object Ui {

    fun dp(context: Context, value: Float): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun rounded(
        context: Context,
        color: Int,
        radiusDp: Float,
        strokeColor: Int = 0,
        strokeDp: Float = 0f
    ): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.RECTANGLE
        d.cornerRadius = radiusDp * context.resources.displayMetrics.density
        d.setColor(color)
        if (strokeDp > 0f) {
            d.setStroke(dp(context, strokeDp), strokeColor)
        }
        return d
    }

    fun oval(color: Int): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(color)
        return d
    }
}