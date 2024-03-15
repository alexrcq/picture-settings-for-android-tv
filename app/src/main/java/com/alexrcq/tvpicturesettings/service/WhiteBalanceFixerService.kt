package com.alexrcq.tvpicturesettings.service

import android.app.Notification
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
import androidx.core.app.NotificationCompat
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.TvSettingsRepository
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.storage.PicturePreferences

class WhiteBalanceFixerService : Service() {

    private lateinit var tvSettingsRepository: TvSettingsRepository
    private lateinit var preferences: PicturePreferences

    private val globalSettingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            when (uri?.lastPathSegment) {
                MtkGlobalKeys.PICTURE_RED_GAIN, MtkGlobalKeys.PICTURE_GREEN_GAIN, MtkGlobalKeys.PICTURE_BLUE_GAIN -> {
                    restorePictureGainIfFixed()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val application = application as App
        tvSettingsRepository = application.tvSettingsRepository
        preferences = application.picturePreferences
        restorePictureGainIfFixed()
        tvSettingsRepository.getGlobalSettings().registerContentObserver(globalSettingsObserver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(1, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        tvSettingsRepository.getGlobalSettings().unregisterContentObserver(globalSettingsObserver)
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_CONTENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

    private fun restorePictureGainIfFixed() {
        if (preferences.isWhiteBalanceFixed) {
            tvSettingsRepository.getPictureSettings().setWhiteBalance(
                redGain = preferences.redGain,
                greenGain = preferences.greenGain,
                blueGain = preferences.blueGain
            )
        }
    }

    companion object : ServiceFactory() {
        private const val NOTIFICATION_TITLE = "White Balance Fixer"
        private const val NOTIFICATION_CONTENT = "Service is running"

        private const val NOTIFICATION_CHANNEL_ID = "channel_id"
        private const val NOTIFICATION_CHANNEL_NAME = "White Balance Fixer"
        override fun getIntent(context: Context): Intent = Intent(context, WhiteBalanceFixerService::class.java)
    }
}