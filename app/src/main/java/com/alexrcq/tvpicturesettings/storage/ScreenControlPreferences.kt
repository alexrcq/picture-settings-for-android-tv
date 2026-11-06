package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences
import android.content.res.Resources
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.manager.model.ScreenScheduleMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class ScreenControlPreferences(sharedPreferences: SharedPreferences, private val resources: Resources) :
    SharedPreferencesStore(sharedPreferences) {

    val screenOffTimeoutMinutes = preferenceFlow(
        PreferencesKeys.SCREEN_OFF_TIMEOUT, resources.getString(R.string.def_screen_off_timeout)
    ).map { it.toLongOrNull() ?: 0L }

    var screenScheduleMode: ScreenScheduleMode
        get() = ScreenScheduleMode.of(
            get(PreferencesKeys.SCREEN_SCHEDULE_MODE, ScreenScheduleMode.OFF.value)
        )
        set(value) {
            put(PreferencesKeys.SCREEN_SCHEDULE_MODE, value.value)
        }

    var screenOnTime: LocalTime
        get() = LocalTime.parse(get(PreferencesKeys.SCREEN_ON_TIME, resources.getString(R.string.def_screen_on_time)))
        set(value) {
            put(PreferencesKeys.SCREEN_ON_TIME, value.toString())
        }

    var screenOffTime: LocalTime
        get() = LocalTime.parse(get(PreferencesKeys.SCREEN_OFF_TIME, resources.getString(R.string.def_screen_off_time)))
        set(value) {
            put(PreferencesKeys.SCREEN_OFF_TIME, value.toString())
        }

    fun isDayEnabled(day: Int): Boolean = get("screen_schedule_day_${day}_enabled", false)

    fun getDayOnTime(day: Int): LocalTime =
        LocalTime.parse(get("screen_schedule_day_${day}_on", resources.getString(R.string.def_screen_on_time)))

    fun getDayOffTime(day: Int): LocalTime =
        LocalTime.parse(get("screen_schedule_day_${day}_off", resources.getString(R.string.def_screen_off_time)))

    val scheduleChangesFlow: Flow<Unit> = changesFlow { key ->
        key == PreferencesKeys.SCREEN_SCHEDULE_MODE ||
            key == PreferencesKeys.SCREEN_ON_TIME ||
            key == PreferencesKeys.SCREEN_OFF_TIME ||
            key.startsWith("screen_schedule_day_")
    }
}
