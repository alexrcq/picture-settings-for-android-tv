package com.alexrcq.tvpicturesettings.ui.fragment

import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.helper.GlobalSettings.Keys.TV_PICTURE_COLOR_TUNE_ENABLE
import com.alexrcq.tvpicturesettings.toBoolean
import com.alexrcq.tvpicturesettings.toInt

class ColorTunerFragment : BaseColorTunerFragment(R.xml.color_tuner_prefs) {

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key == TV_PICTURE_COLOR_TUNE_ENABLE) {
            val isColorTuneEnabled = newValue as Boolean
            globalSettings.putInt(preference.key, isColorTuneEnabled.toInt())
            if (!isColorTuneEnabled) {
                Toast.makeText(requireContext(), R.string.please_wait, Toast.LENGTH_LONG).show()
            }
        }
        return super.onPreferenceChange(preference, newValue)
    }

    override fun updatePreference(preference: Preference) {
        super.updatePreference(preference)
        if (preference.key == TV_PICTURE_COLOR_TUNE_ENABLE) {
            val enableTunerPref = preference as SwitchPreference
            enableTunerPref.isChecked =
                globalSettings.getInt(TV_PICTURE_COLOR_TUNE_ENABLE).toBoolean()
        }
    }
}