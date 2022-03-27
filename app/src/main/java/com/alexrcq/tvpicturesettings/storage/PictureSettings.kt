package com.alexrcq.tvpicturesettings.storage

import android.content.Context
import com.alexrcq.tvpicturesettings.R

class PictureSettings(private val context: Context): GlobalSettings(context) {
    var backlight: Int
        set(value) {
            putInt(KEY_PICTURE_BACKLIGHT, value)
        }
        get() = getInt(KEY_PICTURE_BACKLIGHT)

    var brightness: Int
        set(value) {
            putInt(KEY_PICTURE_BRIGHTNESS, value)
        }
        get() = getInt(KEY_PICTURE_BRIGHTNESS)

    var contrast: Int
        set(value) {
            putInt(KEY_PICTURE_CONTRAST, value)
        }
        get() = getInt(KEY_PICTURE_CONTRAST)

    var saturation: Int
        set(value) {
            putInt(KEY_PICTURE_SATURATION, value)
        }
        get() = getInt(KEY_PICTURE_SATURATION)

    var hue: Int
        set(value) {
            putInt(KEY_PICTURE_HUE, value)
        }
        get() = getInt(KEY_PICTURE_HUE)

    var sharpness: Int
        set(value) {
            putInt(KEY_PICTURE_SHARPNESS, value)
        }
        get() = getInt(KEY_PICTURE_SHARPNESS)

    var temperature: Int
        set(value) {
            putInt(KEY_PICTURE_TEMPERATURE, value)
        }
        get() = getInt(KEY_PICTURE_TEMPERATURE)

    var pictureMode: Int
        set(value) {
            putInt(KEY_PICTURE_MODE, value)
            val temperature: Int? = when (value) {
                PICTURE_MODE_DEFAULT -> PICTURE_TEMPERATURE_DEFAULT
                PICTURE_MODE_BRIGHT -> PICTURE_TEMPERATURE_WARM
                PICTURE_MODE_SPORT -> PICTURE_TEMPERATURE_DEFAULT
                PICTURE_MODE_MOVIE -> PICTURE_TEMPERATURE_COLD
                else -> null
            }
            if (temperature != null) {
                this.temperature = temperature
            }
        }
        get() = getInt(KEY_PICTURE_MODE)

    var noiseReduction: Int
        set(value) {
            putInt(KEY_PICTURE_NOISE_REDUCTION, value)
        }
        get() = getInt(KEY_PICTURE_NOISE_REDUCTION)

    var isAdaptiveLumaEnabled: Boolean
        set(value) {
            val enabled = if (value) 2 else 0
            putInt(KEY_PICTURE_ADAPTIVE_LUMA_CONTROL, enabled)
        }
        get() {
            return when (getInt(KEY_PICTURE_ADAPTIVE_LUMA_CONTROL)) {
                2 -> true
                else -> false
            }
        }

    var isLocalContrastEnabled: Boolean
        set(value) {
            val enabled = if (value) 2 else 0
            putInt(KEY_PICTURE_LOCAL_CONTRAST, enabled)
        }
        get() {
            return when (getInt(KEY_PICTURE_LOCAL_CONTRAST)) {
                2 -> true
                else -> false
            }
        }

    fun resetToDefault() {
        val resources = context.resources
        backlight = resources.getInteger(R.integer.default_backlight)
        brightness = resources.getInteger(R.integer.default_brightness)
        contrast = resources.getInteger(R.integer.default_contrast)
        hue = resources.getInteger(R.integer.default_hue)
        saturation = resources.getInteger(R.integer.default_saturation)
        sharpness = resources.getInteger(R.integer.default_sharpness)
        noiseReduction = resources.getInteger(R.integer.default_noise_reduction)
        val defaultAdaptiveLuma = resources.getInteger(R.integer.default_adaptive_luma_control)
        isAdaptiveLumaEnabled = when (defaultAdaptiveLuma) {
            2 -> true
            else -> false
        }
        val defaultLocalContrast = resources.getInteger(R.integer.default_local_contrast_control)
        isLocalContrastEnabled = when (defaultLocalContrast) {
            2 -> true
            else -> false
        }
        temperature = resources.getInteger(R.integer.default_temperature)
        pictureMode = resources.getInteger(R.integer.default_picture_mode)
    }
}