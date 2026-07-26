package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.BasePreferenceFragment
import com.alexrcq.tvpicturesettings.util.onClick

class BlueLightFilterFragment : BasePreferenceFragment(R.xml.blue_light_filter_prefs) {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        findPreference<Preference>(PreferencesKeys.RESET_VALUES)?.onClick(::resetFilterSettings)
    }

    private fun resetFilterSettings() {
        findPreference<SeekBarPreference>(PreferencesKeys.BLUE_LIGHT_FILTER_TEMPERATURE)?.value =
            resources.getInteger(R.integer.def_blue_light_filter_temperature)
        findPreference<SeekBarPreference>(PreferencesKeys.BLUE_LIGHT_FILTER_POWER)?.value =
            resources.getInteger(R.integer.def_blue_light_filter_power)
    }
}