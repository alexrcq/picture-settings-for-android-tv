package com.alexrcq.tvpicturesettings.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.helper.AlarmScheduler
import com.alexrcq.tvpicturesettings.service.PictureSettingsService
import com.alexrcq.tvpicturesettings.helper.AppSettings

class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (val action = intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                onSystemActionOccurred(context, action)
            }
        }
    }

    private fun onSystemActionOccurred(context: Context, action: String) {
        PictureSettingsService.start(context, afterBoot = action == Intent.ACTION_BOOT_COMPLETED)
        initAutoDarkMode(context)
    }

    private fun initAutoDarkMode(context: Context) {
        val appSettings = AppSettings(context)
        if (appSettings.isAutoDarkModeEnabled) {
            AlarmScheduler(context).apply {
                setDailyAlarm(
                    AlarmScheduler.AlarmType.DAY_MODE_ALARM, appSettings.dayModeTime
                )
                setDailyAlarm(
                    AlarmScheduler.AlarmType.DARK_MODE_ALARM, appSettings.darkModeTime
                )
            }
        }
    }
}