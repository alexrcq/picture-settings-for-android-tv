package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.util.AlarmScheduler

class ScheduledDarkModeFragment : BasePreferenceFragment(R.xml.scheduled_dark_mode_prefs),
    Preference.OnPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceScreen.forEach { preference -> preference.onPreferenceChangeListener = this }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            PreferencesKeys.IS_AUTO_DARK_MODE_ENABLED -> toggleAutoDarkMode(newValue as Boolean)
            PreferencesKeys.DARK_MODE_TIME -> scheduleDarkMode(newValue as String)
            PreferencesKeys.DAY_MODE_TIME -> scheduleDayMode(newValue as String)
        }
        return true
    }

    private fun scheduleDayMode(dayModeTime: String) {
        AlarmScheduler.setDailyAlarm(requireContext(), AlarmScheduler.AlarmType.DAY_MODE_ALARM, dayModeTime)
    }

    private fun scheduleDarkMode(darkModeTime: String) {
        AlarmScheduler.setDailyAlarm(requireContext(), AlarmScheduler.AlarmType.DARK_MODE_ALARM, darkModeTime)
    }

    private fun toggleAutoDarkMode(isAutoDarkModeEnabled: Boolean) {
        if (isAutoDarkModeEnabled) {
            val preferences = (requireActivity().application as App).darkModePreferences
            scheduleDayMode(preferences.dayModeTime)
            scheduleDarkMode(preferences.darkModeTime)
        } else {
            AlarmScheduler.cancelAlarm(requireContext(), AlarmScheduler.AlarmType.DAY_MODE_ALARM)
            AlarmScheduler.cancelAlarm(requireContext(), AlarmScheduler.AlarmType.DARK_MODE_ALARM)
        }
    }
}