package com.alexrcq.tvpicturesettings.manager

import com.alexrcq.tvpicturesettings.manager.model.BlueLightFilterMode
import com.alexrcq.tvpicturesettings.manager.model.DarkMode
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

    val activeMode: DarkMode get() = state.value.activeMode

    val state: StateFlow<DarkModeState> = with(preferences) {
        val overlayParamsFlow = combine(
            screenDimmingEnabledFlow,
            screenDimmingPowerFlow,
            blueLightFilterModeFlow,
            blueLightFilterTempFlow,
            blueLightFilterPowerFlow,
            ::OverlayParams
        )
        combine(
            currentModeFlow, nightBacklightFlow, dayBacklightFlow, overlayParamsFlow
        ) { mode: DarkMode, nightBacklight: Int, dayBacklight: Int, overlayParams: OverlayParams ->
            calculateState(mode, nightBacklight, dayBacklight, overlayParams)
        }.stateIn(scope, SharingStarted.Eagerly, calculateInitialState())
    }

    private fun calculateState(
        mode: DarkMode, nightBacklight: Int, dayBacklight: Int, overlayParams: OverlayParams
    ): DarkModeState = DarkModeState(
        activeMode = mode,
        targetBacklight = if (mode == DarkMode.OFF) dayBacklight else nightBacklight,
        overlayState = OverlayState(
            shouldShowDimmer = overlayParams.isDimmingEnabled,
            dimmingPower = overlayParams.dimmingPower,
            shouldShowBlueLightFilter = when (overlayParams.blueLightFilterMode) {
                BlueLightFilterMode.ALWAYS -> true
                BlueLightFilterMode.NIGHT_ONLY -> mode != DarkMode.OFF
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
        val mode = if (preferences.isAdditionalDimmingEnabled) DarkMode.FULL else DarkMode.ONLY_BACKLIGHT
        applyMode(mode)
    }

    fun disableDarkMode() {
        applyMode(DarkMode.OFF)
    }

    fun toggleMode() {
        val nextMode = when (activeMode) {
            DarkMode.OFF -> if (preferences.isThreeStepsDarkModeEnabled || !preferences.isAdditionalDimmingEnabled) {
                DarkMode.ONLY_BACKLIGHT
            } else {
                DarkMode.FULL
            }
            DarkMode.ONLY_BACKLIGHT -> if (preferences.isThreeStepsDarkModeEnabled) DarkMode.FULL else DarkMode.OFF
            DarkMode.FULL -> DarkMode.OFF
        }
        applyMode(nextMode)
    }

    fun toggleFullDarkMode() {
        val mode = if (activeMode != DarkMode.FULL) DarkMode.FULL else DarkMode.ONLY_BACKLIGHT
        applyMode(mode)
    }

    fun toggleScreenDimming() = with(preferences) {
        isScreenDimmingEnabled = !isScreenDimmingEnabled
    }

    fun setScreenDimmingEnabled(enabled: Boolean) {
        preferences.isScreenDimmingEnabled = enabled
    }

    fun setScreenDimmingPower(power: Int) {
        preferences.screenDimmingPower = power.coerceIn(0, 100)
    }

    fun toggleBlueLightFilter() {
        val newMode = if (preferences.blueLightFilterMode == BlueLightFilterMode.OFF) {
            BlueLightFilterMode.ALWAYS
        } else {
            BlueLightFilterMode.OFF
        }
        preferences.blueLightFilterMode = newMode
    }

    private fun applyMode(mode: DarkMode) {
        preferences.currentMode = mode
        preferences.isScreenDimmingEnabled = (mode == DarkMode.FULL)
    }
}
