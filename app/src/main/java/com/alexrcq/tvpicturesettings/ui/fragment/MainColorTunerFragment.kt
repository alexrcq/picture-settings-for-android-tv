package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.TvSettings
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.util.showToast

class MainColorTunerFragment : ColorTunerFragment(R.xml.color_tuner_prefs) {

    private val pictureSettings: TvSettings.Picture
        get() = (requireActivity().application as App).tvSettings.picture

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key == MtkGlobalKeys.TV_PICTURE_COLOR_TUNE_ENABLE) {
            val isColorTuneEnabled = newValue as Boolean
            pictureSettings.isColorTuneEnabled = isColorTuneEnabled
            if (!isColorTuneEnabled) {
                showToast(getString(R.string.please_wait))
            }
        }
        return super.onPreferenceChange(preference, newValue)
    }

    override fun updatePreference(preference: Preference) {
        super.updatePreference(preference)
        if (preference.key == MtkGlobalKeys.TV_PICTURE_COLOR_TUNE_ENABLE) {
            val enableTunerPref = preference as SwitchPreference
            enableTunerPref.isChecked = pictureSettings.isColorTuneEnabled
        }
    }
}