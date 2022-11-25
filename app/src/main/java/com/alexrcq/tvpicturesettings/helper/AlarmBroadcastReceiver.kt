package com.alexrcq.tvpicturesettings.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.helper.AlarmScheduler.Companion.ACTION_ALARM_TRIGGERED
import com.alexrcq.tvpicturesettings.helper.AlarmScheduler.Companion.EXTRA_ALARM_TYPE
import timber.log.Timber


class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("intent action: ${intent.action}")
        if (intent.action == ACTION_ALARM_TRIGGERED) {
            onAlarmTriggered(intent)
        }
    }

    private fun onAlarmTriggered(intent: Intent) {
        val alarmType =
            intent.getSerializableExtra(EXTRA_ALARM_TYPE) as AlarmScheduler.AlarmType?
        handleAlarm(alarmType)
    }

    private fun handleAlarm(alarmType: AlarmScheduler.AlarmType?) {
        Timber.d("handling the alarm: $alarmType")
        when (alarmType) {
            AlarmScheduler.AlarmType.DARK_MODE_ALARM -> {
                DarkModeManager.requireInstance().isDarkModeEnabled = true
            }
            AlarmScheduler.AlarmType.DAY_MODE_ALARM -> {
                DarkModeManager.requireInstance().isDarkModeEnabled = false
            }
            else -> Unit
        }
    }
}