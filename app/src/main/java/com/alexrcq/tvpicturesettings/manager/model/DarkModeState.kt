package com.alexrcq.tvpicturesettings.manager.model

data class DarkModeState(
    val activeMode: DarkMode,
    val targetBacklight: Int,
    val overlayState: OverlayState
)
