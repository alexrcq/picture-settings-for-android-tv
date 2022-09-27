package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.ui.fragment.AdvancedVideoPreferenceFragment.PreferencesKeys.ADAPTIVE_LUMA_CONTROL
import com.alexrcq.tvpicturesettings.ui.fragment.AdvancedVideoPreferenceFragment.PreferencesKeys.HDR
import com.alexrcq.tvpicturesettings.ui.fragment.AdvancedVideoPreferenceFragment.PreferencesKeys.LOCAL_CONTRAST_CONTROL
import com.alexrcq.tvpicturesettings.ui.fragment.AdvancedVideoPreferenceFragment.PreferencesKeys.NOISE_REDUCTION

class AdvancedVideoPreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private var noiseReductionPref: ListPreference? = null
    private var adaptiveLumaPref: SwitchPreference? = null
    private var localContrastPref: SwitchPreference? = null
    private var hdrPref: SwitchPreference? = null

    private lateinit var pictureSettings: PictureSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.advanced_video_prefs, rootKey)
        pictureSettings = PictureSettings(requireContext())
        noiseReductionPref = findPreference(NOISE_REDUCTION)
        adaptiveLumaPref = findPreference(ADAPTIVE_LUMA_CONTROL)
        localContrastPref = findPreference(LOCAL_CONTRAST_CONTROL)
        hdrPref = findPreference(HDR)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            ADAPTIVE_LUMA_CONTROL -> {
                pictureSettings.isAdaptiveLumaEnabled = newValue as Boolean
            }
            LOCAL_CONTRAST_CONTROL -> {
                pictureSettings.isLocalContrastEnabled = newValue as Boolean
            }
            NOISE_REDUCTION -> {
                pictureSettings.noiseReduction = (newValue as String).toInt()
            }
            HDR -> {
                pictureSettings.isHdrEnabled = newValue as Boolean
            }
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        noiseReductionPref?.value = pictureSettings.noiseReduction.toString()
        localContrastPref?.isChecked = pictureSettings.isLocalContrastEnabled
        adaptiveLumaPref?.isChecked = pictureSettings.isAdaptiveLumaEnabled
        hdrPref?.isChecked = pictureSettings.isHdrEnabled
    }

    private object PreferencesKeys {
        const val NOISE_REDUCTION = "noise_reduction"
        const val ADAPTIVE_LUMA_CONTROL = "adaptive_luma_control"
        const val LOCAL_CONTRAST_CONTROL = "local_contrast_control"
        const val HDR = "hdr"
    }
}