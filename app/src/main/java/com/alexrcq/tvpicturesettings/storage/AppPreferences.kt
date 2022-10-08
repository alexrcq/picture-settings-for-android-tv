package com.alexrcq.tvpicturesettings.storage

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager


val Context.appPreferences: AppPreferences
    get() = AppPreferences(this)

class AppPreferences(val context: Context) {
    private val preferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    var isDayModeAfterScreenOnEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED, false)
        set(value) {
            preferences.edit {
                putBoolean(Keys.IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED, value)
            }
        }

    var nightBacklight: Int
        get() = preferences.getInt(Keys.NIGHT_BACKLIGHT, 0)
        set(value) {
            preferences.edit {
                putInt(Keys.NIGHT_BACKLIGHT, value)
            }
        }

    var isDarkModeEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_DARK_MODE_ENABLED, false)
        set(value) {
            preferences.edit {
                putBoolean(Keys.IS_DARK_MODE_ENABLED, value)
            }
        }

    var dayBacklight: Int
        get() = preferences.getInt(Keys.DAY_BACKLIGHT, -1)
        set(value) {
            preferences.edit {
                putInt(Keys.DAY_BACKLIGHT, value)
            }
        }

    var isDarkFilterEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_DARK_FILTER_ENABLED, false)
        set(value) {
            preferences.edit {
                putBoolean(Keys.IS_DARK_FILTER_ENABLED, value)
            }
        }

    var dayModeTime: String
        get() = preferences.getString(Keys.DAY_MODE_TIME, "08:00")!!
        set(value) {
            preferences.edit {
                putString(Keys.DAY_MODE_TIME, value)
            }
        }

    var darkModeTime: String
        get() = preferences.getString(Keys.DARK_MODE_TIME, "22:30")!!
        set(value) {
            preferences.edit {
                putString(Keys.DARK_MODE_TIME, value)
            }
        }

    var darkFilterPower: Int
        get() = preferences.getInt(Keys.DARK_FILTER_POWER, 50)
        set(value) {
            preferences.edit {
                putInt(Keys.DARK_FILTER_POWER, value)
            }
        }

    object Keys {
        const val IS_AUTO_DARK_MODE_ENABLED = "auto_backlight"
        const val BACKLIGHT = "backlight"
        const val DAY_MODE_TIME = "ab_day_time"
        const val DARK_MODE_TIME = "ab_night_time"
        const val IS_DARK_FILTER_ENABLED = "is_dark_filter_enabled"
        const val DAY_BACKLIGHT = "ab_day_backlight"
        const val NIGHT_BACKLIGHT = "ab_night_backlight"
        const val PICTURE_MODE = "picture_mode"
        const val TEMPERATURE = "temperature"
        const val TAKE_SCREENSHOT = "take_screenshot"
        const val RESET_TO_DEFAULT = "reset_to_default"
        const val POWER_PICTURE_OFF = "power_picture_off"
        const val DARK_FILTER_POWER = "dark_filter_power"
        const val IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED =
            "is_enable_day_mode_after_screen_on_enabled"
        const val IS_DARK_MODE_ENABLED = "is_dark_mode_enabled"

        const val BRIGHTNESS = "brightness"
        const val CONTRAST = "contrast"
        const val SATURATION = "saturation"
        const val HUE = "hue"
        const val SHARPNESS = "sharpness"

        const val NOISE_REDUCTION = "noise_reduction"
        const val ADAPTIVE_LUMA_CONTROL = "adaptive_luma_control"
        const val LOCAL_CONTRAST_CONTROL = "local_contrast_control"
        const val HDR = "hdr"

        const val COLOR_TUNER_ENABLED = "color_tuner_enabled"
        const val COLOR_TUNER_RED_GAIN = "color_tuner_red_gain"
        const val COLOR_TUNER_GREEN_GAIN = "color_tuner_green_gain"
        const val COLOR_TUNER_BLUE_GAIN = "color_tuner_blue_gain"
        const val COLOR_TUNER_GAIN_RESET = "color_tuner_gain_reset"

        const val TUNER_BRIGHTNESS_BLUE = "tuner_brightness_blue"
        const val TUNER_BRIGHTNESS_CVAN = "tuner_brightness_cvan"
        const val TUNER_BRIGHTNESS_FLESH_TONE = "tuner_brightness_flesh_tone"
        const val TUNER_BRIGHTNESS_GREEN = "tuner_brightness_green"
        const val TUNER_BRIGHTNESS_MAGENTA = "tuner_brightness_magenta"
        const val TUNER_BRIGHTNESS_RED = "tuner_brightness_red"
        const val TUNER_BRIGHTNESS_YELLOW = "tuner_brightness_yellow"
        const val TUNER_BRIGHTNESS_RESET = "tuner_brightness_reset"

        const val TUNER_HUE_BLUE = "tuner_hue_blue"
        const val TUNER_HUE_CVAN = "tuner_hue_cvan"
        const val TUNER_HUE_FLESH_TONE = "tuner_hue_flesh_tone"
        const val TUNER_HUE_GREEN = "tuner_hue_green"
        const val TUNER_HUE_MAGENTA = "tuner_hue_magenta"
        const val TUNER_HUE_RED = "tuner_hue_red"
        const val TUNER_HUE_YELLOW = "tuner_hue_yellow"
        const val TUNER_HUE_RESET = "tuner_hue_reset"

        const val TUNER_OFFSET_BLUE = "tuner_offset_blue"
        const val TUNER_OFFSET_GREEN = "tuner_offset_green"
        const val TUNER_OFFSET_RED = "tuner_offset_red"
        const val TUNER_OFFSET_RESET = "tuner_offset_reset"

        const val TUNER_SATURATION_BLUE = "tuner_saturation_blue"
        const val TUNER_SATURATION_CVAN = "tuner_saturation_cvan"
        const val TUNER_SATURATION_FLESH_TONE = "tuner_saturation_flesh_tone"
        const val TUNER_SATURATION_GREEN = "tuner_saturation_green"
        const val TUNER_SATURATION_MAGENTA = "tuner_saturation_magenta"
        const val TUNER_SATURATION_RED = "tuner_saturation_red"
        const val TUNER_SATURATION_YELLOW = "tuner_saturation_yellow"
        const val TUNER_SATURATION_RESET = "tuner_saturation_reset"

        const val ADVANCED_VIDEO = "advanced_video"
        const val COLOR_TUNER = "color_tuner"
        const val SATURATION_TUNE = "saturation_tune"
        const val HUE_TUNE = "hue_tune"
        const val BRIGHTNESS_TUNE = "brightness_tune"
        const val OFFSET_TUNE = "offset_tune"
        const val SCHEDULED_DARK_MODE = "scheduled_dark_mode"
    }
}