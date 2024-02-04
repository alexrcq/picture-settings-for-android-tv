package com.alexrcq.tvpicturesettings.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys

object DeviceUtils {
    fun isCurrentTvSupported(context: Context): Boolean = try {
        // just trying to take a random setting
        Settings.Global.getInt(context.contentResolver, MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL, 0)
        true
    } catch (e: Settings.SettingNotFoundException) {
        false
    }

    fun isModel4kSmartTv(): Boolean = Build.MODEL.contains(TvConstants.TV_MODEL_4K_SMART_TV, true)

    fun isModelMssp(): Boolean = Build.MODEL.contains(TvConstants.TV_MODEL_MSSP_PREFIX, true)
}