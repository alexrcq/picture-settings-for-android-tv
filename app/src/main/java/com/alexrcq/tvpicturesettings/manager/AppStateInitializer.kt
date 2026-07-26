package com.alexrcq.tvpicturesettings.manager

import com.alexrcq.tvpicturesettings.platform.ScreenSettings
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.util.isBetween
import java.time.LocalTime

class AppStateInitializer(
    private val darkModePreferences: DarkModePreferences,
    private val darkModeManager: DarkModeManager,
    private val darkModeScheduler: DarkModeScheduler,
    private val screenScheduler: ScreenScheduler,
    private val screenSettings: ScreenSettings
) {

    fun init() {
        ensureDayBacklightIsSet()
        restoreSchedule()
        syncDarkModeState()
    }

    private fun ensureDayBacklightIsSet() {
        if (darkModePreferences.dayBacklight !in 0..screenSettings.maxBacklight) {
            darkModePreferences.dayBacklight = screenSettings.backlight
        }
    }

    private fun restoreSchedule() {
        darkModeScheduler.updateSchedule()
        screenScheduler.updateSchedule()
    }

    private fun syncDarkModeState() {
        if (!darkModePreferences.isDarkModeScheduleEnabled || darkModePreferences.disableDarkModeOnPowerOn) return

        val nightStart = darkModePreferences.darkModeTime
        val nightEnd = darkModePreferences.dayModeTime

        val isNightNow = LocalTime.now().isBetween(nightStart, nightEnd)

        if (isNightNow) {
            darkModeManager.enableDarkMode()
        } else {
            darkModeManager.disableDarkMode()
        }
    }
}
