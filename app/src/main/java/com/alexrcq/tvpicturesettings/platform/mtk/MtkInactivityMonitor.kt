package com.alexrcq.tvpicturesettings.platform.mtk

import android.os.SystemClock
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.platform.InactivityMonitor
import com.alexrcq.tvpicturesettings.platform.android.SystemProperties
import kotlinx.coroutines.delay

class MtkInactivityMonitor: InactivityMonitor {

    override suspend fun startMonitoring(timeoutMinutes: Long, onTimeout: () -> Unit) {
        if (timeoutMinutes <= 0L) return

        val timeoutMs = timeoutMinutes * 60_000

        var lastHandledInputUptime = -1L

        while (true) {
            val lastInputUptime = SystemProperties.getLong(TvConstants.EVENT_UP_TIME_KEY)

            if (lastInputUptime > 0 && lastInputUptime != lastHandledInputUptime) {
                val idleDurationMs = SystemClock.uptimeMillis() - lastInputUptime
                if (idleDurationMs >= timeoutMs) {
                    onTimeout()
                    lastHandledInputUptime = lastInputUptime
                }
            }
            delay(1000)
        }
    }
}