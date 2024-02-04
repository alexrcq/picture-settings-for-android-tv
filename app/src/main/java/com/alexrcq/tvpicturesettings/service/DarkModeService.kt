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
import com.alexrcq.tvpicturesettings.App.Companion.applicationScope
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.TvSettings
import com.alexrcq.tvpicturesettings.util.AlarmScheduler
import com.alexrcq.tvpicturesettings.util.showToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import timber.log.Timber

class DarkModeService : Service() {

    private lateinit var darkModeManager: DarkModeManager
    private lateinit var preferences: DarkModePreferences
    private lateinit var tvSettings: TvSettings

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d(intent.toString())
            handleIntent(intent)
        }
    }

    private var showModeChangedJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val application = application as App
        darkModeManager = application.darkModeManager
        preferences = application.darkModePreferences
        tvSettings = application.tvSettings
        showModeChangedJob = applicationScope.launch {
            preferences.modeFlow.drop(1).collect { currentMode ->
                showToast(getString(currentMode.message))
            }
        }
        if (preferences.isAutoDarkModeEnabled) {
            AlarmScheduler.setDailyAlarm(this, AlarmScheduler.AlarmType.DAY_MODE_ALARM, preferences.dayModeTime)
            AlarmScheduler.setDailyAlarm(this, AlarmScheduler.AlarmType.DARK_MODE_ALARM, preferences.darkModeTime)
        }
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_TOGGLE_DARK_MODE)
            addAction(ACTION_ENABLE_DARK_MODE)
            addAction(ACTION_DISABLE_DARK_MODE)
            addAction(ACTION_TOGGLE_FILTER)
            addAction(ACTION_ENABLE_FILTER)
            addAction(ACTION_DISABLE_FILTER)
            addAction(ACTION_CHANGE_FILTER_POWER)
            addAction(ACTION_TOGGLE_SCREEN_POWER)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(EXTRA_AFTER_BOOT, false) == true) {
            onScreenOn()
        }
        createNotificationChannel()
        startForeground(2, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        showModeChangedJob?.cancel()
        unregisterReceiver(broadcastReceiver)
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

    private fun onScreenOn() {
        if (preferences.turnOffDarkModeOnScreenOn) {
            darkModeManager.setMode(DarkModeManager.Mode.OFF)
        }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_DARK_MODE -> darkModeManager.toggleMode()
            ACTION_ENABLE_DARK_MODE -> {
                if (darkModeManager.currentMode != DarkModeManager.Mode.OFF) return
                if (preferences.isAdditionalDimmingEnabled) {
                    darkModeManager.setMode(DarkModeManager.Mode.FULL)
                } else {
                    darkModeManager.setMode(DarkModeManager.Mode.ONLY_BACKLIGHT)
                }
            }
            ACTION_DISABLE_DARK_MODE -> darkModeManager.setMode(DarkModeManager.Mode.OFF)
            ACTION_TOGGLE_FILTER -> darkModeManager.toggleScreenFilter()
            ACTION_ENABLE_FILTER -> darkModeManager.setScreenFilterEnabled(true)
            ACTION_DISABLE_FILTER -> darkModeManager.setScreenFilterEnabled(false)
            ACTION_CHANGE_FILTER_POWER -> {
                val filterPower = intent.getIntExtra(EXTRA_FILTER_POWER, 0).coerceAtMost(MAX_FILTER_POWER)
                darkModeManager.setScreenFilterPower(filterPower)
            }
            ACTION_TOGGLE_SCREEN_POWER -> tvSettings.toggleScreenPower()
            Intent.ACTION_SCREEN_ON -> onScreenOn()
        }
    }

    companion object : ServiceFactory() {
        private const val EXTRA_FILTER_POWER = "filter_power"
        private const val MAX_FILTER_POWER = 98

        private const val ACTION_TOGGLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_DARK_MODE"
        private const val ACTION_ENABLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_DARK_MODE"
        private const val ACTION_DISABLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_DISABLE_DARK_MODE"

        private const val ACTION_TOGGLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_FILTER"
        private const val ACTION_ENABLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_FILTER"
        private const val ACTION_DISABLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_DISABLE_FILTER"
        private const val ACTION_CHANGE_FILTER_POWER = "ACTION_CHANGE_FILTER_POWER"

        private const val ACTION_TOGGLE_SCREEN_POWER = "ACTION_TOGGLE_SCREEN_POWER"

        private const val NOTIFICATION_TITLE = "Dark Mode Service"
        private const val NOTIFICATION_CONTENT = "Service is running"

        private const val NOTIFICATION_CHANNEL_ID = "channel_id"
        private const val NOTIFICATION_CHANNEL_NAME = "Dark Mode Service"

        private const val EXTRA_AFTER_BOOT = "after_boot"

        override fun getIntent(context: Context): Intent = Intent(context, DarkModeService::class.java)

        fun startForeground(context: Context, afterBoot: Boolean = false) {
            val intent = getIntent(context)
            if (afterBoot) intent.putExtra(EXTRA_AFTER_BOOT, true)
            context.startForegroundService(intent)
        }
    }
}