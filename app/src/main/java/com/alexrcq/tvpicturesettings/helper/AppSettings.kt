package com.alexrcq.tvpicturesettings.helper

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.alexrcq.tvpicturesettings.service.DarkFilterService
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.DAY_BACKLIGHT
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.helper.GlobalSettings.Keys.PICTURE_BACKLIGHT

class AppSettings(private val context: Context) {

    val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    val dayModeOnScreenOn: Boolean
        get() = preferences.getBoolean(Keys.DAY_MODE_ON_SCREEN_ON_ENABLED, false)

    var isDarkModeEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_DARK_MODE_ENABLED, false)
        set(isDarkModeEnabled) {
            val oldValue = preferences.getBoolean(Keys.IS_DARK_MODE_ENABLED, false)
            if (isDarkModeEnabled == oldValue) return
            preferences.edit {
                putBoolean(Keys.IS_DARK_MODE_ENABLED, isDarkModeEnabled)
            }
            val backlight: Int = if (isDarkModeEnabled) {
                nightBacklight
            } else {
                dayBacklight
            }
            Settings.Global.putInt(context.contentResolver, PICTURE_BACKLIGHT, backlight)
            isDarkFilterEnabled = isDarkModeEnabled && isAdditionalDimmingEnabled
        }

    var isDarkFilterEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_DARK_FILTER_ENABLED, false)
        set(isDarkFilterEnabled) {
            val oldValue = preferences.getBoolean(Keys.IS_DARK_FILTER_ENABLED, false)
            if (isDarkFilterEnabled == oldValue) return
            preferences.edit {
                putBoolean(Keys.IS_DARK_FILTER_ENABLED, isDarkFilterEnabled)
            }
            DarkFilterService.sharedInstance?.darkFilter?.isEnabled = isDarkFilterEnabled
        }

    private val nightBacklight: Int
        get() = preferences.getInt(NIGHT_BACKLIGHT, 0)

    var dayBacklight: Int
        get() = preferences.getInt(DAY_BACKLIGHT, -1)
        set(value) {
            preferences.edit {
                putInt(DAY_BACKLIGHT, value)
            }
        }

    val isAdditionalDimmingEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_ADDITIONAL_DIMMING_ENABLED, false)

    val isAutoDarkModeEnabled: Boolean
        get() = preferences.getBoolean(Keys.IS_AUTO_DARK_MODE_ENABLED, false)

    val isWhiteBalanceFixed: Boolean
        get() = preferences.getBoolean(Keys.FIX_WB_VALUES, false)

    val dayModeTime: String
        get() = preferences.getString(Keys.DAY_MODE_TIME, "08:00")!!

    val darkModeTime: String
        get() = preferences.getString(Keys.DARK_MODE_TIME, "22:30")!!

    val darkFilterPower: Int
        get() = preferences.getInt(Keys.DARK_FILTER_POWER, 50)

    val redGain: Int
        get() = preferences.getInt(Keys.RED_GAIN, 1024)

    val greenGain: Int
        get() = preferences.getInt(Keys.GREEN_GAIN, 1024)

    val blueGain: Int
        get() = preferences.getInt(Keys.BLUE_GAIN, 1024)

    fun toggleDarkMode() {
        isDarkModeEnabled = !isDarkModeEnabled
    }

    fun toggleDarkFilter() {
        isDarkFilterEnabled = !isDarkFilterEnabled
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    object Keys {
        const val IS_AUTO_DARK_MODE_ENABLED = "auto_backlight"
        const val DAY_MODE_TIME = "ab_day_time"
        const val DARK_MODE_TIME = "ab_night_time"
        const val IS_ADDITIONAL_DIMMING_ENABLED = "is_dark_filter_enabled"
        const val DAY_BACKLIGHT = "ab_day_backlight"
        const val NIGHT_BACKLIGHT = "ab_night_backlight"
        const val TAKE_SCREENSHOT = "take_screenshot"
        const val RESET_TO_DEFAULT = "reset_to_default"
        const val DARK_FILTER_POWER = "dark_filter_power"
        const val DAY_MODE_ON_SCREEN_ON_ENABLED = "is_enable_day_mode_after_screen_on_enabled"
        const val IS_DARK_MODE_ENABLED = "is_dark_mode_enabled"
        const val OPEN_PICTURE_SETTINGS = "open_picture_settings"
        const val APP_DESCRIPTION = "app_description"
        const val FIX_WB_VALUES = "fix_wb_values"
        const val RED_GAIN = "picture_red_gain"
        const val GREEN_GAIN = "picture_green_gain"
        const val BLUE_GAIN = "picture_blue_gain"
        const val IS_DARK_FILTER_ENABLED = "dark_filter_enabled"

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