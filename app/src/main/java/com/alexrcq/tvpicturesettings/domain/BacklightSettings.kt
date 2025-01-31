package com.alexrcq.tvpicturesettings.domain

import kotlinx.coroutines.flow.StateFlow

interface BacklightSettings {
    var backlight: Int
    var isAutoBacklightEnabled: Boolean
    val backlightAdjustAllowedFlow: StateFlow<Boolean>

    fun isBacklightAdjustAllowed(): Boolean = backlightAdjustAllowedFlow.value
}