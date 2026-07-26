package com.alexrcq.tvpicturesettings.platform.adb

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PermissionInfo
import android.os.Build
import android.util.Base64
import com.alexrcq.tvpicturesettings.util.waitUntilAllowed
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.File
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.coroutines.cancellation.CancellationException

class LocalAdbShell(private val context: Context) : AdbShell {

    private var adbConnection: AdbConnection? = null

    override suspend fun connect() = withContext(Dispatchers.IO) {
        if (adbConnection != null) return@withContext

        Timber.d("Connecting to $LOCAL_HOST:$DEFAULT_PORT...")
        var socket: Socket? = null
        var connection: AdbConnection? = null

        try {
            socket = Socket(LOCAL_HOST, DEFAULT_PORT)
            connection = AdbConnection.create(socket, getOrCreateCrypto())

            if (!connection.connect(15, TimeUnit.SECONDS, false)) {
                throw TimeoutException("Connection refused or timed out")
            }

            adbConnection = connection
            Timber.d("Connected successfully")
        } catch (e: Exception) {
            Timber.w("Connection failed: ${e.message}")
            runCatching { connection?.close() ?: socket?.close() }
            throw e
        }
    }

    override suspend fun execute(command: String): String = withContext(Dispatchers.IO) {
        if (adbConnection == null) connect()
        Timber.d(command)

        val output = StringBuilder()
        try {
            withTimeout(15_000L) {
                val connection = adbConnection ?: throw IllegalStateException("ADB not connected")
                connection.open("shell:$command").use { stream ->
                    while (true) output.append(String(stream.read(), Charsets.UTF_8))
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (e.message != "Stream closed") {
                Timber.e(e, "Command failed: $command")
                disconnect()
            }
        }
        output.toString()
    }

    override suspend fun grantPermissions(permissions: List<String>) = withContext(Dispatchers.IO) {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val packageName = context.packageName
        val packageManager = context.packageManager

        for (permission in permissions) {
            execute("pm grant $packageName $permission")

            val opName = AppOpsManager.permissionToOp(permission) ?: continue

            if (!packageManager.getPermissionInfo(permission, 0).isDangerous()) {
                execute("appops set $packageName $opName allow")
            }

            val confirmation = withTimeoutOrNull(3000L) {
                appOps.waitUntilAllowed(opName, packageName)
            }

            if (confirmation == null) {
                Timber.w("Failed to confirm $permission (timeout)")
            }
        }
    }

    override suspend fun captureScreen(saveDir: String): Boolean = withContext(Dispatchers.IO) {
        execute("mkdir -p \"$saveDir\"")

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS"))
        val filePath = "$saveDir/screenshot_$timestamp.png"

        execute("screencap -p \"$filePath\"")
        execute("[ -s \"$filePath\" ] && echo success").contains("success")
    }

    override fun disconnect() {
        if (adbConnection == null) return

        runCatching { adbConnection?.close() }

        adbConnection = null
        Timber.d("Disconnected")
    }

    private fun getOrCreateCrypto(): AdbCrypto {
        val publicKey = File(context.cacheDir, "pub.key")
        val privateKey = File(context.cacheDir, "priv.key")

        val base64 = AdbBase64 { Base64.encodeToString(it, Base64.NO_WRAP) }

        return if (publicKey.exists() && privateKey.exists()) {
            AdbCrypto.loadAdbKeyPair(base64, privateKey, publicKey)
        } else {
            AdbCrypto.generateAdbKeyPair(base64).apply { saveAdbKeyPair(privateKey, publicKey) }
        }
    }

    private fun PermissionInfo.isDangerous(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        protection == PermissionInfo.PROTECTION_DANGEROUS
    } else {
        @Suppress("DEPRECATION")
        (protectionLevel and PermissionInfo.PROTECTION_MASK_BASE) == PermissionInfo.PROTECTION_DANGEROUS
    }

    companion object {
        private const val LOCAL_HOST = "127.0.0.1"
        private const val DEFAULT_PORT = 5555
    }
}