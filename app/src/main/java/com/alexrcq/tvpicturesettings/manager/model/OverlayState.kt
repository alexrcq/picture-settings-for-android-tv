package com.alexrcq.tvpicturesettings.manager.model

data class OverlayState(
    val shouldShowDimmer: Boolean,
    val dimmingPower: Int,
    val shouldShowBlueLightFilter: Boolean,
    val blueLightFilterTemp: Int,
    val blueLightFilterPower: Int
) {
    fun withWarningDimming(): OverlayState {
        val currentPower = if (shouldShowDimmer) dimmingPower else 0
        val warningPower = (currentPower + 20).coerceIn(MIN_WARNING_DIMMING_POWER, MAX_WARNING_DIMMING_POWER)
        return copy(shouldShowDimmer = true, dimmingPower = warningPower)
    }

    companion object {
        private const val MIN_WARNING_DIMMING_POWER = 75
        private const val MAX_WARNING_DIMMING_POWER = 92
    }
}
