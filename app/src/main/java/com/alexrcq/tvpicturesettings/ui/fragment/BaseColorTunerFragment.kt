package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.RESET_VALUES
import com.alexrcq.tvpicturesettings.onClick
import com.alexrcq.tvpicturesettings.ui.preference.ColorTunerSeekbarPreference

private const val DEFAULT_TUNER_VALUE = 50

open class BaseColorTunerFragment(@XmlRes private val preferencesResId: Int) :
    GlobalSettingsFragment(preferencesResId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(RESET_VALUES)?.onClick(::resetTunerValues)
    }

    private fun resetTunerValues() {
        preferenceScreen.forEach { preference ->
            if (preference is ColorTunerSeekbarPreference) {
                globalSettings.putInt(preference.key, DEFAULT_TUNER_VALUE)
            }
        }
    }
}