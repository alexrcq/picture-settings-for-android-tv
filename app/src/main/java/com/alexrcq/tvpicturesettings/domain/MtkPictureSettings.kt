package com.alexrcq.tvpicturesettings.domain

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
import com.alexrcq.tvpicturesettings.util.TvUtils.isLocalDimmingSupported
import com.alexrcq.tvpicturesettings.util.TvUtils.isOled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

open class MtkPictureSettings(context: Context, private val global: GlobalSettings) : PictureSettings {

    override var backlight: Int by global.intSetting(MtkGlobalKeys.PICTURE_BACKLIGHT)
    override var isHdrEnabled: Boolean by global.booleanSetting(MtkGlobalKeys.PICTURE_LIST_HDR)
    override var isColorTuneEnabled: Boolean by global.booleanSetting(MtkGlobalKeys.TV_PICTURE_COLOR_TUNE_ENABLE)
    override var isAutoBacklightEnabled: Boolean by global.booleanSetting(MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT)

    override var isLocalContrastEnabled: Boolean
        get() = global.getInt(MtkGlobalKeys.PICTURE_LOCAL_CONTRAST) == 2
        set(enabled) {
            val value = if (enabled) 2 else 0
            global.putInt(MtkGlobalKeys.PICTURE_LOCAL_CONTRAST, value)
        }

    override var isAdaptiveLumaEnabled: Boolean
        get() = global.getInt(MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL) == 2
        set(enabled) {
            val value = if (enabled) 2 else 0
            global.putInt(MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL, value)
        }

    override var pictureMode: Int
        get() = global.getInt(MtkGlobalKeys.PICTURE_MODE)
        set(pictureMode) {
            global.putInt(MtkGlobalKeys.PICTURE_MODE, pictureMode)
            setTemperatureByMode(pictureMode)
        }

    private val isDolbyVisionFlow: Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_NOTIFY_DOLBY_VISION) {
                    val isDolbyVision = intent.getBooleanExtra("DOLBY", false)
                    trySend(isDolbyVision)
                }
            }
        }
        send(false)
        context.registerReceiver(receiver, IntentFilter(ACTION_NOTIFY_DOLBY_VISION))
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    private val isAutoBacklightFlow: Flow<Boolean> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (uri?.lastPathSegment == MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT) {
                    trySend(isAutoBacklightEnabled)
                }
            }
        }
        send(isAutoBacklightEnabled)
        global.registerContentObserver(observer)
        awaitClose {
            global.unregisterContentObserver(observer)
        }
    }

    override val backlightAdjustAllowedFlow: StateFlow<Boolean> =
        combine(isAutoBacklightFlow, isDolbyVisionFlow) { isAutoBacklight, isDolbyVision ->
            !(isAutoBacklight || (isDolbyVision && !isLocalDimmingSupported() && !isOled(context)))
        }.flowOn(Dispatchers.IO)
            .stateIn(
                applicationScope,
                SharingStarted.Eagerly,
                initialValue = !global.getBoolean(MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT, false)
            )

    override fun setWhiteBalance(redGain: Int, greenGain: Int, blueGain: Int) = with(global) {
        putInt(MtkGlobalKeys.PICTURE_RED_GAIN, redGain)
        putInt(MtkGlobalKeys.PICTURE_GREEN_GAIN, greenGain)
        putInt(MtkGlobalKeys.PICTURE_BLUE_GAIN, blueGain)
    }

    override fun resetWhiteBalance() {
        setWhiteBalance(DEFAULT_COLOR_GAIN, DEFAULT_COLOR_GAIN, DEFAULT_COLOR_GAIN)
    }

    override fun resetToDefault() = with(global) {
        // same behaviour as the system app
        putInt(MtkGlobalKeys.PICTURE_RESET_TO_DEFAULT, getInt(MtkGlobalKeys.PICTURE_RESET_TO_DEFAULT) + 1)
        putInt(MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT, 0)
    }

    private fun setTemperatureByMode(pictureMode: Int) {
        val temperature: Int? = when (pictureMode) {
            PICTURE_MODE_DEFAULT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_BRIGHT -> PICTURE_TEMPERATURE_WARM
            PICTURE_MODE_SPORT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_MOVIE -> PICTURE_TEMPERATURE_COLD
            else -> null
        }
        if (temperature != null) {
            global.putInt(MtkGlobalKeys.PICTURE_TEMPERATURE, temperature)
        }
    }

    companion object {
        private const val DEFAULT_COLOR_GAIN = 1024
        private const val ACTION_NOTIFY_DOLBY_VISION =
            "com.android.tv.settings.partnercustomizer.tvsettingservice.NOTIFY_DOLBY_VISION"

        const val PICTURE_MODE_DEFAULT = 7
        const val PICTURE_MODE_BRIGHT = 3
        const val PICTURE_MODE_SPORT = 2
        const val PICTURE_MODE_MOVIE = 9
        const val PICTURE_MODE_USER = 0
        const val PICTURE_TEMPERATURE_WARM = 1
        const val PICTURE_TEMPERATURE_COLD = 2
        const val PICTURE_TEMPERATURE_DEFAULT = 3
    }
}