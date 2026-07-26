package com.alexrcq.tvpicturesettings.platform

interface ScreenSettings {
    val isAutoBacklightEnabled: Boolean
    val maxBacklight: Int
    var backlight: Int
    var isScreenOn: Boolean
    fun toggleScreenPower()
}