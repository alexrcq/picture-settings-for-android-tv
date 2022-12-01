package com.alexrcq.tvpicturesettings.storage

import android.content.Context
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_ADAPTIVE_LUMA_CONTROL
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_AUTO_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_BRIGHTNESS
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_CONTRAST
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_HUE
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_LIST_HDR
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_LOCAL_CONTRAST
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_MODE
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_NOISE_REDUCTION
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_RESET_TO_DEFAULT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_SATURATION
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_SHARPNESS
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_TEMPERATURE
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.POWER_PICTURE_OFF
import com.alexrcq.tvpicturesettings.storage.PictureSettings.Companion.PICTURE_MODE_BRIGHT
import com.alexrcq.tvpicturesettings.storage.PictureSettings.Companion.PICTURE_MODE_DEFAULT
import com.alexrcq.tvpicturesettings.storage.PictureSettings.Companion.PICTURE_MODE_MOVIE
import com.alexrcq.tvpicturesettings.storage.PictureSettings.Companion.PICTURE_MODE_SPORT
import com.alexrcq.tvpicturesettings.storage.PictureSettings.Companion.PICTURE_TEMPERATURE_COLD
import com.alexrcq.tvpicturesettings.storage.PictureSettings.Companion.PICTURE_TEMPERATURE_DEFAULT
import com.alexrcq.tvpicturesettings.storage.PictureSettings.Companion.PICTURE_TEMPERATURE_WARM

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

open class PictureSettingsImpl @Inject constructor(@ApplicationContext context: Context) :
    GlobalSettings(context.contentResolver), PictureSettings {

    override var backlight: Int
        set(value) {
            putInt(PICTURE_BACKLIGHT, value)
        }
        get() = getInt(PICTURE_BACKLIGHT)

    override var brightness: Int
        set(value) {
            putInt(PICTURE_BRIGHTNESS, value)
        }
        get() = getInt(PICTURE_BRIGHTNESS)

    override var contrast: Int
        set(value) {
            putInt(PICTURE_CONTRAST, value)
        }
        get() = getInt(PICTURE_CONTRAST)

    override var saturation: Int
        set(value) {
            putInt(PICTURE_SATURATION, value)
        }
        get() = getInt(PICTURE_SATURATION)

    override var hue: Int
        set(value) {
            putInt(PICTURE_HUE, value)
        }
        get() = getInt(PICTURE_HUE)

    override var sharpness: Int
        set(value) {
            putInt(PICTURE_SHARPNESS, value)
        }
        get() = getInt(PICTURE_SHARPNESS)

    override var temperature: Int
        set(value) {
            putInt(PICTURE_TEMPERATURE, value)
        }
        get() = getInt(PICTURE_TEMPERATURE)

    override var pictureMode: Int
        set(value) {
            putInt(PICTURE_MODE, value)
            val temperature: Int? = when (value) {
                PICTURE_MODE_DEFAULT -> PICTURE_TEMPERATURE_DEFAULT
                PICTURE_MODE_BRIGHT -> PICTURE_TEMPERATURE_WARM
                PICTURE_MODE_SPORT -> PICTURE_TEMPERATURE_DEFAULT
                PICTURE_MODE_MOVIE -> PICTURE_TEMPERATURE_COLD
                else -> null
            }
            if (temperature != null) {
                this.temperature = temperature
            }
        }
        get() = getInt(PICTURE_MODE)

    override var noiseReduction: Int
        set(value) {
            putInt(PICTURE_NOISE_REDUCTION, value)
        }
        get() = getInt(PICTURE_NOISE_REDUCTION)

    override var isAdaptiveLumaEnabled: Boolean
        set(value) {
            val enabled = if (value) 0 else 2
            putInt(PICTURE_ADAPTIVE_LUMA_CONTROL, enabled)
        }
        get() {
            return when (getInt(PICTURE_ADAPTIVE_LUMA_CONTROL)) {
                0 -> true
                else -> false
            }
        }

    override var isLocalContrastEnabled: Boolean
        set(value) {
            val enabled = if (value) 0 else 2
            putInt(PICTURE_LOCAL_CONTRAST, enabled)
        }
        get() {
            return when (getInt(PICTURE_LOCAL_CONTRAST)) {
                0 -> true
                else -> false
            }
        }

    override var isHdrEnabled: Boolean
        set(value) {
            val enabled = if (value) 1 else 0
            putInt(PICTURE_LIST_HDR, enabled)
        }
        get() {
            return when (getInt(PICTURE_LIST_HDR)) {
                1 -> true
                else -> false
            }
        }

    override fun turnOffScreen() {
        putInt(POWER_PICTURE_OFF, 0)
    }

    override fun resetToDefault() {
        // the same behavior as the system app
        putInt(PICTURE_RESET_TO_DEFAULT, getInt(PICTURE_RESET_TO_DEFAULT) + 1)
        putInt(PICTURE_AUTO_BACKLIGHT, 0)
    }
}