package com.alexrcq.tvpicturesettings.storage

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

val Context.appPreferences: AppPreferences
    get() = AppPreferences(this)

class AppPreferences(context: Context) {

    private val preferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    val isDayModeAfterScreenOnEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED, false)

    val nightBacklight: Int
        get() = preferences.getInt(Keys.NIGHT_BACKLIGHT, 0)

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

    val isDarkFilterEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_DARK_FILTER_ENABLED, false)

    val isAutoDarkModeEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_AUTO_DARK_MODE_ENABLED, false)

    val dayModeTime: String
        get() = preferences.getString(Keys.DAY_MODE_TIME, "08:00")!!

    val darkModeTime: String
        get() = preferences.getString(Keys.DARK_MODE_TIME, "22:30")!!

    val darkFilterPower: Int
        get() = preferences.getInt(Keys.DARK_FILTER_POWER, 50)

    object Keys {
        const val IS_AUTO_DARK_MODE_ENABLED = "auto_backlight"
        const val DAY_MODE_TIME = "ab_day_time"
        const val DARK_MODE_TIME = "ab_night_time"
        const val IS_DARK_FILTER_ENABLED = "is_dark_filter_enabled"
        const val DAY_BACKLIGHT = "ab_day_backlight"
        const val NIGHT_BACKLIGHT = "ab_night_backlight"
        const val TAKE_SCREENSHOT = "take_screenshot"
        const val RESET_TO_DEFAULT = "reset_to_default"
        const val DARK_FILTER_POWER = "dark_filter_power"
        const val IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED =
            "is_enable_day_mode_after_screen_on_enabled"
        const val IS_DARK_MODE_ENABLED = "is_dark_mode_enabled"
        const val OPEN_PICTURE_SETTINGS = "open_picture_settings"
        const val APP_DESCRIPTION = "app_description"

        const val VIDEO_PREFERENCES = "video_preferences"
        const val DARK_MODE_PREFERENCES = "dark_mode_preferences"
        const val COLOR_TUNER = "color_tuner"
        const val SATURATION_TUNE = "saturation_tune"
        const val HUE_TUNE = "hue_tune"
        const val BRIGHTNESS_TUNE = "brightness_tune"
        const val OFFSET_TUNE = "offset_tune"
        const val SCHEDULED_DARK_MODE = "scheduled_dark_mode"
        const val WHITE_BALANCE = "white_balance"
        const val RESET_VALUES = "reset_values"
    }
}