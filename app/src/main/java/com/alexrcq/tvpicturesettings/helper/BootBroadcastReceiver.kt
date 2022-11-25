package com.alexrcq.tvpicturesettings.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.storage.AppPreferences

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            onBootCompleted(context)
        }
    }

    private fun onBootCompleted(context: Context) {
        setupDarkMode(AppPreferences(context), AlarmScheduler(context))
    }

    private fun setupDarkMode(appPreferences: AppPreferences, alarmScheduler: AlarmScheduler) {
        if (appPreferences.isDayModeAfterScreenOnEnabled) {
            DarkModeManager.requireInstance().isDarkModeEnabled = false
        }
        if (appPreferences.isAutoDarkModeEnabled) {
            alarmScheduler.apply {
                setDailyAlarm(AlarmScheduler.AlarmType.DAY_MODE_ALARM, appPreferences.dayModeTime)
                setDailyAlarm(AlarmScheduler.AlarmType.DARK_MODE_ALARM, appPreferences.darkModeTime)
            }
        }
    }
}