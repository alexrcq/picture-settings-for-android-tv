package com.alexrcq.tvpicturesettings.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alexrcq.tvpicturesettings.receiver.AlarmBroadcastReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {
    const val EXTRA_ALARM_TYPE = "alarmType"
    const val ACTION_ALARM_TRIGGERED = "com.alerxrcq.tvpicturesettings.AlarmScheduler.ACTION_ALARM_TRIGGERED"

    fun setAlarm(context: Context, alarmType: AlarmType, alarmTime: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmType.ordinal,
            createIntent(context, alarmType),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        var alarmDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(alarmTime))
        if (alarmDateTime.isBefore(LocalDateTime.now())) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context, alarmType: AlarmType) {
        val operation = PendingIntent.getBroadcast(
            context,
            alarmType.ordinal,
            createIntent(context, alarmType),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (operation != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(operation)
        }
    }

    private fun createIntent(context: Context, alarmType: AlarmType): Intent =
        Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
            putExtra(EXTRA_ALARM_TYPE, alarmType)
        }

    enum class AlarmType {
        DARK_MODE_ALARM, DAY_MODE_ALARM
    }
}