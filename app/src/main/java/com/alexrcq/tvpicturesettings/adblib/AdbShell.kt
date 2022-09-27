package com.alexrcq.tvpicturesettings.adblib

import android.Manifest
import android.content.Context
import android.os.Environment
import android.os.FileObserver
import com.alexrcq.tvpicturesettings.BuildConfig
import com.alexrcq.tvpicturesettings.util.hasPermission
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import com.tananaev.adblib.AdbStream
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AdbShell private constructor(
    private val appContext: Context,
    private val host: String = "127.0.0.1",
    private val port: Int = 5555
) {
    private var adbConnection: AdbConnection? = null
    private var isConnected = false

    var screenshotsPath = Environment.getExternalStorageDirectory().path + "/Screenshots"

    suspend fun connect() = withContext(IO) {
        if (!isConnected) {
            adbConnection = createConnection()
            Timber.d("connecting...")
            val isConnectionEstablished = adbConnection?.connect(15L, TimeUnit.SECONDS, false)
            if (isConnectionEstablished == false) {
                throw TimeoutException()
            }
            isConnected = true
            Timber.d("connected")
        }
    }

    private suspend fun execute(command: String) = withContext(IO) {
        Timber.d("executing \"$command\"")
        openShell()?.write("$command\n")
    }

    suspend fun grantPermission(permission: String) {
        Timber.d("granting permission...")
        execute("pm grant ${BuildConfig.APPLICATION_ID} $permission")
        while (true) {
            delay(100)
            if (appContext.hasPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
                Timber.d("$permission granted")
                break
            }
        }
    }

    suspend fun takeScreenshot() {
        if (!appContext.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            grantPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            grantPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val screenshotsDir = File(screenshotsPath)
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs()
        }
        val currentTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS")
        )
        execute("screencap -p $screenshotsPath/screenshot$currentTime.png")
        waitForScreenshotCaptured()
    }

    private fun openShell(): AdbStream? {
        return adbConnection?.open("shell:")
    }

    private var fileObserver: FileObserver? = null

    private suspend fun waitForScreenshotCaptured() = suspendCoroutine { continuation ->
        fileObserver = object : FileObserver(screenshotsPath, CREATE) {
            override fun onEvent(event: Int, path: String?) {
                if (event == CREATE) {
                    continuation.resume(null)
                    stopWatching()
                }
            }
        }
        fileObserver?.startWatching()
    }

    private fun setupCrypto(pubKeyFile: String, privKeyFile: String): AdbCrypto? {
        val publicKey = File(pubKeyFile)
        val privateKey = File(privKeyFile)
        var crypto: AdbCrypto? = null
        if (publicKey.exists() && privateKey.exists()) {
            crypto = try {
                AdbCrypto.loadAdbKeyPair(AndroidBase64(), privateKey, publicKey)
            } catch (e: Exception) {
                null
            }
        }
        if (crypto == null) {
            crypto = AdbCrypto.generateAdbKeyPair(AndroidBase64())
            crypto.saveAdbKeyPair(privateKey, publicKey)
            Timber.d( "Generated new keypair")
        } else {
            Timber.d( "Loaded existing keypair")
        }
        return crypto
    }

    private fun createConnection(): AdbConnection {
        val path = appContext.cacheDir.absolutePath
        val crypto = setupCrypto("$path/pub.key", "$path/priv.key")
        val socket = Socket(host, port)
        return AdbConnection.create(socket, crypto)
    }

    fun disconnect() {
        if (isConnected) {
            adbConnection?.close()
            isConnected = false
            Timber.d( "disconnected")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AdbShell? = null

        @Synchronized
        fun getInstance(context: Context): AdbShell =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdbShell(context).also { INSTANCE = it }
            }
    }
}