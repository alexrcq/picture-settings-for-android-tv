package com.alexrcq.tvpicturesettings.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.IBinder
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.TvSettingsRepository
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.storage.TvSettings
import com.alexrcq.tvpicturesettings.util.AlarmScheduler
import com.alexrcq.tvpicturesettings.util.ServiceUtils
import com.alexrcq.tvpicturesettings.util.showToast
import timber.log.Timber

class DarkModeManager : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var preferences: DarkModePreferences
    private lateinit var tvSettingsRepository: TvSettingsRepository

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d(intent.toString())
            handleIntent(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val application = application as App
        tvSettingsRepository = application.tvSettingsRepository
        preferences = application.darkModePreferences
        preferences.registerOnSharedPreferenceChangeListener(this)
        if (preferences.dayBacklight !in 0..TvSettings.Picture.MAX_BACKLIGHT) {
            preferences.dayBacklight = tvSettingsRepository.getPictureSettings().backlight
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
        unregisterReceiver(broadcastReceiver)
        preferences.unregisterOnSharedPreferenceChangeListener(this)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferencesKeys.CURRENT_MODE_NAME -> {
                val modeName = sharedPreferences.getString(PreferencesKeys.CURRENT_MODE_NAME, Mode.OFF.name)!!
                val mode = DarkModeManager.Mode.valueOf(modeName)
                if (tvSettingsRepository.isBacklightAdjustAllowed()) {
                    applyMode(mode)
                    showToast(getString(mode.message))
                } else {
                    showToast(getString(R.string.mode_cannot_be_applied))
                }
            }
            PreferencesKeys.IS_SCREEN_FILTER_ENABLED -> {
                ensureScreenFilterServiceRunning()
                val enabled = sharedPreferences.getBoolean(PreferencesKeys.IS_SCREEN_FILTER_ENABLED, false)
                ScreenFilterService.sharedInstance?.screenFilter?.setEnabled(enabled)
            }
            PreferencesKeys.SCREEN_FILTER_POWER -> {
                ensureScreenFilterServiceRunning()
                val power = sharedPreferences.getInt(PreferencesKeys.SCREEN_FILTER_POWER, 0)
                ScreenFilterService.sharedInstance?.screenFilter?.setPower(power)
            }
            PreferencesKeys.NIGHT_BACKLIGHT -> {
                if (preferences.currentMode != Mode.OFF && tvSettingsRepository.isBacklightAdjustAllowed()) {
                    val nightBacklight = sharedPreferences.getInt(PreferencesKeys.NIGHT_BACKLIGHT, 0)
                    tvSettingsRepository.getPictureSettings().backlight = nightBacklight
                }
            }
        }
    }

    private fun applyMode(mode: Mode) {
        when (mode) {
            Mode.OFF -> {
                tvSettingsRepository.getPictureSettings().backlight = preferences.dayBacklight
                preferences.isScreenFilterEnabled = false
            }
            Mode.ONLY_BACKLIGHT -> {
                tvSettingsRepository.getPictureSettings().backlight = preferences.nightBacklight
                preferences.isScreenFilterEnabled = false
            }
            Mode.FULL -> {
                tvSettingsRepository.getPictureSettings().backlight = preferences.nightBacklight
                preferences.isScreenFilterEnabled = true
            }
        }
    }

    private fun onScreenOn() {
        if (preferences.turnOffDarkModeOnScreenOn) {
            preferences.currentMode = Mode.OFF
        }
    }

    private fun handleIntent(intent: Intent) = with(preferences) {
        when (intent.action) {
            ACTION_TOGGLE_DARK_MODE -> toggleMode()
            ACTION_ENABLE_DARK_MODE -> {
                if (currentMode != Mode.OFF) return
                currentMode = if (isAdditionalDimmingEnabled) Mode.FULL else Mode.ONLY_BACKLIGHT
            }
            ACTION_DISABLE_DARK_MODE -> currentMode = Mode.OFF
            ACTION_TOGGLE_FILTER -> toggleFilter()
            ACTION_ENABLE_FILTER -> isScreenFilterEnabled = true
            ACTION_DISABLE_FILTER -> isScreenFilterEnabled = false
            ACTION_CHANGE_FILTER_POWER -> screenFilterPower = intent.getIntExtra(EXTRA_FILTER_POWER, 0)
            ACTION_TOGGLE_SCREEN_POWER -> tvSettingsRepository.toggleScreenPower()
            Intent.ACTION_SCREEN_ON -> onScreenOn()
        }
    }

    private fun ensureScreenFilterServiceRunning() {
        if (!ScreenFilterService.isServiceConnected()) {
            ServiceUtils.ensureAccessibilityServiceEnabled(this, ScreenFilterService::class.java)
        }
    }

    enum class Mode(@StringRes val message: Int) {
        OFF(R.string.dark_mode_off), ONLY_BACKLIGHT(R.string.dark_mode_only_backlight), FULL(R.string.dark_mode_full)
    }

    companion object : ServiceFactory() {
        private const val EXTRA_FILTER_POWER = "filter_power"

        private const val ACTION_TOGGLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_DARK_MODE"
        private const val ACTION_ENABLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_DARK_MODE"
        private const val ACTION_DISABLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_DISABLE_DARK_MODE"

        private const val ACTION_TOGGLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_FILTER"
        private const val ACTION_ENABLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_FILTER"
        private const val ACTION_DISABLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_DISABLE_FILTER"
        private const val ACTION_CHANGE_FILTER_POWER = "ACTION_CHANGE_FILTER_POWER"

        private const val ACTION_TOGGLE_SCREEN_POWER = "ACTION_TOGGLE_SCREEN_POWER"

        private const val NOTIFICATION_TITLE = "Dark Mode Manager"
        private const val NOTIFICATION_CONTENT = "Service is running"

        private const val NOTIFICATION_CHANNEL_ID = "channel_id"
        private const val NOTIFICATION_CHANNEL_NAME = "Dark Mode Manager"

        private const val EXTRA_AFTER_BOOT = "after_boot"

        override fun getIntent(context: Context): Intent = Intent(context, DarkModeManager::class.java)

        fun startForeground(context: Context, afterBoot: Boolean = false) {
            val intent = getIntent(context)
            if (afterBoot) intent.putExtra(EXTRA_AFTER_BOOT, true)
            context.startForegroundService(intent)
        }
    }
}