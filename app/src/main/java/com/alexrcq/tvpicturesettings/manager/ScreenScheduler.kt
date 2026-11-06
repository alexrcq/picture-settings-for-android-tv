package com.alexrcq.tvpicturesettings.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.BroadcastActions
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.manager.model.ScreenScheduleMode
import com.alexrcq.tvpicturesettings.receiver.AlarmBroadcastReceiver
import com.alexrcq.tvpicturesettings.storage.ScreenControlPreferences
import com.alexrcq.tvpicturesettings.util.nextOccurrence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class ScreenScheduler(
    private val context: Context,
    private val preferences: ScreenControlPreferences,
    scope: CoroutineScope
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    init {
        preferences.scheduleChangesFlow
            .onEach { updateSchedule() }
            .launchIn(scope)
    }

    fun updateSchedule() {
        clearSchedule()

        val now = LocalDateTime.now()
        var nextOnTime: LocalDateTime? = null
        var nextOffTime: LocalDateTime? = null

        when (preferences.screenScheduleMode) {
            ScreenScheduleMode.OFF -> return
            ScreenScheduleMode.DAILY -> {
                val onTime = preferences.screenOnTime
                val offTime = preferences.screenOffTime
                if (onTime != offTime) {
                    nextOnTime = onTime.nextOccurrence(now)
                    nextOffTime = offTime.nextOccurrence(now)
                }
            }
            ScreenScheduleMode.WEEKLY -> {
                for (day in 1..7) {
                    if (preferences.isDayEnabled(day)) {
                        val onTime = preferences.getDayOnTime(day)
                        val offTime = preferences.getDayOffTime(day)
                        if (onTime != offTime) {
                            val candOn = onTime.nextOccurrence(now, day)
                            val candOff = offTime.nextOccurrence(now, day)

                            if (nextOnTime == null || candOn.isBefore(nextOnTime)) nextOnTime = candOn
                            if (nextOffTime == null || candOff.isBefore(nextOffTime)) nextOffTime = candOff
                        }
                    }
                }
            }
        }

        nextOnTime?.let { schedule(it, BroadcastActions.TURN_ON_SCREEN, ALARM_REQUEST_CODE_ON) }
        nextOffTime?.let { schedule(it, BroadcastActions.TURN_OFF_SCREEN, ALARM_REQUEST_CODE_OFF) }
    }

    private fun schedule(dateTime: LocalDateTime, action: String, requestCode: Int) {
        val triggerTime = dateTime.minus(TvConstants.TV_SETTINGS_SERVICE_WARMUP_MS, ChronoUnit.MILLIS)
        val triggerAtMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMillis, getPendingIntent(action, requestCode, PendingIntent.FLAG_UPDATE_CURRENT)
            )
        } catch (e: SecurityException) {
            // ignore
        }
    }

    private fun clearSchedule() {
        listOf(
            BroadcastActions.TURN_ON_SCREEN to ALARM_REQUEST_CODE_ON,
            BroadcastActions.TURN_OFF_SCREEN to ALARM_REQUEST_CODE_OFF
        ).forEach { (action, requestCode) ->
            val pendingIntent = getPendingIntent(action, requestCode, PendingIntent.FLAG_NO_CREATE)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun getPendingIntent(action: String, requestCode: Int, flags: Int): PendingIntent? = PendingIntent.getBroadcast(
        context,
        requestCode,
        Intent(context, AlarmBroadcastReceiver::class.java).apply { this.action = action },
        flags or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        private const val ALARM_REQUEST_CODE_ON = 201
        private const val ALARM_REQUEST_CODE_OFF = 202
    }
}