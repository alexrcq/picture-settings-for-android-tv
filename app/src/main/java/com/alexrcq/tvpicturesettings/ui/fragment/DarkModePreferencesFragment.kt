package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ensureAccessibilityServiceEnabled
import com.alexrcq.tvpicturesettings.service.DarkFilterService
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.DARK_FILTER_POWER
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.IS_ADDITIONAL_DIMMING_ENABLED
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.helper.GlobalSettings

class DarkModePreferencesFragment : GlobalSettingsFragment(R.xml.dark_mode_prefs) {

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        super.onPreferenceChange(preference, newValue)
        when (preference.key) {
            IS_ADDITIONAL_DIMMING_ENABLED -> onAdditionalDimmingChange(newValue as Boolean)
            NIGHT_BACKLIGHT -> onNightBacklightChange(newValue as Int)
            DARK_FILTER_POWER -> onDarkFilterPowerChange(newValue as Int)
        }
        return true
    }

    private fun onDarkFilterPowerChange(newValue: Int) {
        if (DarkFilterService.sharedInstance == null) {
            requireContext().ensureAccessibilityServiceEnabled(DarkFilterService::class.java)
        }
        DarkFilterService.sharedInstance?.darkFilter?.alpha = newValue / 100f
    }

    private fun onNightBacklightChange(newValue: Int) {
        if (appSettings.isDarkModeEnabled) {
            globalSettings.putInt(GlobalSettings.Keys.PICTURE_BACKLIGHT, newValue)
        }
    }

    private fun onAdditionalDimmingChange(isEnabled: Boolean) {
        with(appSettings) {
            if (isDarkModeEnabled) {
                isDarkFilterEnabled = isEnabled
            }
        }
    }
}