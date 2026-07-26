package com.alexrcq.tvpicturesettings.manager

import androidx.annotation.StringRes
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.manager.model.BlueLightFilterMode
import com.alexrcq.tvpicturesettings.manager.model.DarkModeState
import com.alexrcq.tvpicturesettings.manager.model.OverlayParams
import com.alexrcq.tvpicturesettings.manager.model.OverlayState
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DarkModeManager(private val preferences: DarkModePreferences, scope: CoroutineScope) {

    val activeMode: Mode get() = state.value.activeMode

    val state: StateFlow<DarkModeState> = with(preferences) {
        combine(
            currentModeFlow, nightBacklightFlow, dayBacklightFlow, overlayParamsFlow
        ) { mode: Mode, nightBacklight: Int, dayBacklight: Int, overlayParams: OverlayParams ->
            calculateState(mode, nightBacklight, dayBacklight, overlayParams)
        }.stateIn(scope, SharingStarted.Eagerly, calculateInitialState())
    }

    private fun calculateState(
        mode: Mode, nightBacklight: Int, dayBacklight: Int, overlayParams: OverlayParams
    ): DarkModeState = DarkModeState(
        activeMode = mode,
        targetBacklight = if (mode == Mode.OFF) dayBacklight else nightBacklight,
        overlayState = OverlayState(
            shouldShowDimmer = overlayParams.isDimmingEnabled,
            dimmingPower = overlayParams.dimmingPower,
            shouldShowBlueLightFilter = when (overlayParams.blueLightFilterMode) {
                BlueLightFilterMode.ALWAYS -> true
                BlueLightFilterMode.NIGHT_ONLY -> mode != Mode.OFF
                BlueLightFilterMode.OFF -> false
            },
            blueLightFilterTemp = overlayParams.blueLightFilterTemp,
            blueLightFilterPower = overlayParams.blueLightFilterPower
        )
    )

    private fun calculateInitialState(): DarkModeState {
        val mode = preferences.currentMode
        val nightBacklight = preferences.nightBacklight
        val dayBacklight = preferences.dayBacklight
        val overlayParams = OverlayParams(
            isDimmingEnabled = preferences.isScreenDimmingEnabled,
            dimmingPower = preferences.screenDimmingPower,
            blueLightFilterMode = preferences.blueLightFilterMode,
            blueLightFilterTemp = preferences.blueLightFilterTemp,
            blueLightFilterPower = preferences.blueLightFilterPower
        )
        return calculateState(mode, nightBacklight, dayBacklight, overlayParams)
    }

    fun enableDarkMode() {
        val mode = if (preferences.isAdditionalDimmingEnabled) Mode.FULL else Mode.ONLY_BACKLIGHT
        applyMode(mode)
    }

    fun disableDarkMode() {
        applyMode(Mode.OFF)
    }

    fun toggleMode() {
        val nextMode = when (activeMode) {
            Mode.OFF -> if (preferences.threeStepsDarkModeEnabled || !preferences.isAdditionalDimmingEnabled) {
                Mode.ONLY_BACKLIGHT
            } else {
                Mode.FULL
            }
            Mode.ONLY_BACKLIGHT -> if (preferences.threeStepsDarkModeEnabled) Mode.FULL else Mode.OFF
            Mode.FULL -> Mode.OFF
        }
        applyMode(nextMode)
    }

    fun toggleFullDarkMode() {
        val mode = if (activeMode != Mode.FULL) Mode.FULL else Mode.ONLY_BACKLIGHT
        applyMode(mode)
    }

    fun toggleScreenDimming() = with(preferences) {
        isScreenDimmingEnabled = !isScreenDimmingEnabled
    }

    fun setScreenDimmingEnabled(enabled: Boolean) {
        preferences.isScreenDimmingEnabled = enabled
    }

    fun setScreenDimmingPower(power: Int) {
        preferences.screenDimmingPower = power
    }

    fun toggleBlueLightFilter() {
        val newMode = if (preferences.blueLightFilterMode == BlueLightFilterMode.OFF) {
            BlueLightFilterMode.ALWAYS
        } else {
            BlueLightFilterMode.OFF
        }
        preferences.blueLightFilterMode = newMode
    }

    private fun applyMode(mode: Mode) {
        preferences.currentMode = mode
        preferences.isScreenDimmingEnabled = (mode == Mode.FULL)
    }

    enum class Mode(@StringRes val message: Int) {
        OFF(R.string.dark_mode_off), ONLY_BACKLIGHT(R.string.dark_mode_only_backlight), FULL(R.string.dark_mode_max)
    }
}
