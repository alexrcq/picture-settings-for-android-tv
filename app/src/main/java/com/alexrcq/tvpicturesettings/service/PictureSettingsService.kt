package com.alexrcq.tvpicturesettings.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ensureAccessibilityServiceEnabled
import com.alexrcq.tvpicturesettings.helper.SettingsObserver
import com.alexrcq.tvpicturesettings.receiver.UserActionBroadcastReceiver
import com.alexrcq.tvpicturesettings.receiver.UserActionBroadcastReceiver.Companion.ACTION_DISABLE_DARK_MODE
import com.alexrcq.tvpicturesettings.receiver.UserActionBroadcastReceiver.Companion.ACTION_DISABLE_FILTER
import com.alexrcq.tvpicturesettings.receiver.UserActionBroadcastReceiver.Companion.ACTION_ENABLE_DARK_MODE
import com.alexrcq.tvpicturesettings.receiver.UserActionBroadcastReceiver.Companion.ACTION_ENABLE_FILTER
import com.alexrcq.tvpicturesettings.receiver.UserActionBroadcastReceiver.Companion.ACTION_TOGGLE_DARK_MODE
import com.alexrcq.tvpicturesettings.receiver.UserActionBroadcastReceiver.Companion.ACTION_TOGGLE_FILTER
import com.alexrcq.tvpicturesettings.helper.WhiteBalanceHelper
import com.alexrcq.tvpicturesettings.showActivationToast
import com.alexrcq.tvpicturesettings.helper.AppSettings
import com.alexrcq.tvpicturesettings.helper.GlobalSettings

class PictureSettingsService : Service() {

    private lateinit var appSettings: AppSettings
    private lateinit var globalSettings: GlobalSettings
    private lateinit var settingsObserver: SettingsObserver

    private val userActionReceiver = UserActionBroadcastReceiver(::handleUserAction)

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                onScreenOn()
            }
        }
    }

    override fun onCreate() {
        val application = (application as App)
        globalSettings = application.globalSettings
        appSettings = application.appSettings
        initWhiteBalance()
        settingsObserver = SettingsObserver(appSettings, globalSettings, ::onSettingChanged)
        settingsObserver.observe()
        registerReceiver(screenOnReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        registerReceiver(userActionReceiver, userActionReceiver.intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(EXTRA_AFTER_BOOT, false) == true) {
            onScreenOn()
        }
        createNotificationChannel()
        startForeground(1, createNotification())
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_CONTENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun initWhiteBalance() {
        if (appSettings.isWhiteBalanceFixed) {
            WhiteBalanceHelper(this).setWhiteBalance(
                redGain = appSettings.redGain,
                greenGain = appSettings.greenGain,
                blueGain = appSettings.blueGain
            )
        }
    }

    private fun handleUserAction(action: String?) {
        when (action) {
            ACTION_TOGGLE_DARK_MODE -> appSettings.toggleDarkMode()
            ACTION_ENABLE_DARK_MODE -> appSettings.isDarkModeEnabled = true
            ACTION_DISABLE_DARK_MODE -> appSettings.isDarkModeEnabled = false
            ACTION_TOGGLE_FILTER -> appSettings.toggleDarkFilter()
            ACTION_ENABLE_FILTER -> appSettings.isDarkFilterEnabled = true
            ACTION_DISABLE_FILTER -> appSettings.isDarkFilterEnabled = false
        }
    }

    private fun onSettingChanged(key: String) {
        when (key) {
            AppSettings.Keys.IS_DARK_MODE_ENABLED -> onDarkModeEnabled(appSettings.isDarkModeEnabled)
            AppSettings.Keys.IS_DARK_FILTER_ENABLED -> onDarkFilterEnabled(appSettings.isDarkFilterEnabled)
            GlobalSettings.Keys.PICTURE_RED_GAIN,
            GlobalSettings.Keys.PICTURE_GREEN_GAIN,
            GlobalSettings.Keys.PICTURE_BLUE_GAIN -> onPictureGainChanged(key)
        }
    }

    private fun onDarkModeEnabled(isDarkModeEnabled: Boolean) {
        showActivationToast(
            isActivated = isDarkModeEnabled,
            activationMessage = R.string.dark_mode_activated,
            deactivationMessage = R.string.dark_mode_deactivated
        )
    }

    private fun onDarkFilterEnabled(isDarkFilterEnabled: Boolean) {
        if (DarkFilterService.sharedInstance == null && isDarkFilterEnabled) {
            ensureAccessibilityServiceEnabled(DarkFilterService::class.java)
        }
    }

    private fun onPictureGainChanged(key: String) {
        if (appSettings.isWhiteBalanceFixed) {
            globalSettings.putInt(key, appSettings.preferences.getInt(key, 1024))
        }
    }

    private fun onScreenOn() {
        with(appSettings) {
            if (dayModeOnScreenOn) {
                isDarkModeEnabled = false
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        settingsObserver.stopObserving()
    }

    companion object {
        private const val NOTIFICATION_TITLE = "Picture Settings Service"
        private const val NOTIFICATION_CONTENT = "Service is running"

        private const val NOTIFICATION_CHANNEL_ID = "channel_id"
        private const val NOTIFICATION_CHANNEL_NAME = "Picture Settings Service"

        private const val EXTRA_AFTER_BOOT = "after_boot"

        fun start(context: Context, afterBoot: Boolean = false) {
            val intent = Intent(context, PictureSettingsService::class.java).apply {
                if (afterBoot) {
                    putExtra(EXTRA_AFTER_BOOT, true)
                }
            }
            context.startForegroundService(intent)
        }
    }
}