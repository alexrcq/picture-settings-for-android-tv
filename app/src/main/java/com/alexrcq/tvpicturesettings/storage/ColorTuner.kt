package com.alexrcq.tvpicturesettings.storage

import android.content.Context

class ColorTuner(context: Context) : GlobalSettingsImpl(context.contentResolver) {

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

    var redGain: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_RED, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_RED)

    var greenGain: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_GREEN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_GREEN)

    var blueGain: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_BLUE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_GAIN_BLUE)

    var blueSaturation: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_BLUE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_BLUE)

    var cyanSaturation: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_CVAN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_CVAN)

    var fleshToneSaturation: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_FLESH_TONE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_FLESH_TONE)


    var greenSaturation: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_GREEN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_GREEN)


    var magentaSaturation: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_MEGENTA, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_MEGENTA)

    var yellowSaturation: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_YELLOW, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_YELLOW)

    var redSaturation: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_RED, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_SATURATION_RED)

    var blueHue: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_BLUE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_BLUE)

    var cyanHue: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_CVAN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_CVAN)

    var fleshToneHue: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_FLESH_TONE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_FLESH_TONE)


    var greenHue: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_GREEN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_GREEN)


    var magentaHue: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_MEGENTA, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_MEGENTA)

    var yellowHue: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_YELLOW, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_YELLOW)

    var redHue: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_RED, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_HUE_RED)

    var blueBrightness: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_BLUE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_BLUE)

    var cyanBrightness: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_CVAN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_CVAN)

    var fleshToneBrightness: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_FLESH_TONE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_FLESH_TONE)


    var greenBrightness: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_GREEN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_GREEN)


    var magentaBrightness: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_MEGENTA, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_MEGENTA)

    var yellowBrightness: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_YELLOW, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_YELLOW)

    var redBrightness: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_RED, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_RED)

    var redOffset: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_OFFSET_RED, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_OFFSET_RED)

    var greenOffset: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_OFFSET_GREEN, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_OFFSET_GREEN)

    var blueOffset: Int
        set(value) {
            putInt(KEY_TV_PICTURE_COLOR_TUNE_OFFSET_BLUE, value)
        }
        get() = getInt(KEY_TV_PICTURE_COLOR_TUNE_OFFSET_BLUE)

    fun resetGain() {
        redGain = defaultValue
        greenGain = defaultValue
        blueGain = defaultValue
    }

    fun resetSaturation() {
        blueSaturation = defaultValue
        cyanSaturation = defaultValue
        yellowSaturation = defaultValue
        magentaSaturation = defaultValue
        redSaturation = defaultValue
        fleshToneSaturation = defaultValue
        greenSaturation = defaultValue
    }

    fun resetHue() {
        redHue = defaultValue
        greenHue = defaultValue
        cyanHue = defaultValue
        magentaHue = defaultValue
        blueHue = defaultValue
        yellowHue = defaultValue
        fleshToneHue = defaultValue
    }

    fun resetBrightness() {
        greenBrightness = defaultValue
        blueBrightness = defaultValue
        redBrightness = defaultValue
        magentaBrightness = defaultValue
        yellowBrightness = defaultValue
        fleshToneBrightness = defaultValue
        cyanBrightness = defaultValue
    }

    fun resetOffset() {
        greenOffset = defaultValue
        redOffset = defaultValue
        blueOffset = defaultValue
    }

    companion object {
        const val defaultValue = 50
        const val KEY_TV_PICTURE_COLOR_TUNE_ENABLE = "tv_picture_color_tune_enable"

        const val KEY_TV_PICTURE_COLOR_TUNE_GAIN_RED = "tv_picture_color_tune_gain_red"
        const val KEY_TV_PICTURE_COLOR_TUNE_GAIN_GREEN = "tv_picture_color_tune_gain_green"
        const val KEY_TV_PICTURE_COLOR_TUNE_GAIN_BLUE = "tv_picture_color_tune_gain_blue"

        const val KEY_TV_PICTURE_COLOR_TUNE_SATURATION_BLUE = "tv_picture_color_tune_saturation_blue"
        const val KEY_TV_PICTURE_COLOR_TUNE_SATURATION_CVAN = "tv_picture_color_tune_saturation_cvan"
        const val KEY_TV_PICTURE_COLOR_TUNE_SATURATION_FLESH_TONE = "tv_picture_color_tune_saturation_flesh_tone"
        const val KEY_TV_PICTURE_COLOR_TUNE_SATURATION_GREEN = "tv_picture_color_tune_saturation_green"
        const val KEY_TV_PICTURE_COLOR_TUNE_SATURATION_MEGENTA = "tv_picture_color_tune_saturation_megenta"
        const val KEY_TV_PICTURE_COLOR_TUNE_SATURATION_RED = "tv_picture_color_tune_saturation_red"
        const val KEY_TV_PICTURE_COLOR_TUNE_SATURATION_YELLOW = "tv_picture_color_tune_saturation_yellow"

        const val KEY_TV_PICTURE_COLOR_TUNE_HUE_BLUE = "tv_picture_color_tune_hue_blue"
        const val KEY_TV_PICTURE_COLOR_TUNE_HUE_CVAN = "tv_picture_color_tune_hue_cvan"
        const val KEY_TV_PICTURE_COLOR_TUNE_HUE_FLESH_TONE = "tv_picture_color_tune_hue_flesh_tone"
        const val KEY_TV_PICTURE_COLOR_TUNE_HUE_GREEN = "tv_picture_color_tune_hue_green"
        const val KEY_TV_PICTURE_COLOR_TUNE_HUE_MEGENTA = "tv_picture_color_tune_hue_megenta"
        const val KEY_TV_PICTURE_COLOR_TUNE_HUE_RED = "tv_picture_color_tune_hue_red"
        const val KEY_TV_PICTURE_COLOR_TUNE_HUE_YELLOW = "tv_picture_color_tune_hue_yellow"

        const val KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_BLUE = "tv_picture_color_tune_brightness_blue"
        const val KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_CVAN = "tv_picture_color_tune_brightness_cvan"
        const val KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_FLESH_TONE = "tv_picture_color_tune_brightness_flesh_tone"
        const val KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_GREEN = "tv_picture_color_tune_brightness_green"
        const val KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_MEGENTA = "tv_picture_color_tune_brightness_megenta"
        const val KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_RED = "tv_picture_color_tune_brightness_red"
        const val KEY_TV_PICTURE_COLOR_TUNE_BRIGHTNESS_YELLOW = "tv_picture_color_tune_brightness_yellow"

        const val KEY_TV_PICTURE_COLOR_TUNE_OFFSET_BLUE = "tv_picture_color_tune_offset_blue"
        const val KEY_TV_PICTURE_COLOR_TUNE_OFFSET_GREEN = "tv_picture_color_tune_offset_green"
        const val KEY_TV_PICTURE_COLOR_TUNE_OFFSET_RED = "tv_picture_color_tune_offset_red"
    }
}