package com.alexrcq.tvpicturesettings.platform.adb

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.io.File
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.coroutines.cancellation.CancellationException

class AdbShell(
    private val context: Context,
    private val host: String = DEFAULT_HOST,
    private val port: Int = DEFAULT_PORT
) {

    private var adbConnection: AdbConnection? = null

    suspend fun connect() = withContext(Dispatchers.IO) {
        if (adbConnection != null) return@withContext

        Timber.d("Connecting to $host:$port...")
        var socket: Socket? = null
        var connection: AdbConnection? = null

        try {
            socket = Socket(host, port)
            connection = AdbConnection.create(socket, getOrCreateCrypto())

            if (!connection.connect(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS, false)) {
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

    suspend fun execute(command: String): String = withContext(Dispatchers.IO) {
        if (adbConnection == null) connect()
        Timber.d(command)

        val output = StringBuilder()
        try {
            withTimeout(COMMAND_TIMEOUT_MS) {
                val connection = adbConnection ?: throw IllegalStateException("ADB not connected")
                connection.open("shell:$command").use { stream ->
                    while (true) output.append(String(stream.read(), Charsets.UTF_8))
                }
            }
        } catch (e: TimeoutCancellationException) {
            disconnect()
            throw TimeoutException("Command timed out: $command")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e.message != "Stream closed") {
                Timber.e(e, "Command failed: $command")
                disconnect()
            }
        }
        output.toString()
    }

    fun disconnect() {
        if (adbConnection == null) return

        runCatching { adbConnection?.close() }

        adbConnection = null
        Timber.d("Disconnected")
    }

    private fun getOrCreateCrypto(): AdbCrypto {
        val publicKey = File(context.cacheDir, PUBLIC_KEY_FILE)
        val privateKey = File(context.cacheDir, PRIVATE_KEY_FILE)

        val base64 = AdbBase64 { Base64.encodeToString(it, Base64.NO_WRAP) }

        return if (publicKey.exists() && privateKey.exists()) {
            AdbCrypto.loadAdbKeyPair(base64, privateKey, publicKey)
        } else {
            AdbCrypto.generateAdbKeyPair(base64).apply { saveAdbKeyPair(privateKey, publicKey) }
        }
    }

    companion object {
        private const val DEFAULT_HOST = "127.0.0.1"
        private const val DEFAULT_PORT = 5555

        private const val CONNECT_TIMEOUT_SEC = 15L
        private const val COMMAND_TIMEOUT_MS = 15_000L

        private const val PUBLIC_KEY_FILE = "pub.key"
        private const val PRIVATE_KEY_FILE = "priv.key"
    }
}

@SuppressLint("SdCardPath")
suspend fun AdbShell.captureScreen(saveDir: String = "/sdcard/Screenshots"): Boolean {
    execute("mkdir -p \"$saveDir\"")

    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS"))
    val filePath = "$saveDir/screenshot_$timestamp.png"

    execute("screencap -p \"$filePath\"")
    return execute("[ -s \"$filePath\" ] && echo success").contains("success")
}
