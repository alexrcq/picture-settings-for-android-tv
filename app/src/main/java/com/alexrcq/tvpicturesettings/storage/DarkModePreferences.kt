package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences
import android.content.res.Resources
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.manager.model.BlueLightFilterMode
import com.alexrcq.tvpicturesettings.manager.model.DarkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class DarkModePreferences(sharedPreferences: SharedPreferences, private val resources: Resources) :
    SharedPreferencesStore(sharedPreferences) {

    var isDarkModeScheduleEnabled: Boolean by preference(
        PreferencesKeys.DARK_MODE_SCHEDULE_ENABLED, resources.getBoolean(R.bool.def_dark_mode_schedule_enabled)
    )
    var darkModeTime: LocalTime
        get() = LocalTime.parse(get(PreferencesKeys.DARK_MODE_TIME, resources.getString(R.string.def_dark_mode_time)))
        set(value) {
            put(PreferencesKeys.DARK_MODE_TIME, value.toString())
        }
    var dayModeTime: LocalTime
        get() = LocalTime.parse(get(PreferencesKeys.DAY_MODE_TIME, resources.getString(R.string.def_day_mode_time)))
        set(value) {
            put(PreferencesKeys.DAY_MODE_TIME, value.toString())
        }
    var nightBacklight: Int by preference(
        PreferencesKeys.NIGHT_BACKLIGHT, resources.getInteger(R.integer.def_night_backlight)
    )
    var dayBacklight: Int by preference(
        PreferencesKeys.DAY_BACKLIGHT, resources.getInteger(R.integer.def_day_backlight)
    )
    var disableDarkModeOnPowerOn: Boolean by preference(
        PreferencesKeys.DISABLE_DARK_MODE_ON_POWER_ON, resources.getBoolean(R.bool.def_disable_dark_mode_on_power_on)
    )
    var isAdditionalDimmingEnabled: Boolean by preference(
        PreferencesKeys.ADDITIONAL_DIMMING_ENABLED, resources.getBoolean(R.bool.def_additional_dimming_enabled)
    )
    var isScreenDimmingEnabled: Boolean by preference(
        PreferencesKeys.SCREEN_DIMMING_ENABLED, resources.getBoolean(R.bool.def_screen_dimming_enabled)
    )
    var screenDimmingPower: Int by preference(
        PreferencesKeys.SCREEN_DIMMING_POWER, resources.getInteger(R.integer.def_screen_dimming_power)
    )
    var isThreeStepsDarkModeEnabled: Boolean by preference(
        PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED, resources.getBoolean(R.bool.def_three_steps_dark_mode_enabled)
    )
    var showModeChanged: Boolean by preference(
        PreferencesKeys.SHOW_MODE_CHANGED, resources.getBoolean(R.bool.def_show_mode_changed)
    )
    var blueLightFilterPower: Int by preference(
        PreferencesKeys.BLUE_LIGHT_FILTER_POWER, resources.getInteger(R.integer.def_blue_light_filter_power)
    )
    var blueLightFilterTemp: Int by preference(
        PreferencesKeys.BLUE_LIGHT_FILTER_TEMPERATURE, resources.getInteger(R.integer.def_blue_light_filter_temperature)
    )
    var blueLightFilterMode: BlueLightFilterMode
        get() = BlueLightFilterMode.of(get(PreferencesKeys.BLUE_LIGHT_FILTER_MODE, BlueLightFilterMode.OFF.value))
        set(value) {
            put(PreferencesKeys.BLUE_LIGHT_FILTER_MODE, value.value)
        }
    var currentMode: DarkMode
        get() = DarkMode.valueOf(get(PreferencesKeys.CURRENT_MODE_NAME, DarkMode.OFF.name))
        set(value) {
            put(PreferencesKeys.CURRENT_MODE_NAME, value.name)
        }
    val currentModeFlow: Flow<DarkMode> = preferenceFlow(
        PreferencesKeys.CURRENT_MODE_NAME, DarkMode.OFF.name
    ).map { modeName -> DarkMode.valueOf(modeName) }
    val screenDimmingEnabledFlow: Flow<Boolean> = preferenceFlow(
        PreferencesKeys.SCREEN_DIMMING_ENABLED, resources.getBoolean(R.bool.def_screen_dimming_enabled)
    )
    val screenDimmingPowerFlow: Flow<Int> = preferenceFlow(
        PreferencesKeys.SCREEN_DIMMING_POWER, resources.getInteger(R.integer.def_screen_dimming_power)
    )
    val blueLightFilterModeFlow: Flow<BlueLightFilterMode> = preferenceFlow(
        PreferencesKeys.BLUE_LIGHT_FILTER_MODE, BlueLightFilterMode.OFF.value
    ).map { BlueLightFilterMode.of(it) }
    val blueLightFilterTempFlow: Flow<Int> = preferenceFlow(
        PreferencesKeys.BLUE_LIGHT_FILTER_TEMPERATURE, resources.getInteger(R.integer.def_blue_light_filter_temperature)
    )
    val blueLightFilterPowerFlow: Flow<Int> = preferenceFlow(
        PreferencesKeys.BLUE_LIGHT_FILTER_POWER, resources.getInteger(R.integer.def_blue_light_filter_power)
    )
    val nightBacklightFlow: Flow<Int> =
        preferenceFlow(PreferencesKeys.NIGHT_BACKLIGHT, resources.getInteger(R.integer.def_night_backlight))
    val dayBacklightFlow: Flow<Int> =
        preferenceFlow(PreferencesKeys.DAY_BACKLIGHT, resources.getInteger(R.integer.def_day_backlight))

    val scheduleChangesFlow: Flow<Unit> = changesFlow { key ->
        key == PreferencesKeys.DARK_MODE_SCHEDULE_ENABLED ||
            key == PreferencesKeys.DARK_MODE_TIME ||
            key == PreferencesKeys.DAY_MODE_TIME
    }
}
