package com.alexrcq.tvpicturesettings.platform.mtk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.os.SystemClock
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.platform.InactivityMonitor
import com.alexrcq.tvpicturesettings.platform.android.SystemProperties
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.util.registerReceiverCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest

class MtkInactivityMonitor(
    context: Context,
    scope: CoroutineScope,
    globalSettings: GlobalSettingsStore
) : InactivityMonitor {

    private val deviceAwakeFlow: Flow<Boolean> = callbackFlow {
        val powerManager = context.getSystemService(PowerManager::class.java)
        trySend(powerManager.isInteractive)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> trySend(true)
                    Intent.ACTION_SCREEN_OFF -> trySend(false)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.registerReceiverCompat(receiver, filter)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    private val screenOnFlow: Flow<Boolean> =
        globalSettings.getBooleanFlow(MtkGlobalKeys.POWER_PICTURE_OFF, true)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val idleDurationMs: Flow<Long> = combine(
        deviceAwakeFlow, screenOnFlow
    ) { isDeviceAwake, isScreenOn ->
        isDeviceAwake && isScreenOn
    }
        .distinctUntilChanged()
        .transformLatest { isScreenActive ->
            if (!isScreenActive) {
                emit(0L)
                return@transformLatest
            }
            val screenOnUptime = SystemClock.uptimeMillis()
            while (true) {
                val lastInput = SystemProperties.getLong(TvConstants.EVENT_UP_TIME_KEY)
                val lastActivity = maxOf(lastInput, screenOnUptime)
                emit((SystemClock.uptimeMillis() - lastActivity).coerceAtLeast(0L))
                delay(1000)
            }
        }
        .flowOn(Dispatchers.IO)
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
}
