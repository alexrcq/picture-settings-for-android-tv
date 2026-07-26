package com.alexrcq.tvpicturesettings.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.platform.mtk.MtkVideoSettings
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore
import com.alexrcq.tvpicturesettings.storage.VideoPreferences

class MtkWhiteBalanceLocker : Service() {

    private lateinit var globalSettings: GlobalSettingsStore
    private lateinit var videoPreferences: VideoPreferences

    private val colorGainObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri?.lastPathSegment?.let { key -> enforceLockedValue(key) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        videoPreferences = (application as App).videoPreferences
        globalSettings = GlobalSettingsStore(contentResolver)
        startForeground()
        if (videoPreferences.isWhiteBalanceLocked) {
            startLocking()
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(colorGainObserver)
        super.onDestroy()
    }

    private fun startLocking() {
        MtkVideoSettings.COLOR_GAIN_KEYS.forEach { key ->
            enforceLockedValue(key)
            contentResolver.registerContentObserver(
                Settings.Global.getUriFor(key),
                false,
                colorGainObserver
            )
        }
    }

    private fun enforceLockedValue(key: String) {
        val lockedValue = videoPreferences.get(key, MtkVideoSettings.DEFAULT_COLOR_GAIN)
        globalSettings.putInt(key, lockedValue)
    }

    private fun startForeground() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_CONTENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    companion object : ServiceFactory() {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_TITLE = "White Balance Locker"
        private const val NOTIFICATION_CONTENT = "Service is running"
        private const val NOTIFICATION_CHANNEL_ID = "white_balance_locker_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "White Balance Locker"

        override fun getIntent(context: Context): Intent = Intent(context, MtkWhiteBalanceLocker::class.java)
    }
}