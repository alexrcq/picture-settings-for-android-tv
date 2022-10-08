package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.DarkModeManager
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DARK_MODE_TIME
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DAY_MODE_TIME
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_AUTO_DARK_MODE_ENABLED

class ScheduledDarkModePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.scheduled_dark_mode_prefs, rootKey)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            IS_AUTO_DARK_MODE_ENABLED -> {
                DarkModeManager.requireInstance().setAutoDarkModeEnabled(newValue as Boolean)
            }
            DARK_MODE_TIME -> {
                DarkModeManager.requireInstance().setDarkModeTime(newValue as String)
            }
            DAY_MODE_TIME -> {
                DarkModeManager.requireInstance().setDayModeTime(newValue as String)
            }
        }
        return true
    }
}