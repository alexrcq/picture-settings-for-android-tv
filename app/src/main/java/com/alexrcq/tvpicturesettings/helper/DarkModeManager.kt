package com.alexrcq.tvpicturesettings.helper

import androidx.annotation.StringRes
import com.alexrcq.tvpicturesettings.R

interface DarkModeManager {
    val currentMode: Mode
    fun setMode(mode: Mode)
    fun toggleMode()
    fun setDayBacklight(value: Int)
    fun setNightBacklight(value: Int)
    fun setScreenFilterEnabled(enabled: Boolean)
    fun setScreenFilterPower(value: Int)
    fun toggleScreenFilter()
    fun setStepByStepDarkModeEnabled(enabled: Boolean)

    enum class Mode(@StringRes val message: Int) {
        OFF(R.string.dark_mode_off),
        ONLY_BACKLIGHT(R.string.dark_mode_only_backlight),
        FULL(R.string.dark_mode_full)
    }
}