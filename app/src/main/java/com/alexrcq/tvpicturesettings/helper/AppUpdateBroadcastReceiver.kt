package com.alexrcq.tvpicturesettings.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import timber.log.Timber

class AppUpdateBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.e(intent.action)
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            onApplicationUpdated(context)
        }
    }

    private fun onApplicationUpdated(context: Context) {
        val appPreferences = AppPreferences(context)
        if (appPreferences.isAutoDarkModeEnabled) {
            AlarmScheduler(context).apply {
                setDailyAlarm(AlarmScheduler.AlarmType.DAY_MODE_ALARM, appPreferences.dayModeTime)
                setDailyAlarm(AlarmScheduler.AlarmType.DARK_MODE_ALARM, appPreferences.darkModeTime)
            }
        }
    }
}