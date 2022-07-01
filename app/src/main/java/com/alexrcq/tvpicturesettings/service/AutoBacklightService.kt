package com.alexrcq.tvpicturesettings.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.helper.AppReceiver
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.storage.appPreferences
import com.alexrcq.tvpicturesettings.util.Utils
import java.time.LocalTime
import java.util.*

class AutoBacklightService : Service(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var serviceLaunchScheduler: ServiceLaunchScheduler
    private lateinit var pictureSettings: PictureSettings

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
        if (appPreferences.isDarkFilterEnabled && !Utils.isDarkFilterServiceEnabled(this)) {
            enableDarkFilterService()
        }
        serviceLaunchScheduler = ServiceLaunchScheduler()
        pictureSettings = PictureSettings.getInstance(this)
        appPreferences.registerOnSharedPreferenceChangedListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SCHEDULED_SERVICE_LAUNCH) {
            with(appPreferences) {
                if (isAutoBacklightEnabled) {
                    isDarkModeActivated = isNightNow
                    serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, dayLaunchTime)
                    serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, nightLaunchTime)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppPreferences.Keys.IS_AUTO_BACKLIGHT_ENABLED -> {
                activateAutoBacklight(activated = appPreferences.isAutoBacklightEnabled)
            }
            AppPreferences.Keys.IS_DARK_MODE_ACTIVATED -> {
                activateDarkMode(activated = appPreferences.isDarkModeActivated)
            }
            AppPreferences.Keys.DAY_TIME -> {
                with(appPreferences) {
                    isDarkModeActivated = isNightNow
                    serviceLaunchScheduler.setRepeatingLaunch(
                        DAY_LAUNCH_ID,
                        dayLaunchTime
                    )
                }
            }
            AppPreferences.Keys.NIGHT_TIME -> {
                with(appPreferences) {
                    isDarkModeActivated = isNightNow
                    serviceLaunchScheduler.setRepeatingLaunch(
                        NIGHT_LAUNCH_ID,
                        nightLaunchTime
                    )
                }
            }
            AppPreferences.Keys.IS_DARK_FILTER_ENABLED -> {
                if (!Utils.isDarkFilterServiceEnabled(this)) {
                    enableDarkFilterService()
                }
                activateDarkFilter(
                    activated = appPreferences.isDarkModeActivated && appPreferences.isDarkFilterEnabled
                )
            }
            AppPreferences.Keys.NIGHT_BACKLIGHT -> {
                with(appPreferences) {
                    if (isDarkModeActivated) {
                        pictureSettings.backlight = nightBacklight
                    }
                }
            }
            AppPreferences.Keys.DARK_FILTER_POWER -> {
                DarkFilterService.sharedInstance?.setDarkFilterAlpha(appPreferences.darkFilterPower / 100f)
            }
        }
    }

    private fun enableDarkFilterService() {
        val allEnabledServices =
            Settings.Secure.getString(this.contentResolver, "enabled_accessibility_services")
        Settings.Secure.putString(
            this.contentResolver,
            "enabled_accessibility_services",
            "$allEnabledServices:${this.packageName}/${DarkFilterService::class.java.name}"
        )
    }

    private fun activateAutoBacklight(activated: Boolean) {
        with(appPreferences) {
            if (activated) {
                isDarkModeActivated = isNightNow
                if (!isDarkModeActivated) {
                    dayBacklight = pictureSettings.backlight
                }
                serviceLaunchScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, dayLaunchTime)
                serviceLaunchScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, nightLaunchTime)
            } else {
                isDarkModeActivated = false
                serviceLaunchScheduler.cancelAllScheduledLaunches()
            }
        }
    }

    private fun activateDarkMode(activated: Boolean) {
        with(appPreferences) {
            pictureSettings.backlight = if (activated) {
                nightBacklight
            } else {
                dayBacklight
            }
            activateDarkFilter(activated = activated && isDarkFilterEnabled)
        }
    }

    private fun activateDarkFilter(activated: Boolean) {
        if (activated) {
            DarkFilterService.sharedInstance?.enableDarkFilter()
            DarkFilterService.sharedInstance?.setDarkFilterAlpha(appPreferences.darkFilterPower / 100f)
        } else {
            DarkFilterService.sharedInstance?.disableDarkFilter()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        appPreferences.unregisterOnSharedPreferenceChangedListener(this)
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
            Log.d(TAG, "scheduling the service at ${serviceLaunchCalendar.time}")
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
            Intent(application, AppReceiver::class.java).apply {
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
        const val DAY_LAUNCH_ID = 6700
        const val NIGHT_LAUNCH_ID = 6701

        const val ACTION_SCHEDULED_SERVICE_LAUNCH =
            "com.alerxrcq.tvpicturesettings.AutoBacklightService.ACTION_SCHEDULED_SERVICE_LAUNCH"

        fun start(context: Context, isStartedFromBroadcast: Boolean) {
            val intent = Intent(context, AutoBacklightService::class.java)
            if (isStartedFromBroadcast) {
                intent.action = ACTION_SCHEDULED_SERVICE_LAUNCH
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(
                Intent(context, AutoBacklightService::class.java)
            )
        }
    }
}
