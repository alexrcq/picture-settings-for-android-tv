package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.GlobalSettingsFragment
import com.alexrcq.tvpicturesettings.util.showToast

class MtkColorTunerCategoriesFragment : GlobalSettingsFragment(R.xml.color_tuner_categories_prefs) {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        findPreference<SwitchPreference>(MtkGlobalKeys.TV_PICTURE_COLOR_TUNE_ENABLE)?.setOnPreferenceChangeListener { _, newValue ->
            val isColorTuneEnabled = newValue as Boolean
            if (!isColorTuneEnabled) {
                showToast(getString(R.string.please_wait))
            }
            true
        }
    }
}