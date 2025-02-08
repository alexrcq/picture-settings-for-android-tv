package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences
import android.content.res.Resources
import com.alexrcq.tvpicturesettings.domain.DarkModeManager
import com.alexrcq.tvpicturesettings.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DarkModePreferences(sharedPreferences: SharedPreferences, resources: Resources) :
    SharedPreferencesStore(sharedPreferences) {

    var isAutoDarkModeEnabled: Boolean by preference(
        PreferencesKeys.IS_AUTO_DARK_MODE_ENABLED,
        resources.getBoolean(R.bool.is_auto_dark_mode_enabled)
    )
    var darkModeTime: String by preference(PreferencesKeys.DARK_MODE_TIME, resources.getString(R.string.dark_mode_time))
    var dayModeTime: String by preference(PreferencesKeys.DAY_MODE_TIME, resources.getString(R.string.day_mode_time))
    var nightBacklight: Int by preference(
        PreferencesKeys.NIGHT_BACKLIGHT,
        resources.getInteger(R.integer.night_backlight)
    )
    var dayBacklight: Int by preference(
        PreferencesKeys.DAY_BACKLIGHT,
        resources.getInteger(R.integer.day_backlight)
    )
    var turnOffDarkModeOnPowerOn: Boolean by preference(
        PreferencesKeys.TURN_OFF_DARK_MODE_ON_POWER_ON,
        resources.getBoolean(R.bool.turn_off_dark_mode_on_power_on)
    )
    var isAdditionalDimmingEnabled: Boolean by preference(
        PreferencesKeys.IS_ADDITIONAL_DIMMING_ENABLED,
        resources.getBoolean(R.bool.is_additional_dimming_enabled)
    )
    var isScreenDimmingEnabled: Boolean by preference(
        PreferencesKeys.IS_SCREEN_DIMMING_ENABLED,
        resources.getBoolean(R.bool.is_screen_dimming_enabled)
    )
    var screenDimmingPower: Int by preference(
        PreferencesKeys.SCREEN_DIMMING_POWER,
        resources.getInteger(R.integer.screen_dimming_power)
    )
    var threeStepsDarkModeEnabled: Boolean by preference(
        PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED,
        resources.getBoolean(R.bool.three_steps_dark_mode_enabled)
    )
    var showModeChanged: Boolean by preference(
        PreferencesKeys.SHOW_MODE_CHANGED,
        resources.getBoolean(R.bool.show_mode_changed)
    )
    var isBlueLightFilterEnabled: Boolean by preference(
        PreferencesKeys.IS_BLUE_LIGHT_FILTER_ENABLED,
        resources.getBoolean(R.bool.is_blue_light_filter_enabled)
    )
    var blueLightFilterPower: Int by preference(
        PreferencesKeys.BLUE_LIGHT_FILTER_POWER,
        resources.getInteger(R.integer.blue_light_filter_power)
    )
    var blueLightFilterTemp: Int by preference(
        PreferencesKeys.BLUE_LIGHT_FILTER_TEMPERATURE,
        resources.getInteger(R.integer.blue_filter_temperature)
    )
    var isNightBlueFilterEnabled: Boolean by preference(
        PreferencesKeys.IS_NIGHT_BLUE_FILTER_ENABLED,
        resources.getBoolean(R.bool.is_night_blue_filter_enabled)
    )
    var isAlwaysBlueFilterEnabled: Boolean by preference(
        PreferencesKeys.IS_ALWAYS_BLUE_FILTER_ENABLED,
        resources.getBoolean(R.bool.is_always_blue_filter_enabled)
    )
    var currentMode: DarkModeManager.Mode
        get() = DarkModeManager.Mode.valueOf(get(PreferencesKeys.CURRENT_MODE_NAME, DarkModeManager.Mode.OFF.name))
        set(value) {
            put(PreferencesKeys.CURRENT_MODE_NAME, value.name)
        }
    val modeFlow: Flow<DarkModeManager.Mode> = preferenceFlow(
        PreferencesKeys.CURRENT_MODE_NAME, DarkModeManager.Mode.OFF.name
    ).map { modeName -> DarkModeManager.Mode.valueOf(modeName) }
}