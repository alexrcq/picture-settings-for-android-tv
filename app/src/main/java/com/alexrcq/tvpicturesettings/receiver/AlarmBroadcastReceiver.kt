package com.alexrcq.tvpicturesettings.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.App.Companion.applicationScope
import com.alexrcq.tvpicturesettings.domain.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.util.AlarmScheduler
import com.alexrcq.tvpicturesettings.util.AlarmScheduler.ACTION_ALARM_TRIGGERED
import com.alexrcq.tvpicturesettings.util.AlarmScheduler.EXTRA_ALARM_TYPE
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class AlarmBroadcastReceiver : BroadcastReceiver() {

    private lateinit var darkModePreferences: DarkModePreferences
    private lateinit var darkModeManager: DarkModeManager

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("intent action: ${intent.action}")
        if (intent.action != ACTION_ALARM_TRIGGERED) {
            return
        }
        val application = (context.applicationContext as App)
        darkModePreferences = application.darkModePreferences
        darkModeManager = application.darkModeManager
        val alarmType = intent.getSerializableExtra(EXTRA_ALARM_TYPE) as AlarmScheduler.AlarmType?
        if (alarmType != null) {
            handleAlarm(context, alarmType)
        }
    }

    private fun handleAlarm(context: Context, alarmType: AlarmScheduler.AlarmType) {
        when (alarmType) {
            AlarmScheduler.AlarmType.DARK_MODE_ALARM -> onDarkModeAlarmTriggered(context)
            AlarmScheduler.AlarmType.DAY_MODE_ALARM -> onDayModeAlarmTriggered(context)
        }
    }

    private fun onDarkModeAlarmTriggered(context: Context) {
        applicationScope.launch {
            mutex.withLock {
                if (darkModePreferences.isAdditionalDimmingEnabled) {
                    darkModeManager.currentMode = DarkModeManager.Mode.FULL
                } else {
                    darkModeManager.currentMode = DarkModeManager.Mode.ONLY_BACKLIGHT
                }
            }
        }
        AlarmScheduler.setAlarm(context, AlarmScheduler.AlarmType.DARK_MODE_ALARM, darkModePreferences.darkModeTime)
    }

    private fun onDayModeAlarmTriggered(context: Context) {
        applicationScope.launch {
            mutex.withLock {
                // waiting for the system settings service wake up
                delay(DAY_MODE_DELAY)
                darkModeManager.currentMode = DarkModeManager.Mode.OFF
            }
        }
        AlarmScheduler.setAlarm(context, AlarmScheduler.AlarmType.DAY_MODE_ALARM, darkModePreferences.dayModeTime)
    }

    companion object {
        private const val DAY_MODE_DELAY = 2000L
        private val mutex = Mutex()
    }
}