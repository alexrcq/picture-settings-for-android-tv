package com.alexrcq.tvpicturesettings.storage

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.alexrcq.tvpicturesettings.util.AdbUtils
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import java.io.IOException
import java.net.Socket
import java.util.concurrent.Executors

open class GlobalSettings(context: Context) {

    private var contentResolver = context.contentResolver
    private var connection: AdbConnection? = null
    private val executor = Executors.newSingleThreadExecutor()

    init {
        var crypto: AdbCrypto? = AdbUtils.readCryptoConfig(context.filesDir)
        executor.execute {
            if (crypto == null) {
                crypto = AdbUtils.writeNewCryptoConfig(context.filesDir)
                if (crypto == null) {
                    Log.d("GlobalSettings", "Key Pair Generation Failed")
                    return@execute
                }
                Log.d("GlobalSettings", "New Key Pair Generated")
            }
            connection = AdbConnection.create(Socket("127.0.0.1", 5555), crypto)
            connection?.connect()
        }
    }

    fun putInt(key: String, value: Int) {
        executor.execute {
            try {
                connection?.open("shell:settings put global $key $value")
            } catch (e: IOException) {
                // do nothing, wait for the user
            }
        }
    }

    fun getInt(key: String?): Int {
        return Settings.Global.getInt(contentResolver, key)
    }

    companion object {
        const val KEY_PICTURE_BACKLIGHT = "picture_backlight"
        const val KEY_PICTURE_TEMPERATURE = "picture_color_temperature"
        const val KEY_PICTURE_ADAPTIVE_LUMA_CONTROL = "tv_picture_video_adaptive_luma_control"
        const val KEY_PICTURE_LOCAL_CONTRAST = "tv_picture_video_local_contrast_control"
        const val KEY_PICTURE_NOISE_REDUCTION = "tv_picture_advance_video_dnr"
        const val KEY_PICTURE_MODE = "picture_mode"
        const val KEY_PICTURE_BRIGHTNESS = "picture_brightness"
        const val KEY_PICTURE_CONTRAST = "picture_contrast"
        const val KEY_PICTURE_SATURATION = "picture_saturation"
        const val KEY_PICTURE_HUE = "picture_hue"
        const val KEY_PICTURE_SHARPNESS = "picture_sharpness"
        const val KEY_POWER_PICTURE_OFF = "power_picture_off"
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