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
        val appPreferences = AppPreferences(context)
        if (appPreferences.isDayModeAfterScreenOnEnabled) {
            DarkModeManager.requireInstance().isDarkModeEnabled = false
        }
        if (appPreferences.isAutoDarkModeEnabled) {
            AlarmScheduler(context).apply {
                setDailyAlarm(AlarmScheduler.AlarmType.DAY_MODE_ALARM, appPreferences.dayModeTime)
                setDailyAlarm(AlarmScheduler.AlarmType.DARK_MODE_ALARM, appPreferences.darkModeTime)
            }
        }
    }
}