package com.alexrcq.tvpicturesettings.util

import android.Manifest
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime

fun Context.registerReceiverCompat(
    receiver: BroadcastReceiver, filter: IntentFilter, isExported: Boolean = true
): Intent? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val flags = if (isExported) Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED
        registerReceiver(receiver, filter, flags)
    } else {
        registerReceiver(receiver, filter)
    }
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(message: String) {
    requireContext().showToast(message)
}

fun Preference.onClick(onClick: () -> Unit) {
    setOnPreferenceClickListener {
        onClick()
        true
    }
}

fun LocalTime.isBetween(start: LocalTime, end: LocalTime): Boolean {
    if (start == end) return false
    return if (start.isBefore(end)) {
        this >= start && this < end
    } else {
        this >= start || this < end
    }
}

fun LocalTime.nextOccurrence(from: LocalDateTime, dayOfWeek: Int? = null): LocalDateTime {
    require(dayOfWeek == null || dayOfWeek in 1..7) { "dayOfWeek must be in 1..7" }
    var candidate = from.toLocalDate().atTime(this)

    if (candidate.isBefore(from)) {
        candidate = candidate.plusDays(1)
    }

    if (dayOfWeek != null) {
        while (candidate.dayOfWeek.value != dayOfWeek) {
            candidate = candidate.plusDays(1)
        }
    }
    return candidate
}

suspend fun AppOpsManager.waitUntilAllowed(opName: String, packageName: String) = callbackFlow {
    val listener = AppOpsManager.OnOpChangedListener { _, _ ->
        if (checkOpMode(opName, packageName) == AppOpsManager.MODE_ALLOWED) {
            trySend(Unit)
        }
    }
    startWatchingMode(opName, packageName, listener)
    listener.onOpChanged(null, null)
    awaitClose {
        stopWatchingMode(listener)
    }
}.first()

fun AppOpsManager.checkOpMode(op: String, packageName: String): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        unsafeCheckOpNoThrow(op, android.os.Process.myUid(), packageName)
    } else {
        @Suppress("DEPRECATION") checkOpNoThrow(op, android.os.Process.myUid(), packageName)
    }
}

fun permissionToOp(permission: String): String? =
    AppOpsManager.permissionToOp(permission) ?: when (permission) {
        Manifest.permission.PACKAGE_USAGE_STATS -> AppOpsManager.OPSTR_GET_USAGE_STATS
        else -> null
    }

fun Context.hasPermission(permission: String): Boolean {
    val op = permissionToOp(permission)
        ?: return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    val mode = getSystemService(AppOpsManager::class.java).checkOpMode(op, packageName)
    return mode == AppOpsManager.MODE_ALLOWED ||
        (mode == AppOpsManager.MODE_DEFAULT &&
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
}

fun PermissionInfo.isDangerous(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    protection == PermissionInfo.PROTECTION_DANGEROUS
} else {
    @Suppress("DEPRECATION")
    (protectionLevel and PermissionInfo.PROTECTION_MASK_BASE) == PermissionInfo.PROTECTION_DANGEROUS
}