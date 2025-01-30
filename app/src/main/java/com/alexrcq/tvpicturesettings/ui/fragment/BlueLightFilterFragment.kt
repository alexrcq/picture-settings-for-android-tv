package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.util.onClick

class BlueLightFilterFragment : BasePreferenceFragment(R.xml.blue_light_filter_prefs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(PreferencesKeys.RESET_VALUES)?.onClick(::restoreDefaults)
        findPreference<SwitchPreference>(PreferencesKeys.IS_BLUE_LIGHT_FILTER_ENABLED)?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val preferences = (requireActivity().application as App).darkModePreferences
                preferences.isAlwaysBlueFilterEnabled = newValue as Boolean
                true
            }
    }

    private fun restoreDefaults() {
        findPreference<SeekBarPreference>(PreferencesKeys.BLUE_LIGHT_FILTER_TEMPERATURE)?.value =
            resources.getInteger(R.integer.blue_filter_temperature)
        findPreference<SeekBarPreference>(PreferencesKeys.BLUE_LIGHT_FILTER_POWER)?.value =
            resources.getInteger(R.integer.blue_light_filter_power)
    }
}