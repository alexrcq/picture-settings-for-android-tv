package com.alexrcq.tvpicturesettings.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alexrcq.tvpicturesettings.service.DarkFilterService
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import java.time.LocalTime
import java.util.*

class AutoBacklightManager(val context: Context) {

    private val managerWorkScheduler = ManagerWorkScheduler()
    private val pictureSettings = PictureSettings.getInstance(context)
    private val appPreferences = AppPreferences(context)

    fun rescheduleWork() {
        if (!appPreferences.isAutoBacklightEnabled) return
        adjustBacklightDependingOnTimeOfDay()
        managerWorkScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, appPreferences.dayLaunchTime)
        managerWorkScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, appPreferences.nightLaunchTime)
    }

    fun switchDarkFilter(enabled: Boolean) {
        if (enabled) {
            DarkFilterService.sharedInstance?.enableDarkFilter()
        } else {
            DarkFilterService.sharedInstance?.disableDarkFilter()
        }
    }

    private fun adjustBacklightDependingOnTimeOfDay() {
        pictureSettings.backlight = if (appPreferences.isNightNow) {
            appPreferences.nightBacklight
        } else {
            appPreferences.dayBacklight
        }
        switchDarkFilter(enabled = (appPreferences.isDarkFilterEnabled && appPreferences.isNightNow))
    }

    fun switchAutoBacklight(enabled: Boolean) {
        if (enabled) {
            if (!appPreferences.isNightNow) {
                appPreferences.dayBacklight = pictureSettings.backlight
            }
            adjustBacklightDependingOnTimeOfDay()
            managerWorkScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, appPreferences.dayLaunchTime)
            managerWorkScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, appPreferences.nightLaunchTime)
        } else {
            if (appPreferences.isDarkFilterEnabled) {
                DarkFilterService.sharedInstance?.disableDarkFilter()
            }
            pictureSettings.backlight = appPreferences.dayBacklight
            managerWorkScheduler.cancelAllScheduledLaunches()
        }
    }

    fun setDaytimeManagerLaunchTime(time: String) {
        adjustBacklightDependingOnTimeOfDay()
        managerWorkScheduler.setRepeatingLaunch(DAY_LAUNCH_ID, time)
    }

    fun setNighttimeLaunchTime(time: String) {
        adjustBacklightDependingOnTimeOfDay()
        managerWorkScheduler.setRepeatingLaunch(NIGHT_LAUNCH_ID, time)
    }

    private inner class ManagerWorkScheduler {

        private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
            context,
            launchId,
            getIntent(),
            PendingIntent.FLAG_IMMUTABLE
        )

        private fun getIntent(): Intent =
            Intent(context, AppReceiver::class.java).apply {
                action = ACTION_SCHEDULED_MANAGER_LAUNCH
            }

        fun cancelAllScheduledLaunches() {
            Log.d(TAG, "canceling the scheduled launches...")
            alarmManager.cancel(getPendingIntentFor(DAY_LAUNCH_ID))
            alarmManager.cancel(getPendingIntentFor(NIGHT_LAUNCH_ID))
        }
    }

    companion object {
        private const val TAG = "AutoBacklightManager"
        const val DAY_LAUNCH_ID = 6700
        const val NIGHT_LAUNCH_ID = 6701

        const val ACTION_SCHEDULED_MANAGER_LAUNCH =
            "com.alerxrcq.tvpicturesettings.AutoBacklightManager.ACTION_SCHEDULED_MANAGER_LAUNCH"
    }
}
