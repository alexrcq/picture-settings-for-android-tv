package com.alexrcq.tvpicturesettings

import android.Manifest
import android.os.Environment
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val DEFAULT_SCREENSHOTS_FOLDER = "Screenshots"

class CaptureScreenUseCase(private val adbShell: AdbShell) {

    suspend operator fun invoke(savePath: String = "${Environment.getExternalStorageDirectory().path}/$DEFAULT_SCREENSHOTS_FOLDER"): Boolean =
        try {
            withContext(Dispatchers.IO) {
                adbShell.connect()
                adbShell.grantPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                adbShell.grantPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                adbShell.captureScreen(saveDir = prepareDir(savePath))
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Screen capture failed")
            false
        }
}
