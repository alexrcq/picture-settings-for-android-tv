package com.alexrcq.tvpicturesettings.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.BroadcastActions
import com.alexrcq.tvpicturesettings.TvConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = (context.applicationContext as App)
        val darkModeManager = app.darkModeManager
        val darkModeScheduler = app.darkModeScheduler
        val screenSettings = app.screenSettings
        val screenScheduler = app.screenScheduler
        val applicationScope = app.applicationScope

        val pendingResult = goAsync()

        applicationScope.launch {
            delay(TvConstants.TV_SETTINGS_SERVICE_WARMUP_MS)

            when (intent.action) {
                BroadcastActions.ENABLE_DARK_MODE -> {
                    darkModeManager.enableDarkMode()
                    darkModeScheduler.updateSchedule()
                }
                BroadcastActions.DISABLE_DARK_MODE -> {
                    darkModeManager.disableDarkMode()
                    darkModeScheduler.updateSchedule()
                }
                BroadcastActions.TURN_ON_SCREEN -> {
                    screenSettings.isScreenOn = true
                    screenScheduler.updateSchedule()
                }
                BroadcastActions.TURN_OFF_SCREEN -> {
                    screenSettings.isScreenOn = false
                    screenScheduler.updateSchedule()
                }
            }

            pendingResult.finish()
        }
    }
}
