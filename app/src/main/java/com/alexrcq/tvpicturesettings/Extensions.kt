package com.alexrcq.tvpicturesettings

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Button
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import timber.log.Timber

fun Button.requestFocusForced() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
}

fun Context.hasPermission(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

val Context.isDebuggingEnabled: Boolean
    get() = Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1

val Context.isCurrentTvSupported: Boolean
    get() {
        return try {
            // just trying to take a random setting
            PictureSettings(this).isAdaptiveLumaEnabled
            true
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

val Context.isDarkModeManagerEnabled: Boolean
    get() {
        val prefString = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return prefString != null && prefString.contains(packageName + "/" + DarkModeManager::class.java.name)
    }

fun Context.enableAccessibilityService(cls: Class<out AccessibilityService>) {
    Timber.d( "enabling the ${cls.name} service...")
    val allEnabledServices =
        Settings.Secure.getString(contentResolver, "enabled_accessibility_services")
    Settings.Secure.putString(
        contentResolver,
        "enabled_accessibility_services",
        "$allEnabledServices:${packageName}/${cls.name}"
    )
}
