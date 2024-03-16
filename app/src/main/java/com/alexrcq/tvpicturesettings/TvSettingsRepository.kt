package com.alexrcq.tvpicturesettings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.alexrcq.tvpicturesettings.App.Companion.applicationScope
import com.alexrcq.tvpicturesettings.storage.GlobalSettings
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.storage.TvSettings
import com.alexrcq.tvpicturesettings.util.TvUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

private const val TV_SOURCE_CHECK_INTERVAL = 2500L

class TvSettingsRepository(private val context: Context, private val tvSettings: TvSettings) {

    val isTvSourceInactiveFlow: Flow<Boolean> = flow {
        while (true) {
            emit(tvSettings.isTvSourceInactive())
            delay(TV_SOURCE_CHECK_INTERVAL)
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    private val isDolbyVisionFlow: Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == TvConstants.ACTION_NOTIFY_DOLBY_VISION) {
                    val isDolbyVision = intent.getBooleanExtra("DOLBY", false)
                    trySend(isDolbyVision)
                }
            }
        }
        send(false)
        context.registerReceiver(receiver, IntentFilter(TvConstants.ACTION_NOTIFY_DOLBY_VISION))
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    private val isAutoBacklightFlow: Flow<Boolean> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (uri?.lastPathSegment == MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT) {
                    trySend(tvSettings.picture.isAutoBacklightEnabled)
                }
            }
        }
        send(tvSettings.picture.isAutoBacklightEnabled)
        tvSettings.global.registerContentObserver(observer)
        awaitClose {
            tvSettings.global.unregisterContentObserver(observer)
        }
    }

    val isBacklightAdjustAllowedStateFlow: StateFlow<Boolean> =
        combine(isAutoBacklightFlow, isDolbyVisionFlow) { isAutoBacklight, isDolbyVision ->
            !(isAutoBacklight || (isDolbyVision && !TvUtils.isLocalDimmingSupported() && !TvUtils.isOled(context)))
        }.flowOn(Dispatchers.IO).stateIn(applicationScope, SharingStarted.Eagerly, false)

    fun getGlobalSettings(): GlobalSettings = tvSettings.global

    fun getPictureSettings(): TvSettings.Picture = tvSettings.picture

    fun isBacklightAdjustAllowed(): Boolean = isBacklightAdjustAllowedStateFlow.value

    fun isAdbEnabled(): Boolean = tvSettings.isAdbEnabled()

    fun toggleScreenPower() {
        tvSettings.toggleScreenPower()
    }
}