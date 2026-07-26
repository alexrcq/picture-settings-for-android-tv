package com.alexrcq.tvpicturesettings.platform.adb

import android.annotation.SuppressLint

interface AdbShell {
    suspend fun connect()

    suspend fun execute(command: String): String

    suspend fun grantPermissions(permissions: List<String>)

    @SuppressLint("SdCardPath")
    suspend fun captureScreen(saveDir: String = "/sdcard/Screenshots"): Boolean

    fun disconnect()
}