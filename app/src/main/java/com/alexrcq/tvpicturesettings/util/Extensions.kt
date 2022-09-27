package com.alexrcq.tvpicturesettings.util

import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.alexrcq.tvpicturesettings.service.DarkModeManager
import timber.log.Timber

fun AlertDialog.setButtonFocused(@DialogButton button: Int) {
    this.getButton(button).apply {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }
}

fun Context.hasPermission(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

val Context.isDebuggingEnabled: Boolean
    get() = Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1

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
