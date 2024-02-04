package com.alexrcq.tvpicturesettings.helper

import android.content.Context
import com.alexrcq.tvpicturesettings.util.ServiceUtils
import com.alexrcq.tvpicturesettings.service.ScreenFilterService
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.TvSettings

class DarkModeManagerImpl(
    private val context: Context,
    private val pictureSettings: TvSettings.Picture,
    private val preferences: DarkModePreferences
) : DarkModeManager {

    private var modeToggleStrategy: ModeToggleStrategy

    init {
        if (preferences.dayBacklight !in 0..TvSettings.Picture.MAX_BACKLIGHT) {
            setDayBacklight(pictureSettings.backlight)
        }
        modeToggleStrategy = if (preferences.threeStepsDarkModeEnabled) {
            ThreeStepsDarkMode()
        } else {
            TwoStepsDarkMode()
        }
    }

    override val currentMode: DarkModeManager.Mode get() = DarkModeManager.Mode.valueOf(preferences.currentModeName)

    override fun setMode(mode: DarkModeManager.Mode) {
        preferences.currentModeName = mode.name
        when (mode) {
            DarkModeManager.Mode.OFF -> {
                pictureSettings.backlight = preferences.dayBacklight
                setScreenFilterEnabled(false)
            }
            DarkModeManager.Mode.ONLY_BACKLIGHT -> {
                pictureSettings.backlight = preferences.nightBacklight
                setScreenFilterEnabled(false)
            }
            DarkModeManager.Mode.FULL -> {
                pictureSettings.backlight = preferences.nightBacklight
                setScreenFilterEnabled(true)
            }
        }
    }

    override fun toggleMode() {
        modeToggleStrategy.toggleMode()
    }

    override fun setDayBacklight(value: Int) {
        preferences.dayBacklight = value
    }

    override fun setNightBacklight(value: Int) {
        preferences.nightBacklight = value
        if (currentMode != DarkModeManager.Mode.OFF) {
            pictureSettings.backlight = value
        }
    }

    override fun setStepByStepDarkModeEnabled(enabled: Boolean) {
        preferences.threeStepsDarkModeEnabled = enabled
        modeToggleStrategy = if (enabled) ThreeStepsDarkMode() else TwoStepsDarkMode()
    }

    override fun setScreenFilterEnabled(enabled: Boolean) {
        preferences.isScreenFilterEnabled = enabled
        ensureScreenFilterServiceRunning()
        ScreenFilterService.sharedInstance?.screenFilter?.isEnabled = enabled
    }

    override fun setScreenFilterPower(value: Int) {
        preferences.screenFilterPower = value
        ensureScreenFilterServiceRunning()
        ScreenFilterService.sharedInstance?.screenFilter?.setPower(value)
    }

    override fun toggleScreenFilter() {
        setScreenFilterEnabled(!preferences.isScreenFilterEnabled)
    }

    private fun ensureScreenFilterServiceRunning() {
        if (!ScreenFilterService.isServiceConnected()) {
            ServiceUtils.ensureAccessibilityServiceEnabled(context, ScreenFilterService::class.java)
        }
    }

    private inner class TwoStepsDarkMode : ModeToggleStrategy {
        override fun toggleMode() {
            val nextMode = when (currentMode) {
                DarkModeManager.Mode.OFF -> {
                    if (preferences.isAdditionalDimmingEnabled) {
                        DarkModeManager.Mode.FULL
                    } else {
                        DarkModeManager.Mode.ONLY_BACKLIGHT
                    }
                }
                DarkModeManager.Mode.FULL -> DarkModeManager.Mode.OFF
                DarkModeManager.Mode.ONLY_BACKLIGHT -> DarkModeManager.Mode.OFF
            }
            setMode(nextMode)
        }
    }

    private inner class ThreeStepsDarkMode : ModeToggleStrategy {
        override fun toggleMode() {
            val nextMode = when (currentMode) {
                DarkModeManager.Mode.OFF -> DarkModeManager.Mode.ONLY_BACKLIGHT
                DarkModeManager.Mode.ONLY_BACKLIGHT -> DarkModeManager.Mode.FULL
                DarkModeManager.Mode.FULL -> DarkModeManager.Mode.OFF
            }
            setMode(nextMode)
        }
    }

    private interface ModeToggleStrategy {
        fun toggleMode()
    }
}