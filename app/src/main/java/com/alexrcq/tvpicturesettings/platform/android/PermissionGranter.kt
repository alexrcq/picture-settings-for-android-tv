package com.alexrcq.tvpicturesettings.platform.android

import android.app.AppOpsManager
import android.content.Context
import com.alexrcq.tvpicturesettings.platform.adb.AdbShell
import com.alexrcq.tvpicturesettings.util.isDangerous
import com.alexrcq.tvpicturesettings.util.permissionToOp
import com.alexrcq.tvpicturesettings.util.waitUntilAllowed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

class PermissionGranter(private val context: Context, private val adbShell: AdbShell) {

    suspend fun grantPermissions(permissions: List<String>) = withContext(Dispatchers.IO) {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val packageName = context.packageName
        val packageManager = context.packageManager

        try {
            for (permission in permissions) {
                adbShell.execute("pm grant $packageName $permission")

                val opName = permissionToOp(permission) ?: continue

                if (!packageManager.getPermissionInfo(permission, 0).isDangerous()) {
                    adbShell.execute("appops set $packageName $opName allow")
                }

                withTimeoutOrNull(PERMISSION_CONFIRM_TIMEOUT_MS) {
                    appOps.waitUntilAllowed(opName, packageName)
                } ?: Timber.w("Failed to confirm $permission (timeout)")
            }
        } finally {
            adbShell.disconnect()
        }
    }

    companion object {
        private const val PERMISSION_CONFIRM_TIMEOUT_MS = 3_000L
    }
}
