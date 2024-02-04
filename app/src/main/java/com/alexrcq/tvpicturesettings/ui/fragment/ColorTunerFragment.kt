package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.preference.ColorTunerSeekbarPreference
import com.alexrcq.tvpicturesettings.util.onClick

private const val DEFAULT_TUNER_VALUE = 50

open class ColorTunerFragment(@XmlRes private val prefsResId: Int) : GlobalSettingsFragment(prefsResId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(PreferencesKeys.RESET_VALUES)?.onClick(::resetTunerValues)
    }

    private fun resetTunerValues() {
        preferenceScreen.forEach { preference ->
            if (preference is ColorTunerSeekbarPreference) {
                globalSettings.putInt(preference.key, DEFAULT_TUNER_VALUE)
            }
        }
    }
}