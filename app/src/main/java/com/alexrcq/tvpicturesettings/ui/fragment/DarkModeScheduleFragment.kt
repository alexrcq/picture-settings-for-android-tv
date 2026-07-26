package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.BasePreferenceFragment

class DarkModeScheduleFragment : BasePreferenceFragment(R.xml.dark_mode_schedule_prefs) {

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            PreferencesKeys.DARK_MODE_SCHEDULE_ENABLED,
            PreferencesKeys.DARK_MODE_TIME,
            PreferencesKeys.DAY_MODE_TIME -> {
                (requireActivity().application as App).darkModeScheduler.updateSchedule()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(prefsListener)
        super.onDestroy()
    }
}