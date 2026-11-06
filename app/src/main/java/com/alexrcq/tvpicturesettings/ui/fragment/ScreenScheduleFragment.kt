package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.manager.model.ScreenScheduleMode
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.BasePreferenceFragment

class ScreenScheduleFragment : BasePreferenceFragment(R.xml.screen_schedule_prefs) {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val modePreference = findPreference<ListPreference>(PreferencesKeys.SCREEN_SCHEDULE_MODE)
        updateVisibility(ScreenScheduleMode.of(modePreference?.value))
        modePreference?.setOnPreferenceChangeListener { _, newValue ->
            updateVisibility(ScreenScheduleMode.of(newValue as? String))
            true
        }
    }

    private fun updateVisibility(mode: ScreenScheduleMode) {
        val isDaily = (mode == ScreenScheduleMode.DAILY)
        val isWeekly = (mode == ScreenScheduleMode.WEEKLY)

        findPreference<Preference>(PreferencesKeys.SCREEN_ON_TIME)?.isVisible = isDaily
        findPreference<Preference>(PreferencesKeys.SCREEN_OFF_TIME)?.isVisible = isDaily

        for (i in 1..7) {
            findPreference<Preference>("screen_schedule_day_$i")?.isVisible = isWeekly
        }
    }
}
