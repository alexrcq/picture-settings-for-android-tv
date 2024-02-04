package com.alexrcq.tvpicturesettings.util

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings

object ServiceUtils {
    fun ensureAccessibilityServiceEnabled(context: Context, cls: Class<out AccessibilityService>) {
        val enabledServices =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val isServiceEnabled = enabledServices != null && enabledServices.contains(context.packageName + "/" + cls.name)
        if (!isServiceEnabled) {
            Settings.Secure.putString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                "$enabledServices:${context.packageName}/${cls.name}"
            )
        }
    }
}