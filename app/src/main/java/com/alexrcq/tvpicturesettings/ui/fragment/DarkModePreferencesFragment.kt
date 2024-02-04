package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.R

class DarkModePreferencesFragment : BasePreferenceFragment(R.xml.dark_mode_prefs),
    Preference.OnPreferenceChangeListener {

    private lateinit var darkModeManager: DarkModeManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        darkModeManager = (requireActivity().application as App).darkModeManager
        updatePreferences()
        preferenceScreen.forEach { preference: Preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    private fun updatePreferences() {
        val preferences = (requireActivity().application as App).darkModePreferences
        findPreference<SwitchPreference>(PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED)?.isChecked =
            preferences.threeStepsDarkModeEnabled
        findPreference<SeekBarPreference>(PreferencesKeys.NIGHT_BACKLIGHT)?.value = preferences.nightBacklight
        findPreference<SeekBarPreference>(PreferencesKeys.SCREEN_FILTER_POWER)?.value = preferences.screenFilterPower
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            PreferencesKeys.NIGHT_BACKLIGHT -> darkModeManager.setNightBacklight(newValue as Int)
            PreferencesKeys.SCREEN_FILTER_POWER -> darkModeManager.setScreenFilterPower(newValue as Int)
            PreferencesKeys.STEP_BY_STEP_DARK_MODE_ENABLED ->
                darkModeManager.setStepByStepDarkModeEnabled(newValue as Boolean)
            PreferencesKeys.IS_ADDITIONAL_DIMMING_ENABLED -> {
                if (darkModeManager.currentMode != DarkModeManager.Mode.OFF) {
                    darkModeManager.setScreenFilterEnabled(newValue as Boolean)
                }
            }
        }
        return true
    }
}