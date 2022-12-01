package com.alexrcq.tvpicturesettings.storage

interface PictureSettings {

    var backlight: Int

    var brightness: Int

    var contrast: Int

    var saturation: Int

    var hue: Int

    var sharpness: Int

    var temperature: Int

    var pictureMode: Int

    var noiseReduction: Int

    var isAdaptiveLumaEnabled: Boolean

    var isLocalContrastEnabled: Boolean

    var isHdrEnabled: Boolean

    fun turnOffScreen()

    fun resetToDefault()

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