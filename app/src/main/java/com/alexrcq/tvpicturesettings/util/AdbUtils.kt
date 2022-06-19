package com.alexrcq.tvpicturesettings.util

import com.alexrcq.tvpicturesettings.adblib.AndroidBase64
import com.tananaev.adblib.AdbCrypto
import java.io.Closeable
import java.io.File
import java.io.IOException

object AdbUtils {

    private const val PUBLIC_KEY_NAME = "public.key"
    private const val PRIVATE_KEY_NAME = "private.key"

    fun readCryptoConfig(dataDir: File?): AdbCrypto? {
        val publicKey = File(dataDir, PUBLIC_KEY_NAME)
        val privateKey = File(dataDir, PRIVATE_KEY_NAME)
        var crypto: AdbCrypto? = null
        if (publicKey.exists() && privateKey.exists()) {
            crypto = try {
                AdbCrypto.loadAdbKeyPair(AndroidBase64(), privateKey, publicKey)
            } catch (e: Exception) {
                null
            }
        }
        return crypto
    }

    fun writeNewCryptoConfig(dataDir: File?): AdbCrypto? {
        val publicKey = File(dataDir, PUBLIC_KEY_NAME)
        val privateKey = File(dataDir, PRIVATE_KEY_NAME)
        var crypto: AdbCrypto?
        try {
            crypto = AdbCrypto.generateAdbKeyPair(AndroidBase64())
            crypto.saveAdbKeyPair(privateKey, publicKey)
        } catch (e: Exception) {
            e.printStackTrace()
            crypto = null
        }
        return crypto
    }

    fun safeClose(c: Closeable?): Boolean {
        if (c == null) return false
        try {
            c.close()
        } catch (e: IOException) {
            return false
        }
        return true
    }

    fun safeAsyncClose(c: Closeable?) {
        if (c == null) return
        Thread {
            try {
                c.close()
            } catch (ignored: IOException) {
            }
        }.start()
    }
}