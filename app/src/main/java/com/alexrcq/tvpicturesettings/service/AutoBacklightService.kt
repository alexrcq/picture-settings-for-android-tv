package com.alexrcq.tvpicturesettings.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.AutoBacklightService.Preferences.Keys.DAY_TIME
import com.alexrcq.tvpicturesettings.service.AutoBacklightService.Preferences.Keys.IS_AUTO_BACKLIGHT_ENABLED
import com.alexrcq.tvpicturesettings.service.AutoBacklightService.Preferences.Keys.NIGHT_TIME
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import timber.log.Timber
import java.time.LocalTime
import java.util.*

class AutoBacklightService : Service() {

    private lateinit var serviceLaunchScheduler: ServiceLaunchScheduler
    private lateinit var pictureSettings: PictureSettings
    private lateinit var preferences: Preferences

    private var binder = ServiceBinder()

    var isAutoBacklightEnabled: Boolean
        get() = preferences.isAutoBacklightEnabled
        set(enabled) {
            preferences.isAutoBacklightEnabled = enabled
            if (enabled) {
                val isDarkModeEnabled = preferences.isNightNow
                DarkModeManager.sharedInstance?.isDarkModeEnabled = isDarkModeEnabled
                if (!isDarkModeEnabled) {
                    DarkModeManager.sharedInstance?.dayBacklight = pictureSettings.backlight
                }
                serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, preferences.dayModeTime)
                serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, preferences.darkModeTime)
                return
            }
            DarkModeManager.sharedInstance?.isDarkModeEnabled = false
            serviceLaunchScheduler.cancelAllScheduledLaunches()
        }

    var dayModeTime: String
        get() = preferences.dayModeTime
        set(value) {
            preferences.dayModeTime = value
            DarkModeManager.sharedInstance?.isDarkModeEnabled = preferences.isNightNow
            serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, value)
        }

    var darkModeTime: String
        get() = preferences.darkModeTime
        set(value) {
            preferences.darkModeTime = value
            DarkModeManager.sharedInstance?.isDarkModeEnabled = preferences.isNightNow
            serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, value)
        }

    override fun onCreate() {
        super.onCreate()
        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannelCompat.Builder(
                "main",
                NotificationManagerCompat.IMPORTANCE_LOW
            ).setName(resources.getString(R.string.app_name)).build()
        )
        startForeground(
            4221,
            NotificationCompat.Builder(this, "main")
                .setContentTitle(resources.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_android)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        )
        serviceLaunchScheduler = ServiceLaunchScheduler()
        pictureSettings = PictureSettings(applicationContext)
        preferences = Preferences(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            with(DarkModeManager.requireInstance()) {
                if (isDayModeAfterScreenOnEnabled) {
                    isDarkModeEnabled = false
                }
            }
        }
        if (intent?.action == ACTION_SCHEDULED_SERVICE_LAUNCH) {
            if (isAutoBacklightEnabled) {
                DarkModeManager.requireInstance().isDarkModeEnabled = preferences.isNightNow
            }
        }
        serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, dayModeTime)
        serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, darkModeTime)
        return START_NOT_STICKY
    }

    private inner class ServiceLaunchScheduler {
        private val alarmManager =
            application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        fun setRepeatingLaunch(launchId: Int, time: String) {
            val serviceLaunchTime = LocalTime.parse(time)
            val serviceLaunchCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, serviceLaunchTime.hour)
                set(Calendar.MINUTE, serviceLaunchTime.minute)
                set(Calendar.SECOND, 0)
            }
            adjustServiceLaunchTime(serviceLaunchCalendar)
            Timber.d( "scheduling the service at ${serviceLaunchCalendar.time}")
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
            application,
            launchId,
            getIntent(),
            PendingIntent.FLAG_IMMUTABLE
        )

        private fun getIntent(): Intent =
            Intent(application, StartServiceBroadcastReceiver::class.java).apply {
                action = ACTION_SCHEDULED_SERVICE_LAUNCH
            }

        fun cancelAllScheduledLaunches() {
            alarmManager.cancel(getPendingIntentFor(DAY_LAUNCH_ID))
            alarmManager.cancel(getPendingIntentFor(NIGHT_LAUNCH_ID))
        }
    }

    class StartServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d( "onReceive, action: ${intent.action}")
            if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
                intent.action == ACTION_SCHEDULED_SERVICE_LAUNCH
            ) {
                start(context, intent)
            }
        }
    }

    class Preferences(context: Context) {
        private val preferences =
            context.applicationContext.getSharedPreferences("auto_backlight_prefs", MODE_PRIVATE)

        var isAutoBacklightEnabled: Boolean
            get() = preferences.getBoolean(IS_AUTO_BACKLIGHT_ENABLED, false)
            set(value) {
                preferences.edit {
                    putBoolean(IS_AUTO_BACKLIGHT_ENABLED, value)
                }
            }

        var dayModeTime: String
            get() = preferences.getString(DAY_TIME, "08:00")!!
            set(value) {
                preferences.edit {
                    putString(DAY_TIME, value)
                }
            }

        var darkModeTime: String
            get() = preferences.getString(NIGHT_TIME, "22:30")!!
            set(value) {
                preferences.edit {
                    putString(NIGHT_TIME, value)
                }
            }

        val isNightNow: Boolean
            get() {
                val currentTime = LocalTime.now()
                val sunsetTime = LocalTime.parse(darkModeTime)
                val sunriseTime = LocalTime.parse(dayModeTime)
                return currentTime >= sunsetTime || currentTime <= sunriseTime
            }

        object Keys {
            const val IS_AUTO_BACKLIGHT_ENABLED = "auto_backlight"
            const val DAY_TIME = "day_time"
            const val NIGHT_TIME = "night_time"
        }
    }

    inner class ServiceBinder : Binder() {
        fun getService(): AutoBacklightService = this@AutoBacklightService
    }

    companion object {
        const val DAY_LAUNCH_ID = 6700
        const val NIGHT_LAUNCH_ID = 6701

        const val ACTION_SCHEDULED_SERVICE_LAUNCH =
            "com.alerxrcq.tvpicturesettings.AutoBacklightService.ACTION_SCHEDULED_SERVICE_LAUNCH"

        fun start(context: Context, intent: Intent?) {
            context.startForegroundService(
                Intent(context, AutoBacklightService::class.java).apply {
                    action = intent?.action
                }
            )
        }
    }
}
