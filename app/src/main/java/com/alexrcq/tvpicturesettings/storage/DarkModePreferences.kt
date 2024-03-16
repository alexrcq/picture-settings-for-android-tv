package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences
import com.alexrcq.tvpicturesettings.service.DarkModeManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DEFAULT_SCREEN_FILTER_POWER = 50

class DarkModePreferences(sharedPreferences: SharedPreferences) : SharedPreferencesStore(sharedPreferences) {
    var isAutoDarkModeEnabled: Boolean by preference(PreferencesKeys.IS_AUTO_DARK_MODE_ENABLED, false)
    var darkModeTime: String by preference(PreferencesKeys.DARK_MODE_TIME, "")
    var dayModeTime: String by preference(PreferencesKeys.DAY_MODE_TIME, "")
    var nightBacklight: Int by preference(PreferencesKeys.NIGHT_BACKLIGHT, 0)
    var dayBacklight: Int by preference(PreferencesKeys.DAY_BACKLIGHT, -1)
    var turnOffDarkModeOnScreenOn: Boolean by preference(PreferencesKeys.TURN_OFF_DARK_MODE_ON_SCREEN_ON, false)
    var isAdditionalDimmingEnabled: Boolean by preference(PreferencesKeys.IS_ADDITIONAL_DIMMING_ENABLED, false)
    var isScreenFilterEnabled: Boolean by preference(PreferencesKeys.IS_SCREEN_FILTER_ENABLED, false)
    var screenFilterPower: Int by preference(PreferencesKeys.SCREEN_FILTER_POWER, DEFAULT_SCREEN_FILTER_POWER)
    var threeStepsDarkModeEnabled: Boolean
        get() = get(PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED, false)
        set(enabled) {
            put(PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED, enabled)
            modeToggleStrategy = if (enabled) threeStepsDarkMode else twoStepsDarkMode
        }

    private val twoStepsDarkMode = object : ModeToggleStrategy {
        override fun toggleMode() {
            val nextMode = when (currentMode) {
                DarkModeManager.Mode.OFF -> {
                    if (isAdditionalDimmingEnabled) {
                        DarkModeManager.Mode.FULL
                    } else {
                        DarkModeManager.Mode.ONLY_BACKLIGHT
                    }
                }
                DarkModeManager.Mode.ONLY_BACKLIGHT -> DarkModeManager.Mode.OFF
                DarkModeManager.Mode.FULL -> DarkModeManager.Mode.OFF
            }
            currentMode = nextMode
        }
    }

    private val threeStepsDarkMode = object : ModeToggleStrategy {
        override fun toggleMode() {
            val nextMode = when (currentMode) {
                DarkModeManager.Mode.OFF -> DarkModeManager.Mode.ONLY_BACKLIGHT
                DarkModeManager.Mode.ONLY_BACKLIGHT -> DarkModeManager.Mode.FULL
                DarkModeManager.Mode.FULL -> DarkModeManager.Mode.OFF
            }
            currentMode = nextMode
        }
    }

    var currentMode: DarkModeManager.Mode
        get() = DarkModeManager.Mode.valueOf(get(PreferencesKeys.CURRENT_MODE_NAME, DarkModeManager.Mode.OFF.name))
        set(value) {
            put(PreferencesKeys.CURRENT_MODE_NAME, value.name)
        }

    val modeFlow: Flow<DarkModeManager.Mode> = preferenceFlow(
        PreferencesKeys.CURRENT_MODE_NAME, DarkModeManager.Mode.OFF.name
    ).map { modeName -> DarkModeManager.Mode.valueOf(modeName) }

    private var modeToggleStrategy: ModeToggleStrategy =
        if (threeStepsDarkModeEnabled) threeStepsDarkMode else twoStepsDarkMode

    fun toggleMode() {
        modeToggleStrategy.toggleMode()
    }

    fun toggleFilter() {
        isScreenFilterEnabled = !isScreenFilterEnabled
    }

    private interface ModeToggleStrategy {
        fun toggleMode()
    }
}