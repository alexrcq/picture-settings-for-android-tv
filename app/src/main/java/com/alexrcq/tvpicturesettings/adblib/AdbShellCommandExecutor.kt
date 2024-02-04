package com.alexrcq.tvpicturesettings.adblib

import android.content.Context
import android.os.FileObserver.CREATE
import com.alexrcq.tvpicturesettings.BuildConfig
import com.alexrcq.tvpicturesettings.util.hasPermission
import com.alexrcq.tvpicturesettings.util.waitForFileEvent
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private const val CONNECTION_TIMEOUT_SECONDS = 25L
private const val PERMISSION_CHECK_INTERVAL = 50L
private const val ENSURE_PERMISSION_GRANTED_DELAY = 200L

class AdbShellCommandExecutor(private val context: Context) : AdbClient {

    private var adbConnection: AdbConnection? = null
    private var isConnected = false

    override suspend fun connect(host: String, port: Int) = withContext(IO) {
        if (!isConnected) {
            adbConnection = createConnection(host, port)
            Timber.d("connecting...")
            val isConnectionEstablished = adbConnection?.connect(
                CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS, false
            )
            if (isConnectionEstablished == false) {
                throw TimeoutException("adb connection timeout")
            }
            isConnected = true
            Timber.d("connected")
        }
    }

    override suspend fun execute(command: String): Unit = withContext(IO) {
        if (!isConnected) connect()
        Timber.d(command)
        adbConnection?.open("shell:")?.write("$command\n")
    }

    override suspend fun grantPermission(permission: String) {
        if (context.hasPermission(permission)) return
        execute("pm grant ${BuildConfig.APPLICATION_ID} $permission")
        while (true) {
            delay(PERMISSION_CHECK_INTERVAL)
            if (context.hasPermission(permission)) {
                // the permission is actually not granted yet, waiting
                delay(ENSURE_PERMISSION_GRANTED_DELAY)
                Timber.d("$permission granted")
                break
            }
        }
    }

    override suspend fun captureScreen(saveDir: File) = withContext(IO) {
        val currentTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS")
        )
        val waitForFileCreationJob = launch(start = CoroutineStart.UNDISPATCHED) {
            saveDir.waitForFileEvent(CREATE)
        }
        execute("screencap -p ${saveDir.path}/screenshot$currentTime.png")
        waitForFileCreationJob.join()
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
            Timber.d("Generated new keypair")
        } else {
            Timber.d("Loaded existing keypair")
        }
        return crypto
    }

    private fun createConnection(host: String, port: Int): AdbConnection {
        val path = context.cacheDir.absolutePath
        val crypto = setupCrypto("$path/pub.key", "$path/priv.key")
        val socket = Socket(host, port)
        return AdbConnection.create(socket, crypto)
    }

    override fun disconnect() {
        if (isConnected) {
            adbConnection?.close()
            isConnected = false
            Timber.d("disconnected")
        }
    }
}