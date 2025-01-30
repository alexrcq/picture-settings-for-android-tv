package com.alexrcq.tvpicturesettings.domain

import androidx.annotation.StringRes
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences

class DarkModeManager(private val preferences: DarkModePreferences, private val pictureSettings: PictureSettings) {

    private val twoStepsDarkMode = object : ModeToggleStrategy {
        override fun toggleMode() {
            val nextMode = when (currentMode) {
                Mode.OFF -> if (preferences.isAdditionalDimmingEnabled) Mode.FULL else Mode.ONLY_BACKLIGHT
                Mode.ONLY_BACKLIGHT, Mode.FULL -> Mode.OFF
            }
            currentMode = nextMode
        }
    }

    private val threeStepsDarkMode = object : ModeToggleStrategy {
        override fun toggleMode() {
            val nextMode = when (currentMode) {
                Mode.OFF -> Mode.ONLY_BACKLIGHT
                Mode.ONLY_BACKLIGHT -> Mode.FULL
                Mode.FULL -> Mode.OFF
            }
            currentMode = nextMode
        }
    }

    private val toggleStrategy: ModeToggleStrategy
        get() = if (preferences.threeStepsDarkModeEnabled) threeStepsDarkMode else twoStepsDarkMode

    var currentMode: Mode
        get() = preferences.currentMode
        set(value) {
            if (!pictureSettings.isBacklightAdjustAllowed()) return
            preferences.currentMode = value
            applyMode(value)
        }

    private fun applyMode(mode: Mode) {
        when (mode) {
            Mode.OFF -> {
                pictureSettings.backlight = preferences.dayBacklight
                preferences.isScreenDimmingEnabled = false
            }
            Mode.ONLY_BACKLIGHT -> {
                pictureSettings.backlight = preferences.nightBacklight
                preferences.isScreenDimmingEnabled = false
            }
            Mode.FULL -> {
                pictureSettings.backlight = preferences.nightBacklight
                preferences.isScreenDimmingEnabled = true
            }
        }
        preferences.isBlueLightFilterEnabled =
            preferences.isAlwaysBlueFilterEnabled || (preferences.isNightBlueFilterEnabled && mode != Mode.OFF)
    }

    fun toggleMode() {
        toggleStrategy.toggleMode()
    }

    fun toggleScreenDimming() = with(preferences) {
        isScreenDimmingEnabled = !isScreenDimmingEnabled
    }

    private interface ModeToggleStrategy {
        fun toggleMode()
    }

    enum class Mode(@StringRes val message: Int) {
        OFF(R.string.dark_mode_off),
        ONLY_BACKLIGHT(R.string.dark_mode_only_backlight),
        FULL(R.string.dark_mode_max)
    }
}
