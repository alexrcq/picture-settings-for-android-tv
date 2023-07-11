package com.alexrcq.tvpicturesettings

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.FileObserver
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.helper.GlobalSettings.Keys.PICTURE_ADAPTIVE_LUMA_CONTROL
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

val Context.isAdbEnabled: Boolean
    get() = Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0).toBoolean()

val Context.isCurrentTvSupported: Boolean
    get() {
        return try {
            // just trying to take a random setting
            Settings.Global.getInt(contentResolver, PICTURE_ADAPTIVE_LUMA_CONTROL, 0)
            true
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

fun Context.ensureAccessibilityServiceEnabled(cls: Class<out AccessibilityService>) {
    val enabledServices =
        Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    val isServiceEnabled =
        enabledServices != null && enabledServices.contains(packageName + "/" + cls.name)
    if (!isServiceEnabled) {
        Settings.Secure.putString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            "$enabledServices:${packageName}/${cls.name}"
        )
    }
}

fun Context.showActivationToast(
    isActivated: Boolean,
    @StringRes activationMessage: Int,
    @StringRes deactivationMessage: Int
) {
    val resId: Int = if (isActivated)
        activationMessage
    else
        deactivationMessage
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Boolean.toInt(): Int = if (this) 1 else 0

fun Int.toBoolean(): Boolean = this == 1

var Activity.isWindowVisible: Boolean
    set(value) {
        window.decorView.isVisible = value
    }
    get() = window.decorView.isVisible

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

fun prepareDir(path: String): File {
    val directory = File(path)
    if (directory.exists()) {
        return directory
    }
    directory.mkdirs()
    return directory
}

fun hasActiveTvSource(contentResolver: ContentResolver): Boolean {
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

fun Preference.onClick(onClick: () -> Unit) {
    setOnPreferenceClickListener {
        onClick.invoke()
        true
    }
}