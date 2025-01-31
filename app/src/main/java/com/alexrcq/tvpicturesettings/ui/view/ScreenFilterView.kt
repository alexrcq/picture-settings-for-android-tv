package com.alexrcq.tvpicturesettings.ui.view

import android.content.Context
import android.view.View

open class ScreenFilterView(context: Context): View(context) {

    companion object {
        const val MIN_POWER = 0
        const val MAX_POWER = 98
        const val SCALE_FACTOR = 100
        const val DEFAULT_ALPHA = 0.5f
    }

    init {
        alpha = DEFAULT_ALPHA
    }

    fun setPower(power: Int) {
        alpha = power.coerceIn(MIN_POWER, MAX_POWER) / SCALE_FACTOR.toFloat()
    }
}