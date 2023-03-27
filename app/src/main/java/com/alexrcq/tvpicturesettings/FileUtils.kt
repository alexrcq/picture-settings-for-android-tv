package com.alexrcq.tvpicturesettings

import android.os.FileObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

object FileUtils {

    fun prepareFolder(fullPath: String): File {
        val directory = File(fullPath)
        if (directory.exists()) {
            return directory
        }
        directory.mkdirs()
        return directory
    }

    suspend fun File.waitForFileEvent(mask: Int) = suspendCancellableCoroutine { continuation ->
        @Suppress("DEPRECATION")
        val fileObserver = object : FileObserver(this.path, mask) {
            override fun onEvent(event: Int, path: String?) {
                if (event == CREATE) {
                    continuation.resume(Unit)
                    stopWatching()
                }
            }
        }
        fileObserver.startWatching()
        continuation.invokeOnCancellation {
            fileObserver.stopWatching()
        }
    }
}
