package com.alexrcq.tvpicturesettings.storage

interface GlobalSettings {

    fun putInt(key: String, value: Int)

    fun getInt(key: String): Int

    object Keys {
        const val PICTURE_BACKLIGHT = "picture_backlight"
        const val PICTURE_TEMPERATURE = "picture_color_temperature"
        const val PICTURE_ADAPTIVE_LUMA_CONTROL = "tv_picture_video_adaptive_luma_control"
        const val PICTURE_LOCAL_CONTRAST = "tv_picture_video_local_contrast_control"
        const val PICTURE_MODE = "picture_mode"
        const val PICTURE_BRIGHTNESS = "picture_brightness"
        const val PICTURE_CONTRAST = "picture_contrast"
        const val PICTURE_SATURATION = "picture_saturation"
        const val PICTURE_HUE = "picture_hue"
        const val PICTURE_SHARPNESS = "picture_sharpness"
        const val PICTURE_LIST_HDR = "picture_list_hdr"
        const val PICTURE_RESET_TO_DEFAULT = "picture_reset_to_default"
        const val PICTURE_AUTO_BACKLIGHT = "picture_auto_backlight"
        const val POWER_PICTURE_OFF = "power_picture_off"
        const val TV_PICTURE_COLOR_TUNE_ENABLE = "tv_picture_color_tune_enable"
        const val PICTURE_RED_GAIN = "picture_red_gain"
        const val PICTURE_GREEN_GAIN = "picture_green_gain"
        const val PICTURE_BLUE_GAIN = "picture_blue_gain"
    }

    object Values {
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