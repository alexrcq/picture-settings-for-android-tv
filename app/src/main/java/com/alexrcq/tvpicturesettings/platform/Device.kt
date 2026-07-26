package com.alexrcq.tvpicturesettings.platform

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.platform.android.SystemProperties
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys

object Device {

    val isMssp: Boolean = Build.MODEL.contains("MSSP", true)

    val is4kSmartTv: Boolean = Build.MODEL.contains("4K SMART TV", true)

    fun isSupported(context: Context): Boolean = try {
        // just trying to take a random setting
        Settings.Global.getInt(context.contentResolver, MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL)
        true
    } catch (e: Settings.SettingNotFoundException) {
        false
    }

    fun hasInactivityDetection(): Boolean = SystemProperties.containsKey(TvConstants.EVENT_UP_TIME_KEY)
}