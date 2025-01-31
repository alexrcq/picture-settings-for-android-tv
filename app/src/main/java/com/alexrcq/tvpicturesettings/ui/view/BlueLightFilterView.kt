package com.alexrcq.tvpicturesettings.ui.view

import android.content.Context
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class BlueLightFilterView(context: Context): ScreenFilterView(context) {

    fun setTemperature(temperature: Int) {
        val temp = temperature.coerceIn(1000, 40000) / 100.0

        val red: Int = if (temp <= 66) 255 else {
            val t = temp - 60
            min(255, max(0, (329.698727446 * t.pow(-0.1332047592)).toInt()))
        }

        val green: Int = if (temp <= 66) {
            val t = temp
            min(255, max(0, (99.4708025861 * ln(t) - 161.1195681661).toInt()))
        } else {
            val t = temp - 60
            min(255, max(0, (288.1221695283 * t.pow(-0.0755148492)).toInt()))
        }

        val blue: Int = if (temp >= 66) 255 else {
            if (temp <= 19) 0 else {
                val t = temp - 10
                min(255, max(0, (138.5177312231 * ln(t) - 305.0447927307).toInt()))
            }
        }
        val color: Int = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
        setBackgroundColor(color)
    }
}