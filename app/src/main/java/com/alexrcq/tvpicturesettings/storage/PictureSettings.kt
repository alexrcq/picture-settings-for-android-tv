package com.alexrcq.tvpicturesettings.storage

import android.annotation.SuppressLint
import android.content.Context

class PictureSettings private constructor(context: Context) :
    GlobalSettingsImpl(context.contentResolver) {

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
            val enabled = if (value) 0 else 2
            putInt(KEY_PICTURE_ADAPTIVE_LUMA_CONTROL, enabled)
        }
        get() {
            return when (getInt(KEY_PICTURE_ADAPTIVE_LUMA_CONTROL)) {
                0 -> true
                else -> false
            }
        }

    var isLocalContrastEnabled: Boolean
        set(value) {
            val enabled = if (value) 0 else 2
            putInt(KEY_PICTURE_LOCAL_CONTRAST, enabled)
        }
        get() {
            return when (getInt(KEY_PICTURE_LOCAL_CONTRAST)) {
                0 -> true
                else -> false
            }
        }

    var isHdrEnabled: Boolean
        set(value) {
            val enabled = if (value) 1 else 0
            putInt(KEY_PICTURE_LIST_HDR, enabled)
        }
        get() {
            return when (getInt(KEY_PICTURE_LIST_HDR)) {
                1 -> true
                else -> false
            }
        }

    var isColorTuneEnabled: Boolean
        set(value) {
            val enabled = if (value) 1 else 0
            putInt(KEY_TV_PICTURE_COLOR_TUNE_ENABLE, enabled)
        }
        get() {
            return when (getInt(KEY_TV_PICTURE_COLOR_TUNE_ENABLE)) {
                1 -> true
                else -> false
            }
        }

    var colorTuneRedGain: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_RED, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_RED)

    var colorTuneGreenGain: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_GREEN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_GREEN)

    var colorTuneBlueGain: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_BLUE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_BLUE)

    fun turnOffScreen() {
        putInt(KEY_POWER_PICTURE_OFF, 0)
    }

    fun resetToDefault() {
        // the same behavior as the system app
        putInt(KEY_PICTURE_RESET_TO_DEFAULT, getInt(KEY_PICTURE_RESET_TO_DEFAULT) + 1)
        putInt(KEY_PICTURE_AUTO_BACKLIGHT, 0)
    }

    companion object {
        const val KEY_PICTURE_BACKLIGHT = "picture_backlight"
        const val KEY_PICTURE_TEMPERATURE = "picture_color_temperature"
        const val KEY_PICTURE_ADAPTIVE_LUMA_CONTROL = "tv_picture_video_adaptive_luma_control"
        const val KEY_PICTURE_LOCAL_CONTRAST = "tv_picture_video_local_contrast_control"
        const val KEY_PICTURE_NOISE_REDUCTION = "tv_picture_advance_video_dnr"
        const val KEY_PICTURE_MODE = "picture_mode"
        const val KEY_PICTURE_BRIGHTNESS = "picture_brightness"
        const val KEY_PICTURE_CONTRAST = "picture_contrast"
        const val KEY_PICTURE_SATURATION = "picture_saturation"
        const val KEY_PICTURE_HUE = "picture_hue"
        const val KEY_PICTURE_SHARPNESS = "picture_sharpness"
        const val KEY_POWER_PICTURE_OFF = "power_picture_off"
        const val KEY_PICTURE_LIST_HDR = "picture_list_hdr"
        const val KEY_PICTURE_RESET_TO_DEFAULT = "picture_reset_to_default"
        const val KEY_PICTURE_AUTO_BACKLIGHT = "picture_auto_backlight"
        const val KEY_TV_PICTURE_COLOR_TUNE_ENABLE = "tv_picture_color_tune_enable"
        const val KEY_TV_PICTURE_COLOR_TUNE_GAIN_RED = "tv_picture_color_tune_gain_red"
        const val KEY_TV_PICTURE_COLOR_TUNE_GAIN_GREEN = "tv_picture_color_tune_gain_green"
        const val KEY_TV_PICTURE_COLOR_TUNE_GAIN_BLUE = "tv_picture_color_tune_gain_blue"
        const val PICTURE_MODE_DEFAULT = 7
        const val PICTURE_MODE_BRIGHT = 3
        const val PICTURE_MODE_SPORT = 2
        const val PICTURE_MODE_MOVIE = 9
        const val PICTURE_MODE_USER = 0
        const val PICTURE_TEMPERATURE_WARM = 1
        const val PICTURE_TEMPERATURE_COLD = 2
        const val PICTURE_TEMPERATURE_DEFAULT = 3

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: PictureSettings? = null

        fun getInstance(context: Context): PictureSettings =
            INSTANCE ?: PictureSettings(context).also {
                INSTANCE = it
            }
    }
}