package com.alexrcq.tvpicturesettings.platform

interface VideoSettings {
    fun applyPictureMode(mode: Int)
    fun resetWhiteBalance()
    fun resetToDefault()
}