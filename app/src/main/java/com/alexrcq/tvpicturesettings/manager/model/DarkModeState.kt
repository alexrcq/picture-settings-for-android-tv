package com.alexrcq.tvpicturesettings.manager.model

import com.alexrcq.tvpicturesettings.manager.DarkModeManager

data class DarkModeState(
    val activeMode: DarkModeManager.Mode,
    val targetBacklight: Int,
    val overlayState: OverlayState
)