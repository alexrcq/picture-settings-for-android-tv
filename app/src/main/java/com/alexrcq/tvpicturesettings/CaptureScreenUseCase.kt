package com.alexrcq.tvpicturesettings

import android.Manifest
import android.os.Environment
import com.alexrcq.tvpicturesettings.adblib.AdbClient
import timber.log.Timber
import java.io.File

private const val DEFAULT_SCREENSHOTS_PATH = "/Screenshots"

class CaptureScreenUseCase(private val adb: AdbClient) {
    suspend operator fun invoke(savePath: String = getDefaultPath()): Boolean = try {
        adb.grantPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        adb.grantPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        adb.captureScreen(saveDir = createDirectoryIfNotExists(savePath))
        true
    } catch (e: Exception) {
        Timber.e(e, "Screen capture failed")
        false
    }

    private fun getDefaultPath(): String = "${Environment.getExternalStorageDirectory().path}$DEFAULT_SCREENSHOTS_PATH"
}

private fun createDirectoryIfNotExists(path: String): File {
    val directory = File(path)
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory
}