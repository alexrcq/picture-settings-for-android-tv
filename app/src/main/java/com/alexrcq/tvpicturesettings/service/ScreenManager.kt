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
import androidx.core.app.NotificationCompat
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.domain.DarkModeManager
import com.alexrcq.tvpicturesettings.ui.OverlayManager
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.domain.PictureSettings
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.domain.TvSettings
import com.alexrcq.tvpicturesettings.ui.view.BlueLightFilterView
import com.alexrcq.tvpicturesettings.ui.view.ScreenDimmerView
import com.alexrcq.tvpicturesettings.util.AlarmScheduler
import com.alexrcq.tvpicturesettings.util.hasPermission
import com.alexrcq.tvpicturesettings.util.showToast
import timber.log.Timber

class ScreenManager : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var darkModePrefs: DarkModePreferences
    private lateinit var tvSettings: TvSettings
    private lateinit var screenDimmerView: ScreenDimmerView
    private lateinit var blueLightFilterView: BlueLightFilterView
    private lateinit var overlayManager: OverlayManager
    private lateinit var darkModeManager: DarkModeManager

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d(intent.toString())
            handleIntent(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val application = application as App
        tvSettings = application.tvSettings
        darkModePrefs = application.darkModePreferences
        darkModeManager = application.darkModeManager
        overlayManager = application.overlayManager
        screenDimmerView = ScreenDimmerView(this).apply {
            setPower(darkModePrefs.screenDimmingPower)
        }
        blueLightFilterView = BlueLightFilterView(this).apply {
            setPower(darkModePrefs.blueLightFilterPower)
            setTemperature(darkModePrefs.blueLightFilterTemp)
        }
        setupOverlay()
        setupPreferences()
        setupDarkModeAlarms()
        registerReceiver()
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
        darkModePrefs.unregisterOnSharedPreferenceChangeListener(this)
        overlayManager.destroyOverlay()
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

    private fun setupOverlay() {
        overlayManager.apply {
            if (hasPermission(android.Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                initOverlay()
            }
            updateViewAttachment(screenDimmerView, isAttachedToOverlay = darkModePrefs.isScreenDimmingEnabled)
            updateViewAttachment(blueLightFilterView, isAttachedToOverlay = darkModePrefs.isBlueLightFilterEnabled)
        }
    }

    private fun setupPreferences() {
        darkModePrefs.registerOnSharedPreferenceChangeListener(this)
        if (darkModePrefs.dayBacklight !in 0..PictureSettings.MAX_BACKLIGHT) {
            darkModePrefs.dayBacklight = tvSettings.picture.backlight
        }
    }

    private fun setupDarkModeAlarms() {
        if (darkModePrefs.isAutoDarkModeEnabled) {
            AlarmScheduler.setDailyAlarm(this, AlarmScheduler.AlarmType.DAY_MODE_ALARM, darkModePrefs.dayModeTime)
            AlarmScheduler.setDailyAlarm(this, AlarmScheduler.AlarmType.DARK_MODE_ALARM, darkModePrefs.darkModeTime)
        }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_TOGGLE_DARK_MODE)
            addAction(ACTION_ENABLE_DARK_MODE)
            addAction(ACTION_DISABLE_DARK_MODE)
            addAction(ACTION_TOGGLE_DIMMING)
            addAction(ACTION_ENABLE_DIMMING)
            addAction(ACTION_DISABLE_DIMMING)
            addAction(ACTION_CHANGE_DIMMING_POWER)
            addAction(ACTION_TOGGLE_SCREEN_POWER)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun onScreenOn() {
        if (darkModePrefs.turnOffDarkModeOnScreenOn) {
            darkModeManager.currentMode = DarkModeManager.Mode.OFF
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferencesKeys.CURRENT_MODE_NAME -> handleModeChanged(sharedPreferences, key)
            PreferencesKeys.IS_SCREEN_DIMMING_ENABLED -> handleScreenDimmingEnabled(sharedPreferences, key)
            PreferencesKeys.SCREEN_DIMMING_POWER -> handleScreenDimmingPower(sharedPreferences, key)
            PreferencesKeys.NIGHT_BACKLIGHT -> handleNightBacklight(sharedPreferences, key)
            PreferencesKeys.IS_BLUE_LIGHT_FILTER_ENABLED -> handleBlueLightFilterEnabled(sharedPreferences, key)
            PreferencesKeys.BLUE_LIGHT_FILTER_TEMPERATURE -> handleBlueLightFilterTemperature(sharedPreferences, key)
            PreferencesKeys.BLUE_LIGHT_FILTER_POWER -> handleBlueLightFilterPower(sharedPreferences, key)
            PreferencesKeys.IS_NIGHT_BLUE_FILTER_ENABLED -> handleNightBlueLightFilterEnabled(sharedPreferences, key)
            PreferencesKeys.IS_ADDITIONAL_DIMMING_ENABLED -> handleAdditionalDimmingEnabled(sharedPreferences, key)
        }
    }

    private fun handleModeChanged(sharedPreferences: SharedPreferences, key: String) {
        if (darkModePrefs.showModeChanged) {
            val mode = DarkModeManager.Mode.valueOf(sharedPreferences.getString(key, DarkModeManager.Mode.OFF.name)!!)
            showToast(getString(mode.message))
        }
    }

    private fun handleScreenDimmingEnabled(sharedPreferences: SharedPreferences, key: String) {
        val isDimmingEnabled = sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.is_screen_dimming_enabled))
        overlayManager.updateViewAttachment(screenDimmerView, isAttachedToOverlay = isDimmingEnabled)
    }

    private fun handleScreenDimmingPower(sharedPreferences: SharedPreferences, key: String) {
        screenDimmerView.setPower(sharedPreferences.getInt(key, resources.getInteger(R.integer.screen_dimming_power)))
    }

    private fun handleNightBacklight(sharedPreferences: SharedPreferences, key: String) {
        if (darkModeManager.currentMode != DarkModeManager.Mode.OFF && tvSettings.picture.isBacklightAdjustAllowed()) {
            tvSettings.picture.backlight =
                sharedPreferences.getInt(key, resources.getInteger(R.integer.night_backlight))
        }
    }

    private fun handleBlueLightFilterEnabled(sharedPreferences: SharedPreferences, key: String) {
        val isEnabled = sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.is_blue_light_filter_enabled))
        overlayManager.updateViewAttachment(blueLightFilterView, isAttachedToOverlay = isEnabled)
    }

    private fun handleBlueLightFilterTemperature(sharedPreferences: SharedPreferences, key: String) {
        blueLightFilterView.setTemperature(
            sharedPreferences.getInt(key, resources.getInteger(R.integer.blue_filter_temperature))
        )
    }

    private fun handleBlueLightFilterPower(sharedPreferences: SharedPreferences, key: String) {
        blueLightFilterView.setPower(
            sharedPreferences.getInt(key, resources.getInteger(R.integer.blue_light_filter_power))
        )
    }

    private fun handleNightBlueLightFilterEnabled(sharedPreferences: SharedPreferences, key: String) {
        if (darkModeManager.currentMode != DarkModeManager.Mode.OFF) {
            darkModePrefs.isBlueLightFilterEnabled =
                sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.is_night_blue_filter_enabled))
        }
    }

    private fun handleAdditionalDimmingEnabled(sharedPreferences: SharedPreferences, key: String) {
        if (darkModeManager.currentMode != DarkModeManager.Mode.OFF) {
            darkModePrefs.isScreenDimmingEnabled =
                sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.is_additional_dimming_enabled))
        }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_DARK_MODE -> darkModeManager.toggleMode()
            ACTION_ENABLE_DARK_MODE -> {
                if (darkModeManager.currentMode != DarkModeManager.Mode.OFF) return
                darkModeManager.currentMode = if (darkModePrefs.isAdditionalDimmingEnabled) {
                    DarkModeManager.Mode.FULL
                } else {
                    DarkModeManager.Mode.ONLY_BACKLIGHT
                }
            }
            ACTION_DISABLE_DARK_MODE -> darkModeManager.currentMode = DarkModeManager.Mode.OFF
            ACTION_TOGGLE_DIMMING -> darkModeManager.toggleScreenDimming()
            ACTION_ENABLE_DIMMING -> darkModePrefs.isScreenDimmingEnabled = true
            ACTION_DISABLE_DIMMING -> darkModePrefs.isScreenDimmingEnabled = false
            ACTION_CHANGE_DIMMING_POWER -> darkModePrefs.screenDimmingPower =
                intent.getIntExtra(EXTRA_DIMMING_POWER, 0)
            ACTION_TOGGLE_SCREEN_POWER -> tvSettings.toggleScreenPower()
            Intent.ACTION_SCREEN_ON -> onScreenOn()
        }
    }

    companion object : ServiceFactory() {
        private const val EXTRA_DIMMING_POWER = "filter_power"

        private const val ACTION_TOGGLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_DARK_MODE"
        private const val ACTION_ENABLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_DARK_MODE"
        private const val ACTION_DISABLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_DISABLE_DARK_MODE"

        private const val ACTION_TOGGLE_DIMMING = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_FILTER"
        private const val ACTION_ENABLE_DIMMING = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_FILTER"
        private const val ACTION_DISABLE_DIMMING = "com.alexrcq.tvpicturesettings.ACTION_DISABLE_FILTER"
        private const val ACTION_CHANGE_DIMMING_POWER = "ACTION_CHANGE_FILTER_POWER"

        private const val ACTION_TOGGLE_SCREEN_POWER = "ACTION_TOGGLE_SCREEN_POWER"

        private const val NOTIFICATION_TITLE = "Screen Manager"
        private const val NOTIFICATION_CONTENT = "Service is running"

        private const val NOTIFICATION_CHANNEL_ID = "channel_id"
        private const val NOTIFICATION_CHANNEL_NAME = "Screen Manager"

        private const val EXTRA_AFTER_BOOT = "after_boot"

        override fun getIntent(context: Context): Intent = Intent(context, ScreenManager::class.java)

        fun startForeground(context: Context, afterBoot: Boolean = false) {
            val intent = getIntent(context)
            if (afterBoot) intent.putExtra(EXTRA_AFTER_BOOT, true)
            context.startForegroundService(intent)
        }
    }
}