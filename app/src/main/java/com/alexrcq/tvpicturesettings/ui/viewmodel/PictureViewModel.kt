package com.alexrcq.tvpicturesettings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.manager.DarkModeManager
import com.alexrcq.tvpicturesettings.platform.BacklightControlPolicy
import com.alexrcq.tvpicturesettings.platform.ScreenSettings
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PictureViewModel(
    private val screenSettings: ScreenSettings,
    private val darkModeManager: DarkModeManager,
    private val darkModePreferences: DarkModePreferences,
    backlightControlPolicy: BacklightControlPolicy
) : ViewModel() {

    val darkModeHints: StateFlow<Int> =
        darkModeManager.state
            .map { it.activeMode }
            .distinctUntilChanged()
            .flatMapLatest { mode ->
                flow {
                    while (true) {
                        emit(mode.getHintStringRes(darkModePreferences.threeStepsDarkModeEnabled))
                        delay(HOW_TO_SWITCH_MODE_HINT_DURATION)
                        emit(R.string.hold_to_toggle_extra_dimm_hint)
                        delay(HOLD_TO_DIMMING_HINT_DURATION)
                    }
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                darkModeManager.activeMode.getHintStringRes(darkModePreferences.threeStepsDarkModeEnabled)
            )

    val canAdjustBacklight: StateFlow<Boolean> = backlightControlPolicy.isAdjustmentAllowed

    fun onBacklightChange(backlight: Int) {
        if (darkModeManager.activeMode == DarkModeManager.Mode.OFF) {
            darkModePreferences.dayBacklight = backlight
        }
    }

    fun toggleDarkMode() {
        darkModeManager.toggleMode()
    }

    fun toggleFullDarkMode() {
        darkModeManager.toggleFullDarkMode()
    }

    fun toggleScreenPower() {
        screenSettings.toggleScreenPower()
    }

    companion object {
        private const val HOW_TO_SWITCH_MODE_HINT_DURATION = 7000L
        private const val HOLD_TO_DIMMING_HINT_DURATION = 3500L

        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            screenSettings: ScreenSettings,
            darkModeManager: DarkModeManager,
            darkModePreferences: DarkModePreferences,
            backlightControlPolicy: BacklightControlPolicy
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PictureViewModel(
                screenSettings, darkModeManager, darkModePreferences, backlightControlPolicy
            ) as T
        }
    }
}

private fun DarkModeManager.Mode.getHintStringRes(threeStepsEnabled: Boolean): Int = when (this) {
    DarkModeManager.Mode.OFF -> R.string.click_to_dark_mode
    DarkModeManager.Mode.ONLY_BACKLIGHT -> if (threeStepsEnabled) {
        R.string.click_to_turn_on_the_dark_filter
    } else {
        R.string.click_to_day_mode
    }
    DarkModeManager.Mode.FULL -> R.string.click_to_day_mode
}