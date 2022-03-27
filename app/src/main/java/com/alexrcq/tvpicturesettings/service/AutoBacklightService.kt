package com.alexrcq.tvpicturesettings.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit
import com.alexrcq.tvpicturesettings.AppReceiver
import com.alexrcq.tvpicturesettings.storage.GlobalSettings
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.util.GlobalSettingsObserver
import java.time.LocalTime
import java.util.*

class AutoBacklightService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val binder = ServiceBinder()

    private var globalSettingsObserver: GlobalSettingsObserver? = null

    private lateinit var serviceLaunchScheduler: ServiceLaunchScheduler
    private lateinit var servicePrefs: Preferences
    private lateinit var pictureSettings: PictureSettings

    override fun onCreate() {
        pictureSettings = PictureSettings(this)
        serviceLaunchScheduler = ServiceLaunchScheduler()
        servicePrefs = Preferences(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        servicePrefs.registerOnSharedPreferenceChangeListener(this)
        globalSettingsObserver = GlobalSettingsObserver()
        globalSettingsObserver?.observe(contentResolver) { key ->
            if (key == GlobalSettings.KEY_PICTURE_BACKLIGHT) {
                onBacklightChanged()
            }
        }
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        if (intent.action == AppReceiver.ACTION_RECEIVER_LAUNCHED) {
            adjustBacklightDependingOnTimeOfDay()
            serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, servicePrefs.dayLaunchTime)
            serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, servicePrefs.nightLaunchTime)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        servicePrefs.unregisterOnSharedPreferenceChangeListener(this)
        globalSettingsObserver?.stopObserving()
        return true
    }

    private val isNightNow: Boolean
        get() {
            val currentTime = LocalTime.now()
            val sunsetTime = LocalTime.parse(servicePrefs.nightLaunchTime)
            val sunriseTime = LocalTime.parse(servicePrefs.dayLaunchTime)
            return currentTime >= sunsetTime || currentTime <= sunriseTime
        }

    private fun switchDarkFilter(enabled: Boolean) {
        if (enabled) {
            DarkFilterService.sharedInstance?.enableDarkFilter()
        } else {
            DarkFilterService.sharedInstance?.disableDarkFilter()
        }
    }

    private fun adjustBacklightDependingOnTimeOfDay() {
        if (!servicePrefs.isAutoBacklightEnabled) return
        pictureSettings.backlight = if (isNightNow) {
            servicePrefs.nightBacklight
        } else {
            servicePrefs.dayBacklight
        }
        switchDarkFilter(enabled = shouldEnableDarkFilter())
    }

    private fun shouldRememberCurrentBacklight(): Boolean =
        servicePrefs.isAutoBacklightEnabled && !isNightNow

    private fun rememberCurrentBacklight() {
        servicePrefs.dayBacklight = pictureSettings.backlight
    }

    private fun onBacklightChanged() {
        if (shouldRememberCurrentBacklight()) {
            rememberCurrentBacklight()
        }
    }

    private fun onAutoBacklightSwitched() {
        if (servicePrefs.isAutoBacklightEnabled) {
            if (!isNightNow) {
                rememberCurrentBacklight()
            }
            adjustBacklightDependingOnTimeOfDay()
            serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, servicePrefs.dayLaunchTime)
            serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, servicePrefs.nightLaunchTime)
        } else {
            if (servicePrefs.isDarkFilterEnabled) {
                DarkFilterService.sharedInstance?.disableDarkFilter()
            }
            pictureSettings.backlight = servicePrefs.dayBacklight
            serviceLaunchScheduler.cancelAllScheduledLaunches()
        }
    }

    private fun onNightBacklightChanged() {
        if (isNightNow) {
            pictureSettings.backlight = servicePrefs.nightBacklight
        }
    }

    private fun onDayLaunchTimeChanged() {
        adjustBacklightDependingOnTimeOfDay()
        serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, servicePrefs.dayLaunchTime)
    }

    private fun onNightLaunchTimeChanged() {
        adjustBacklightDependingOnTimeOfDay()
        serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, servicePrefs.nightLaunchTime)
    }

    private fun shouldEnableDarkFilter(): Boolean = servicePrefs.isDarkFilterEnabled && isNightNow

    fun enable() {
        servicePrefs.isAutoBacklightEnabled = true
    }

    fun disable() {
        servicePrefs.isAutoBacklightEnabled = false
    }

    fun enableDarkFilter() {
        servicePrefs.isDarkFilterEnabled = true
    }

    fun disableDarkFilter() {
        servicePrefs.isDarkFilterEnabled = false
    }

    fun setDaytimeLaunchTime(time: String) {
        servicePrefs.dayLaunchTime = time
    }

    fun setNighttimeLaunchTime(time: String) {
        servicePrefs.nightLaunchTime = time
    }

    fun setNightBacklight(value: Int) {
        servicePrefs.nightBacklight = value
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Preferences.Keys.IS_AUTO_BACKLIGHT_ENABLED -> {
                onAutoBacklightSwitched()
            }
            Preferences.Keys.IS_DARK_FILTER_ENABLED -> {
                switchDarkFilter(enabled = shouldEnableDarkFilter())
            }
            Preferences.Keys.DAY_LAUNCH_TIME -> {
                onDayLaunchTimeChanged()
            }
            Preferences.Keys.NIGHT_LAUNCH_TIME -> {
                onNightLaunchTimeChanged()
            }
            Preferences.Keys.NIGHT_BACKLIGHT -> {
                onNightBacklightChanged()
            }
        }
    }

    inner class ServiceBinder : Binder() {
        fun getService() = this@AutoBacklightService
    }

    private class Preferences(context: Context) {

        private var preferences = context.getSharedPreferences("autobacklight.prefs", MODE_PRIVATE)

        var nightBacklight: Int
            get() = preferences.getInt(Keys.NIGHT_BACKLIGHT, 0)
            set(value) {
                preferences.edit {
                    putInt(Keys.NIGHT_BACKLIGHT, value)
                }
            }

        var dayBacklight: Int
            get() = preferences.getInt(Keys.DAY_BACKLIGHT, 80)
            set(value) {
                preferences.edit {
                    putInt(Keys.DAY_BACKLIGHT, value)
                }
            }

        var isAutoBacklightEnabled: Boolean
            get() = preferences.getBoolean(Keys.IS_AUTO_BACKLIGHT_ENABLED, false)
            set(value) {
                preferences.edit {
                    putBoolean(Keys.IS_AUTO_BACKLIGHT_ENABLED, value)
                }
            }

        var isDarkFilterEnabled: Boolean
            get() = preferences.getBoolean(Keys.IS_DARK_FILTER_ENABLED, false)
            set(value) {
                preferences.edit {
                    putBoolean(Keys.IS_DARK_FILTER_ENABLED, value)
                }
            }

        var dayLaunchTime: String
            get() = preferences.getString(Keys.DAY_LAUNCH_TIME, "08:00")!!
            set(value) {
                preferences.edit {
                    putString(Keys.DAY_LAUNCH_TIME, value)
                }
            }

        var nightLaunchTime: String
            get() = preferences.getString(Keys.NIGHT_LAUNCH_TIME, "22:30")!!
            set(value) {
                preferences.edit {
                    putString(Keys.NIGHT_LAUNCH_TIME, value)
                }
            }

        fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            preferences.registerOnSharedPreferenceChangeListener(listener)
        }

        fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }

        object Keys {
            const val IS_AUTO_BACKLIGHT_ENABLED = "is_auto_backlight_enabled"
            const val DAY_LAUNCH_TIME = "day_launch_time"
            const val NIGHT_LAUNCH_TIME = "night_launch_time"
            const val IS_DARK_FILTER_ENABLED = "is_dark_filter_enabled"
            const val DAY_BACKLIGHT = "day_backlight"
            const val NIGHT_BACKLIGHT = "night_backlight"
        }
    }

    private inner class ServiceLaunchScheduler {

        private val alarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        fun setRepeatingLaunch(launchId: Int, time: String) {
            val serviceLaunchTime = LocalTime.parse(time)
            val serviceLaunchCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, serviceLaunchTime.hour)
                set(Calendar.MINUTE, serviceLaunchTime.minute)
                set(Calendar.SECOND, 0)
            }
            adjustServiceLaunchTime(serviceLaunchCalendar)
            Log.d(TAG, "scheduling the service launch at ${serviceLaunchCalendar.time}")
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                serviceLaunchCalendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                getPendingIntentFor(launchId)
            )
        }

        private fun adjustServiceLaunchTime(serviceLaunchTime: Calendar) {
            val now = Calendar.getInstance()
            if (serviceLaunchTime.before(now)) {
                serviceLaunchTime.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        private fun getPendingIntentFor(launchId: Int): PendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            launchId,
            getIntent(),
            PendingIntent.FLAG_IMMUTABLE
        )

        private fun getIntent(): Intent =
            Intent(applicationContext, AppReceiver::class.java).apply {
                action = ACTION_SCHEDULED_SERVICE_LAUNCH
            }

        fun cancelAllScheduledLaunches() {
            Log.d(TAG, "canceling the scheduled launches...")
            alarmManager.cancel(getPendingIntentFor(DAY_LAUNCH_ID))
            alarmManager.cancel(getPendingIntentFor(NIGHT_LAUNCH_ID))
        }
    }

    companion object {
        private const val TAG = "AutoBacklightService"
        private const val DAY_LAUNCH_ID = 6700
        private const val NIGHT_LAUNCH_ID = 6701

        const val ACTION_SCHEDULED_SERVICE_LAUNCH =
            "com.alerxrcq.tvpicturesettings.AutoBacklightService.ACTION_SCHEDULED_SERVICE_LAUNCH"
    }
}
