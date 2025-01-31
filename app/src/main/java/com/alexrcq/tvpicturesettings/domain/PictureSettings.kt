package com.alexrcq.tvpicturesettings.domain

interface PictureSettings: BacklightSettings, WhiteBalanceSettings, HdrSettings, ColorTuneSettings, ContrastSettings {
    var pictureMode: Int

    fun resetToDefault()

    companion object {
        const val MAX_BACKLIGHT = 100
    }
}