package com.alexrcq.tvpicturesettings.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.FileObserver
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

fun Context.hasPermission(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Boolean.toInt(): Int = if (this) 1 else 0

fun Int.toBoolean(): Boolean = this == 1

fun Fragment.setWindowVisible(visible: Boolean) {
    requireActivity().window.decorView.isVisible = visible
}

fun Fragment.showToast(message: String) {
    requireContext().showToast(message)
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

fun Preference.onClick(onClick: () -> Unit) {
    setOnPreferenceClickListener {
        onClick()
        true
    }
}