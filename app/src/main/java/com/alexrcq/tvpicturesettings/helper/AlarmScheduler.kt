package com.alexrcq.tvpicturesettings.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {

    private fun createAlarmIntent(): Intent {
        return Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
        }
    }

    fun setDailyAlarm(alarmType: AlarmType, alarmTime: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = createAlarmIntent().apply {
            putExtra(EXTRA_ALARM_TYPE, alarmType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmType.ordinal,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        var alarmDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(alarmTime))
        if (alarmDateTime.isBefore(LocalDateTime.now())) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelAlarm(alarmType: AlarmType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val operation = PendingIntent.getBroadcast(
            context,
            alarmType.ordinal,
            createAlarmIntent(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (operation != null) {
            alarmManager.cancel(operation)
        }
    }

    enum class AlarmType {
        DARK_MODE_ALARM, DAY_MODE_ALARM
    }

    companion object {
        const val EXTRA_ALARM_TYPE = "alarmType"
        const val ACTION_ALARM_TRIGGERED =
            "com.alerxrcq.tvpicturesettings.AlarmScheduler.ACTION_ALARM_TRIGGERED"
    }
}