package com.alexrcq.tvpicturesettings.storage

import android.content.Context
import android.util.Log
import com.alexrcq.tvpicturesettings.util.AdbUtils
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import java.io.IOException
import java.net.Socket
import java.util.concurrent.Executors

abstract class AdbGlobalSettings(val context: Context): GlobalSettingsImpl(context.contentResolver) {
    private var connection: AdbConnection? = null
    private val executor = Executors.newSingleThreadExecutor()
    init {
        var crypto: AdbCrypto? = AdbUtils.readCryptoConfig(context.filesDir)
        executor.execute {
            if (crypto == null) {
                crypto = AdbUtils.writeNewCryptoConfig(context.filesDir)
                if (crypto == null) {
                    Log.d("AdbGlobalSettings", "Key Pair Generation Failed")
                    return@execute
                }
                Log.d("AdbGlobalSettings", "New Key Pair Generated")
            }
            connection = AdbConnection.create(Socket("127.0.0.1", 5555), crypto)
            connection?.connect()
        }
    }

    override fun putInt(key: String, value: Int) {
        executor.execute {
            try {
                connection?.open("shell:settings put global $key $value")
            } catch (e: IOException) {
                // do nothing, wait for the user
            }
        }
    }
}