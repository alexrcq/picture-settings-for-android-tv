package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences
import android.content.res.Resources
import com.alexrcq.tvpicturesettings.R
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class ScreenControlPreferences(sharedPreferences: SharedPreferences, resources: Resources) :
    SharedPreferencesStore(sharedPreferences) {

    val screenOffTimeoutMinutes = preferenceFlow(
        PreferencesKeys.SCREEN_OFF_TIMEOUT, resources.getString(R.string.def_screen_off_timeout)
    ).map { it.toLongOrNull() ?: 0L }

    var screenScheduleMode: String by preference(PreferencesKeys.SCREEN_SCHEDULE_MODE, "0")

    var screenOnTime: LocalTime
        get() = LocalTime.parse(get(PreferencesKeys.SCREEN_ON_TIME, "08:00"))
        set(value) {
            put(PreferencesKeys.SCREEN_ON_TIME, value.toString())
        }

    var screenOffTime: LocalTime
        get() = LocalTime.parse(get(PreferencesKeys.SCREEN_OFF_TIME, "23:00"))
        set(value) {
            put(PreferencesKeys.SCREEN_OFF_TIME, value.toString())
        }

    fun isDayEnabled(day: Int): Boolean = get("screen_schedule_day_${day}_enabled", false)

    fun getDayOnTime(day: Int): LocalTime = LocalTime.parse(get("screen_schedule_day_${day}_on", "08:00"))

    fun getDayOffTime(day: Int): LocalTime = LocalTime.parse(get("screen_schedule_day_${day}_off", "23:00"))
}
