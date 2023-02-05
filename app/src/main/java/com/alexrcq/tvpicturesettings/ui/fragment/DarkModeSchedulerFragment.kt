package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.helper.AlarmScheduler
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DARK_MODE_TIME
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DAY_MODE_TIME
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_AUTO_DARK_MODE_ENABLED

class DarkModeSchedulerFragment : BasePreferenceFragment(R.xml.scheduled_dark_mode_prefs) {

    private lateinit var alarmScheduler: AlarmScheduler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alarmScheduler = AlarmScheduler(requireContext())
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            IS_AUTO_DARK_MODE_ENABLED -> setupAutoDarkMode(isAutoDarkModeEnabled = newValue as Boolean)
            DARK_MODE_TIME -> scheduleDarkMode(darkModeTime = newValue as String)
            DAY_MODE_TIME -> scheduleDayMode(dayModeTime = newValue as String)
        }
        return true
    }

    private fun scheduleDayMode(dayModeTime: String) {
        alarmScheduler.setDailyAlarm(AlarmScheduler.AlarmType.DAY_MODE_ALARM, dayModeTime)
    }

    private fun scheduleDarkMode(darkModeTime: String) {
        alarmScheduler.setDailyAlarm(AlarmScheduler.AlarmType.DARK_MODE_ALARM, darkModeTime)
    }

    private fun setupAutoDarkMode(isAutoDarkModeEnabled: Boolean) {
        if (!isAutoDarkModeEnabled) {
            alarmScheduler.cancelAlarm(AlarmScheduler.AlarmType.DAY_MODE_ALARM)
            alarmScheduler.cancelAlarm(AlarmScheduler.AlarmType.DARK_MODE_ALARM)
            return
        }
        with(AppPreferences(requireContext())) {
            scheduleDayMode(dayModeTime)
            scheduleDarkMode(darkModeTime)
        }
    }
}