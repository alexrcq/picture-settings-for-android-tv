package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PictureSettings

class AdvancedVideoPreferenceFragment : LeanbackPreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private var noiseReductionPref: ListPreference? = null
    private var adaptiveLumaPref: SwitchPreference? = null
    private var localContrastPref: SwitchPreference? = null

    private lateinit var pictureSettings: PictureSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.advanced_video_prefs, rootKey)
        pictureSettings = PictureSettings(requireContext())
        noiseReductionPref = findPreference(Keys.NOISE_REDUCTION)
        adaptiveLumaPref = findPreference(Keys.ADAPTIVE_LUMA_CONTROL)
        localContrastPref = findPreference(Keys.LOCAL_CONTRAST_CONTROL)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        noiseReductionPref?.value = pictureSettings.noiseReduction.toString()
        localContrastPref?.isChecked = pictureSettings.isLocalContrastEnabled
        adaptiveLumaPref?.isChecked = pictureSettings.isAdaptiveLumaEnabled
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            Keys.ADAPTIVE_LUMA_CONTROL -> {
                pictureSettings.isAdaptiveLumaEnabled = newValue as Boolean
            }
            Keys.LOCAL_CONTRAST_CONTROL -> {
                pictureSettings.isLocalContrastEnabled = newValue as Boolean
            }
            Keys.NOISE_REDUCTION -> {
                pictureSettings.noiseReduction = (newValue as String).toInt()
            }
        }
        return true
    }

    private object Keys {
        const val NOISE_REDUCTION = "noise_reduction"
        const val ADAPTIVE_LUMA_CONTROL = "adaptive_luma_control"
        const val LOCAL_CONTRAST_CONTROL = "local_contrast_control"
    }
}