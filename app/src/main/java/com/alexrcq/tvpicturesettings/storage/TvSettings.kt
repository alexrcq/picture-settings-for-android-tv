package com.alexrcq.tvpicturesettings.storage

import android.provider.Settings
import com.alexrcq.tvpicturesettings.util.toBoolean

interface TvSettings {
    val global: GlobalSettings
    val picture: Picture
    fun isTvSourceInactive(): Boolean
    fun isAdbEnabled(): Boolean = global.getInt(Settings.Global.ADB_ENABLED).toBoolean()
    fun toggleScreenPower()

    interface Picture {
        var backlight: Int
        var pictureMode: Int
        var isHdrEnabled: Boolean
        var isColorTuneEnabled: Boolean
        var isLocalContrastEnabled: Boolean
        var isAdaptiveLumaEnabled: Boolean
        var isAutoBacklightEnabled: Boolean
        fun setPictureTemperature(pictureMode: Int)
        fun setWhiteBalance(redGain: Int, greenGain: Int, blueGain: Int)
        fun resetWhiteBalance()
        fun resetToDefault()

        companion object {
            const val MAX_BACKLIGHT = 100
        }
    }
}