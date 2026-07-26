package com.alexrcq.tvpicturesettings.platform.mtk

import com.alexrcq.tvpicturesettings.platform.VideoSettings
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys

class MtkVideoSettings(private val globalSettings: GlobalSettingsStore) : VideoSettings {

    override fun applyPictureMode(mode: Int) = with(globalSettings) {
        putInt(MtkGlobalKeys.PICTURE_MODE, mode)
        val temperature: Int? = when (mode) {
            PICTURE_MODE_DEFAULT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_BRIGHT -> PICTURE_TEMPERATURE_WARM
            PICTURE_MODE_SPORT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_MOVIE -> PICTURE_TEMPERATURE_COLD
            else -> null
        }
        if (temperature != null) {
            putInt(MtkGlobalKeys.PICTURE_TEMPERATURE, temperature)
        }
    }

    override fun resetWhiteBalance() {
        COLOR_GAIN_KEYS.forEach { key -> globalSettings.putInt(key, DEFAULT_COLOR_GAIN) }
    }

    override fun resetToDefault() = with(globalSettings) {
        // Same behaviour as the system app
        val resetValue = getInt(MtkGlobalKeys.PICTURE_RESET_TO_DEFAULT, 0)
        putInt(MtkGlobalKeys.PICTURE_RESET_TO_DEFAULT, resetValue + 1)
        putInt(MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT, 0)
    }

    companion object {
        const val PICTURE_MODE_DEFAULT = 7
        const val PICTURE_MODE_BRIGHT = 3
        const val PICTURE_MODE_SPORT = 2
        const val PICTURE_MODE_MOVIE = 9
        const val PICTURE_MODE_USER = 0
        const val PICTURE_TEMPERATURE_WARM = 1
        const val PICTURE_TEMPERATURE_COLD = 2
        const val PICTURE_TEMPERATURE_DEFAULT = 3

        const val DEFAULT_COLOR_GAIN = 1024

        val COLOR_GAIN_KEYS = listOf(
            MtkGlobalKeys.PICTURE_RED_GAIN, MtkGlobalKeys.PICTURE_GREEN_GAIN, MtkGlobalKeys.PICTURE_BLUE_GAIN
        )
    }
}