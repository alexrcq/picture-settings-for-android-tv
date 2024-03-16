package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys

class DarkModePreferencesFragment : BasePreferenceFragment(R.xml.dark_mode_prefs),
    Preference.OnPreferenceChangeListener {

    private lateinit var preferences: DarkModePreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = (requireActivity().application as App).darkModePreferences
        updatePreferences()
        preferenceScreen.forEach { preference: Preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    private fun updatePreferences() {
        findPreference<SwitchPreference>(PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED)?.isChecked =
            preferences.threeStepsDarkModeEnabled
        findPreference<SeekBarPreference>(PreferencesKeys.NIGHT_BACKLIGHT)?.value = preferences.nightBacklight
        findPreference<SeekBarPreference>(PreferencesKeys.SCREEN_FILTER_POWER)?.value = preferences.screenFilterPower
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            PreferencesKeys.NIGHT_BACKLIGHT -> preferences.nightBacklight = newValue as Int
            PreferencesKeys.SCREEN_FILTER_POWER -> preferences.screenFilterPower = newValue as Int
            PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED -> preferences.threeStepsDarkModeEnabled =
                newValue as Boolean
            PreferencesKeys.IS_ADDITIONAL_DIMMING_ENABLED -> {
                if (preferences.currentMode != DarkModeManager.Mode.OFF) {
                    preferences.isScreenFilterEnabled = newValue as Boolean
                }
            }
        }
        return true
    }
}