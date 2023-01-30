package com.alexrcq.tvpicturesettings

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import android.provider.Settings
import android.widget.Button
import com.alexrcq.tvpicturesettings.storage.PictureSettingsImpl
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume

fun Button.requestFocusForced() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
}

fun Context.hasPermission(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

val Context.hasActiveTvSource: Boolean
    get() {
        val currentSourceNameUri = Uri.parse(TvConstants.CURRENT_TV_SOURCE_URI)
        val cursor = contentResolver.query(
            currentSourceNameUri,
            null,
            null,
            null
        ) ?: return false
        cursor.moveToFirst()
        val currentSourceName = try {
            cursor.getString(cursor.getColumnIndexOrThrow("value"))
        } catch (e: Exception) {
            return false
        } finally {
            cursor.close()
        }
        Timber.d("currentSource: $currentSourceName")
        return currentSourceName != "Null"
    }

val Context.isAdbEnabled: Boolean
    get() = Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1

val Context.isCurrentTvSupported: Boolean
    get() {
        return try {
            // just trying to take a random setting
            PictureSettingsImpl(this).isAdaptiveLumaEnabled
            true
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

fun isModelName(modelName: String): Boolean {
    return Build.MODEL.contains(modelName, true)
}

fun Context.isAccessibilityServiceEnabled(cls: Class<out AccessibilityService>): Boolean {
    val allEnabledServices = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return allEnabledServices != null && allEnabledServices.contains(packageName + "/" + cls.name)
}

fun Context.enableAccessibilityService(cls: Class<out AccessibilityService>) {
    Timber.d("enabling the ${cls.name} service...")
    val allEnabledServices =
        Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    Settings.Secure.putString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        "$allEnabledServices:${packageName}/${cls.name}"
    )
}

suspend fun File.waitForFileEvent(mask: Int) = suspendCancellableCoroutine { continuation ->
    @Suppress("DEPRECATION")
    val fileObserver = object : FileObserver(this.path, mask) {
        override fun onEvent(event: Int, path: String?) {
            if (event == CREATE) {
                continuation.resume(Unit)
                stopWatching()
            }
        }
    }
    fileObserver.startWatching()
    continuation.invokeOnCancellation {
        fileObserver.stopWatching()
    }
}