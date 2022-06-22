package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.PictureSettings

class AdvancedVideoPreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private var noiseReductionPref: ListPreference? = null
    private var adaptiveLumaPref: SwitchPreference? = null
    private var localContrastPref: SwitchPreference? = null
    private var hdrPref: SwitchPreference? = null

    private lateinit var pictureSettings: PictureSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.advanced_video_prefs, rootKey)
        pictureSettings = PictureSettings.getInstance(requireContext())
        noiseReductionPref = findPreference(AppPreferences.Keys.NOISE_REDUCTION)
        adaptiveLumaPref = findPreference(AppPreferences.Keys.ADAPTIVE_LUMA_CONTROL)
        localContrastPref = findPreference(AppPreferences.Keys.LOCAL_CONTRAST_CONTROL)
        hdrPref = findPreference(AppPreferences.Keys.HDR)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    private val onNoiseReductionChangedCallback = { key: String, value: Int ->
        if (key == PictureSettings.KEY_PICTURE_NOISE_REDUCTION) {
            noiseReductionPref?.value = value.toString()
        }
    }

    override fun onStart() {
        super.onStart()
        pictureSettings.addOnSettingsChangedCallback(onNoiseReductionChangedCallback)
        updateUi()
    }

    override fun onStop() {
        super.onStop()
        pictureSettings.removeOnSettingsChangedCallback(onNoiseReductionChangedCallback)
    }

    private fun updateUi() {
        noiseReductionPref?.value = pictureSettings.noiseReduction.toString()
        localContrastPref?.isChecked = pictureSettings.isLocalContrastEnabled
        adaptiveLumaPref?.isChecked = pictureSettings.isAdaptiveLumaEnabled
        hdrPref?.isChecked = pictureSettings.isHdrEnabled
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            AppPreferences.Keys.ADAPTIVE_LUMA_CONTROL -> {
                pictureSettings.isAdaptiveLumaEnabled = newValue as Boolean
            }
            AppPreferences.Keys.LOCAL_CONTRAST_CONTROL -> {
                pictureSettings.isLocalContrastEnabled = newValue as Boolean
            }
            AppPreferences.Keys.NOISE_REDUCTION -> {
                pictureSettings.noiseReduction = (newValue as String).toInt()
            }
            AppPreferences.Keys.HDR -> {
                pictureSettings.isHdrEnabled = newValue as Boolean
            }
        }
        return true
    }
}