package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.BasePreferenceFragment

class ScreenScheduleFragment : BasePreferenceFragment(R.xml.screen_schedule_prefs) {

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            PreferencesKeys.SCREEN_SCHEDULE_MODE,
            PreferencesKeys.SCREEN_ON_TIME,
            PreferencesKeys.SCREEN_OFF_TIME -> {
                updateVisibility()
                (requireActivity().application as App).screenScheduler.updateSchedule()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(prefsListener)
        updateVisibility()
    }

    private fun updateVisibility() {
        val mode = preferenceManager.sharedPreferences?.getString(PreferencesKeys.SCREEN_SCHEDULE_MODE, "0") ?: "0"
        val isDaily = (mode == "1")
        val isWeekly = (mode == "2")

        findPreference<Preference>(PreferencesKeys.SCREEN_ON_TIME)?.isVisible = isDaily
        findPreference<Preference>(PreferencesKeys.SCREEN_OFF_TIME)?.isVisible = isDaily

        for (i in 1..7) {
            findPreference<Preference>("screen_schedule_day_$i")?.isVisible = isWeekly
        }
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(prefsListener)
        super.onDestroy()
    }
}