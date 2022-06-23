package com.alexrcq.tvpicturesettings.util

import android.Manifest
import com.alexrcq.tvpicturesettings.adblib.AndroidBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import java.net.Socket
import java.util.concurrent.Executors

object AdbUtils {
    fun grantWriteSecureSettingsPermission() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val connection = AdbConnection.create(
                Socket("127.0.0.1", 5555),
                AdbCrypto.generateAdbKeyPair(AndroidBase64())
            )
            connection?.connect()
            connection?.open("shell: pm grant com.alexrcq.tvpicturesettings ${Manifest.permission.WRITE_SECURE_SETTINGS}")
        }
    }
}