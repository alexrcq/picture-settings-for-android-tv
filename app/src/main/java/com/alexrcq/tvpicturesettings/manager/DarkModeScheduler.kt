package com.alexrcq.tvpicturesettings.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.BroadcastActions
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.receiver.AlarmBroadcastReceiver
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.util.nextOccurrence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class DarkModeScheduler(
    private val context: Context,
    private val preferences: DarkModePreferences,
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

        if (!preferences.isDarkModeScheduleEnabled) return

        val darkModeTime = preferences.darkModeTime
        val dayModeTime = preferences.dayModeTime

        if (darkModeTime == dayModeTime) return

        val now = LocalDateTime.now()

        val nextDarkModeTime = darkModeTime.nextOccurrence(now)
        val nextDayModeTime = dayModeTime.nextOccurrence(now)

        val (nextActionTime, action) = if (nextDarkModeTime.isBefore(nextDayModeTime)) {
            nextDarkModeTime to BroadcastActions.ENABLE_DARK_MODE
        } else {
            nextDayModeTime to BroadcastActions.DISABLE_DARK_MODE
        }

        schedule(nextActionTime, action)
    }

    private fun schedule(dateTime: LocalDateTime, action: String) {
        val triggerTime = dateTime.minus(TvConstants.TV_SETTINGS_SERVICE_WARMUP_MS, ChronoUnit.MILLIS)
        val triggerAtMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMillis, getPendingIntent(action, PendingIntent.FLAG_UPDATE_CURRENT)
            )
        } catch (e: SecurityException) {
            // ignore
        }
    }

    private fun clearSchedule() {
        listOf(BroadcastActions.ENABLE_DARK_MODE, BroadcastActions.DISABLE_DARK_MODE).forEach { action ->
            val pendingIntent = getPendingIntent(action, PendingIntent.FLAG_NO_CREATE)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun getPendingIntent(action: String, flags: Int): PendingIntent? = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        Intent(context, AlarmBroadcastReceiver::class.java).apply { this.action = action },
        flags or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        private const val ALARM_REQUEST_CODE = 101
    }
}