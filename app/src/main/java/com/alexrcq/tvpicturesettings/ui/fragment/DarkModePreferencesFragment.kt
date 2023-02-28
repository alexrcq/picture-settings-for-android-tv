package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DARK_FILTER_POWER
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_DARK_FILTER_ENABLED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings

class DarkModePreferencesFragment : GlobalSettingsFragment(R.xml.dark_mode_prefs) {

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        super.onPreferenceChange(preference, newValue)
        when (preference.key) {
            IS_DARK_FILTER_ENABLED -> onDarkFilterPreferenceChange(newValue)
            NIGHT_BACKLIGHT -> onNightBacklightPreferenceChange(newValue)
            DARK_FILTER_POWER -> DarkModeManager.requireInstance().darkFilter.alpha =
                (newValue as Int) / 100f
        }
        return true
    }

    private fun onNightBacklightPreferenceChange(newValue: Any) {
        if (DarkModeManager.requireInstance().isDarkModeEnabled) {
            globalSettings.putInt(GlobalSettings.Keys.PICTURE_BACKLIGHT, newValue as Int)
        }
    }

    private fun onDarkFilterPreferenceChange(newValue: Any) {
        with(DarkModeManager.requireInstance()) {
            if (isDarkModeEnabled) {
                darkFilter.isEnabled = newValue as Boolean
            }
        }
    }
}