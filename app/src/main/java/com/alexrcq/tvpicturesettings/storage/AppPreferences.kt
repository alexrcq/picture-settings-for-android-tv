package com.alexrcq.tvpicturesettings.storage

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import java.time.LocalTime

class AppPreferences(context: Context) {

    private var preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    var nightBacklight: Int
        get() = preferences.getInt(Keys.NIGHT_BACKLIGHT, 0)
        set(value) {
            preferences.edit {
                putInt(Keys.NIGHT_BACKLIGHT, value)
            }
        }

    var isBacklightBarSwitchEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_NIGHT_BACKLIGHT_ACTIVATED, false)
        set(value) {
            preferences.edit {
                putBoolean(Keys.IS_NIGHT_BACKLIGHT_ACTIVATED, value)
            }
        }

    var dayBacklight: Int
        get() = preferences.getInt(Keys.DAY_BACKLIGHT, 80)
        set(value) {
            preferences.edit {
                putInt(Keys.DAY_BACKLIGHT, value)
            }
        }

    var isAutoBacklightEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_AUTO_BACKLIGHT_ENABLED, false)
        set(value) {
            preferences.edit {
                putBoolean(Keys.IS_AUTO_BACKLIGHT_ENABLED, value)
            }
        }

    var isDarkFilterEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_DARK_FILTER_ENABLED, false)
        set(value) {
            preferences.edit {
                putBoolean(Keys.IS_DARK_FILTER_ENABLED, value)
            }
        }

    var dayLaunchTime: String
        get() = preferences.getString(Keys.DAY_TIME, "08:00")!!
        set(value) {
            preferences.edit {
                putString(Keys.DAY_TIME, value)
            }
        }

    var nightLaunchTime: String
        get() = preferences.getString(Keys.NIGHT_TIME, "22:30")!!
        set(value) {
            preferences.edit {
                putString(Keys.NIGHT_TIME, value)
            }
        }

    val isNightNow: Boolean
        get() {
            val currentTime = LocalTime.now()
            val sunsetTime = LocalTime.parse(nightLaunchTime)
            val sunriseTime = LocalTime.parse(dayLaunchTime)
            return currentTime >= sunsetTime || currentTime <= sunriseTime
        }

    object Keys {
        const val IS_AUTO_BACKLIGHT_ENABLED = "auto_backlight"
        const val IS_NIGHT_BACKLIGHT_ACTIVATED = "is_night_backlight_activated"
        const val BACKLIGHT = "backlight"
        const val DAY_TIME = "ab_day_time"
        const val NIGHT_TIME = "ab_night_time"
        const val IS_DARK_FILTER_ENABLED = "is_dark_filter_enabled"
        const val DAY_BACKLIGHT = "ab_day_backlight"
        const val NIGHT_BACKLIGHT = "ab_night_backlight"
        const val PICTURE_MODE = "picture_mode"
        const val TEMPERATURE = "temperature"
        const val RESET_TO_DEFAULT = "reset_to_default"
        const val POWER_PICTURE_OFF = "power_picture_off"
        const val HDR = "hdr"

        const val BRIGHTNESS = "brightness"
        const val CONTRAST = "contrast"
        const val SATURATION = "saturation"
        const val HUE = "hue"
        const val SHARPNESS = "sharpness"

        const val NOISE_REDUCTION = "noise_reduction"
        const val ADAPTIVE_LUMA_CONTROL = "adaptive_luma_control"
        const val LOCAL_CONTRAST_CONTROL = "local_contrast_control"
    }
}