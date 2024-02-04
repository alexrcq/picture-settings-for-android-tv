package com.alexrcq.tvpicturesettings.storage

private const val DEFAULT_COLOR_GAIN = 1024

open class MtkPictureSettings(private val global: GlobalSettings) : TvSettings.Picture {

    override var backlight: Int by global.intSetting(MtkGlobalKeys.PICTURE_BACKLIGHT)
    override var pictureMode: Int by global.intSetting(MtkGlobalKeys.PICTURE_MODE)
    override var isHdrEnabled: Boolean by global.booleanSetting(MtkGlobalKeys.PICTURE_LIST_HDR)
    override var isColorTuneEnabled: Boolean by global.booleanSetting(MtkGlobalKeys.TV_PICTURE_COLOR_TUNE_ENABLE)

    override var isLocalContrastEnabled: Boolean
        get() = global.getInt(MtkGlobalKeys.PICTURE_LOCAL_CONTRAST) == 2
        set(enabled) {
            val value = if (enabled) 2 else 0
            global.putInt(MtkGlobalKeys.PICTURE_LOCAL_CONTRAST, value)
        }

    override var isAdaptiveLumaEnabled: Boolean
        get() = global.getInt(MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL) == 2
        set(enabled) {
            val value = if (enabled) 2 else 0
            global.putInt(MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL, value)
        }

    override fun setPictureTemperature(pictureMode: Int) {
        val temperature: Int? = when (pictureMode) {
            PICTURE_MODE_DEFAULT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_BRIGHT -> PICTURE_TEMPERATURE_WARM
            PICTURE_MODE_SPORT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_MOVIE -> PICTURE_TEMPERATURE_COLD
            else -> null
        }
        if (temperature != null) {
            global.putInt(MtkGlobalKeys.PICTURE_TEMPERATURE, temperature)
        }
    }

    override fun setWhiteBalance(redGain: Int, greenGain: Int, blueGain: Int) = with(global) {
        putInt(MtkGlobalKeys.PICTURE_RED_GAIN, redGain)
        putInt(MtkGlobalKeys.PICTURE_GREEN_GAIN, greenGain)
        putInt(MtkGlobalKeys.PICTURE_BLUE_GAIN, blueGain)
    }

    override fun resetWhiteBalance() {
        setWhiteBalance(DEFAULT_COLOR_GAIN, DEFAULT_COLOR_GAIN, DEFAULT_COLOR_GAIN)
    }

    override fun resetToDefault() = with(global) {
        // same behaviour as the system app
        putInt(MtkGlobalKeys.PICTURE_RESET_TO_DEFAULT, getInt(MtkGlobalKeys.PICTURE_RESET_TO_DEFAULT) + 1)
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
    }
}