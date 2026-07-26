package com.alexrcq.tvpicturesettings.platform.mtk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Xml
import com.alexrcq.tvpicturesettings.platform.BacklightControlPolicy
import com.alexrcq.tvpicturesettings.platform.ScreenSettings
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.util.registerReceiverCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.xmlpull.v1.XmlPullParser
import java.io.File

class MtkBacklightControlPolicy(
    private val context: Context, private val screenSettings: ScreenSettings, scope: CoroutineScope
) : BacklightControlPolicy {

    private val isOled: Boolean = context.packageManager.hasSystemFeature("mitv.hardware.panel.oled")

    private val isLocalDimmingSupported: Deferred<Boolean> = scope.async(Dispatchers.IO) {
        queryTvConfigByKey("support_local_dimming") == "YES"
    }

    private val isDolbyVisionActive: Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_NOTIFY_DOLBY_VISION) {
                    trySend(intent.getBooleanExtra("DOLBY", false))
                }
            }
        }
        send(false)
        context.registerReceiverCompat(receiver, IntentFilter(ACTION_NOTIFY_DOLBY_VISION))
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    private val isAutoBacklightEnabled: Flow<Boolean> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (uri?.lastPathSegment == MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT) {
                    trySend(screenSettings.isAutoBacklightEnabled)
                }
            }
        }
        send(screenSettings.isAutoBacklightEnabled)
        context.contentResolver.registerContentObserver(Settings.Global.CONTENT_URI, true, observer)
        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    override val isAdjustmentAllowed: StateFlow<Boolean> = combine(
        isAutoBacklightEnabled, isDolbyVisionActive
    ) { isAutoBacklightEnabled, isDolbyVisionActive ->
        !(isAutoBacklightEnabled || (isDolbyVisionActive && !isLocalDimmingSupported.await() && !isOled))
    }.stateIn(scope, SharingStarted.Eagerly, !screenSettings.isAutoBacklightEnabled)

    companion object {
        private const val ACTION_NOTIFY_DOLBY_VISION =
            "com.android.tv.settings.partnercustomizer.tvsettingservice.NOTIFY_DOLBY_VISION"
    }
}

private fun queryTvConfigByKey(key: String): String? {
    val configLocations = listOf("/system/etc/mitv.config_product.xml", "/system/etc/mitv.config.xml")
    for (location in configLocations) {
        val configFile = File(location)
        if (!configFile.canRead()) continue
        try {
            configFile.inputStream().use { stream ->
                val parser = Xml.newPullParser()
                parser.setInput(stream, null)
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                        if (parser.getAttributeValue(null, "key") == key) {
                            return parser.getAttributeValue(null, "value")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }
    return null
}