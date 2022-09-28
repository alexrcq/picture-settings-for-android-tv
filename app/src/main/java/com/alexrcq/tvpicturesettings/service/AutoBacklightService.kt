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
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.storage.appPreferences
import timber.log.Timber
import java.time.LocalTime
import java.util.*

class AutoBacklightService : Service() {

    private lateinit var pictureSettings: PictureSettings
    private lateinit var serviceLaunchScheduler: ServiceLaunchScheduler

    private var binder = ServiceBinder()

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
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleAction(intent?.action)
        serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, appPreferences.dayTime)
        serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, appPreferences.nightTime)
        return START_NOT_STICKY
    }

    private fun handleAction(action: String?) {
        when(action) {
            Intent.ACTION_BOOT_COMPLETED -> onBootCompleted()
            ACTION_SCHEDULED_SERVICE_LAUNCH -> onScheduledServiceLaunch()
        }
    }

    private fun onBootCompleted() {
        with(appPreferences) {
            if (isDayModeAfterScreenOnEnabled) {
                DarkModeManager.requireInstance().isDarkModeEnabled = false
            }
        }
    }

    private fun onScheduledServiceLaunch() {
        with(appPreferences) {
            if (isAutoBacklightEnabled) {
                DarkModeManager.requireInstance().isDarkModeEnabled = appPreferences.isNightNow
            }
        }
    }

    fun handleAutoBacklight(isAutoBacklightEnabled: Boolean) {
        if (isAutoBacklightEnabled) {
            val isDarkModeEnabled = appPreferences.isNightNow
            DarkModeManager.requireInstance().isDarkModeEnabled = isDarkModeEnabled
            if (!isDarkModeEnabled) {
                appPreferences.dayBacklight = pictureSettings.backlight
            }
            serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, appPreferences.dayTime)
            serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, appPreferences.nightTime)
            return
        }
        DarkModeManager.requireInstance().isDarkModeEnabled = false
        serviceLaunchScheduler.cancelAllScheduledLaunches()
    }

    fun setDayModeTime(dayModeTime: String) {
        DarkModeManager.requireInstance().isDarkModeEnabled = appPreferences.isNightNow
        serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, dayModeTime)
    }

    fun setDarkModeTime(darkModeTime: String) {
        DarkModeManager.requireInstance().isDarkModeEnabled = appPreferences.isNightNow
        serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, darkModeTime)
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
