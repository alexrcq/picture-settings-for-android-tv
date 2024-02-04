package com.alexrcq.tvpicturesettings.storage

import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DEFAULT_SCREEN_FILTER_POWER = 50

class DarkModePreferences(preferencesStore: SharedPreferencesStore) {
    var isAutoDarkModeEnabled: Boolean by preferencesStore.preference(PreferencesKeys.IS_AUTO_DARK_MODE_ENABLED, false)
    var darkModeTime: String by preferencesStore.preference(PreferencesKeys.DARK_MODE_TIME, "")
    var dayModeTime: String by preferencesStore.preference(PreferencesKeys.DAY_MODE_TIME, "")
    var nightBacklight: Int by preferencesStore.preference(PreferencesKeys.NIGHT_BACKLIGHT, 0)
    var dayBacklight: Int by preferencesStore.preference(PreferencesKeys.DAY_BACKLIGHT, -1)
    var threeStepsDarkModeEnabled: Boolean by preferencesStore.preference(
        PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED, false
    )
    var turnOffDarkModeOnScreenOn: Boolean by preferencesStore.preference(
        PreferencesKeys.TURN_OFF_DARK_MODE_ON_SCREEN_ON, false
    )
    var isAdditionalDimmingEnabled: Boolean by preferencesStore.preference(
        PreferencesKeys.IS_ADDITIONAL_DIMMING_ENABLED, false
    )
    var isScreenFilterEnabled: Boolean by preferencesStore.preference(PreferencesKeys.IS_SCREEN_FILTER_ENABLED, false)
    var screenFilterPower: Int by preferencesStore.preference(
        PreferencesKeys.SCREEN_FILTER_POWER, DEFAULT_SCREEN_FILTER_POWER
    )
    var currentModeName: String by preferencesStore.preference(
        PreferencesKeys.CURRENT_MODE_NAME, DarkModeManager.Mode.OFF.name
    )
    val modeFlow: Flow<DarkModeManager.Mode> =
        preferencesStore.preferenceFlow(PreferencesKeys.CURRENT_MODE_NAME, DarkModeManager.Mode.OFF.name)
            .map { modeName -> DarkModeManager.Mode.valueOf(modeName) }
}