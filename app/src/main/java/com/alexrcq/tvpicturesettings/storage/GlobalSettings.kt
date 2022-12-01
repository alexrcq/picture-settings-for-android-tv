package com.alexrcq.tvpicturesettings.storage

import android.content.ContentResolver
import android.provider.Settings
import timber.log.Timber

open class GlobalSettings(private val contentResolver: ContentResolver) {
    fun putInt(key: String, value: Int) {
        Timber.d("putInt : key = $key, value = $value")
        Settings.Global.putInt(contentResolver, key, value)
    }
    fun getInt(key: String): Int {
        val value = Settings.Global.getInt(contentResolver, key)
        Timber.d("getInt : key = $key, value = $value")
        return value
    }

    object Keys {
        const val PICTURE_BACKLIGHT = "picture_backlight"
        const val PICTURE_TEMPERATURE = "picture_color_temperature"
        const val PICTURE_ADAPTIVE_LUMA_CONTROL = "tv_picture_video_adaptive_luma_control"
        const val PICTURE_LOCAL_CONTRAST = "tv_picture_video_local_contrast_control"
        const val PICTURE_NOISE_REDUCTION = "tv_picture_advance_video_dnr"
        const val PICTURE_MODE = "picture_mode"
        const val PICTURE_BRIGHTNESS = "picture_brightness"
        const val PICTURE_CONTRAST = "picture_contrast"
        const val PICTURE_SATURATION = "picture_saturation"
        const val PICTURE_HUE = "picture_hue"
        const val PICTURE_SHARPNESS = "picture_sharpness"
        const val POWER_PICTURE_OFF = "power_picture_off"
        const val PICTURE_LIST_HDR = "picture_list_hdr"
        const val PICTURE_RESET_TO_DEFAULT = "picture_reset_to_default"
        const val PICTURE_AUTO_BACKLIGHT = "picture_auto_backlight"
    }
}