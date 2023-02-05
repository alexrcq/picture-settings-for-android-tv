package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.TV_PICTURE_COLOR_TUNE_ENABLE

class ColorTunerFragment : BaseColorTunerFragment(R.xml.color_tuner_prefs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(TV_PICTURE_COLOR_TUNE_ENABLE)?.setOnPreferenceChangeListener { _, newValue ->
            val isColorTuneEnabled = newValue as Boolean
            if (!isColorTuneEnabled) {
                Toast.makeText(requireContext(), R.string.please_wait, Toast.LENGTH_LONG).show()
            }
            return@setOnPreferenceChangeListener true
        }
    }
}