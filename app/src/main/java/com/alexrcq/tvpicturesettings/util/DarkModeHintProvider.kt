package com.alexrcq.tvpicturesettings.util

import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences

class DarkModeHintProvider(private val darkModePreferences: DarkModePreferences) {
    fun getClickToNextModeHint(mode: DarkModeManager.Mode): Int = when (mode) {
        DarkModeManager.Mode.OFF -> R.string.click_to_dark_mode
        DarkModeManager.Mode.ONLY_BACKLIGHT -> getHintForOnlyBacklight()
        DarkModeManager.Mode.FULL -> R.string.click_to_day_mode
    }

    private fun getHintForOnlyBacklight(): Int = if (darkModePreferences.threeStepsDarkModeEnabled) {
        R.string.click_to_turn_on_the_dark_filter
    } else {
        R.string.click_to_day_mode
    }
}
