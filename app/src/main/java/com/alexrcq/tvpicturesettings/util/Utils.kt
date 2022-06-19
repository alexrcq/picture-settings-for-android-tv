package com.alexrcq.tvpicturesettings.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import com.alexrcq.tvpicturesettings.service.DarkFilterService
import com.alexrcq.tvpicturesettings.storage.GlobalSettings

object Utils {

    fun isCurrentTVSupported(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, GlobalSettings.KEY_PICTURE_BACKLIGHT)
            true
        } catch (e: Settings.SettingNotFoundException) {
            Log.e("Utils", "The current TV model is not supported", e)
            false
        }
    }

    fun isDebuggingEnabled(context: Context): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1

    fun hasPermission(context: Context, permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    fun isDarkFilterServiceEnabled(context: Context): Boolean {
        val prefString = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return prefString != null && prefString.contains(context.packageName + "/" + DarkFilterService::class.java.name)
    }
}