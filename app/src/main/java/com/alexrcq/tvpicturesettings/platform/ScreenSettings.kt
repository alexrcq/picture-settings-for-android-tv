package com.alexrcq.tvpicturesettings.platform

interface ScreenSettings {
    val maxBacklight: Int
    var backlight: Int
    var isScreenOn: Boolean
    fun toggleScreenPower()
}