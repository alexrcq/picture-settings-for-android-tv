package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.ADAPTIVE_LUMA_CONTROL
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.HDR
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.LOCAL_CONTRAST_CONTROL
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.NOISE_REDUCTION
import com.alexrcq.tvpicturesettings.storage.PictureSettings

class AdvancedVideoPreferenceFragment : BasePreferenceFragment(R.xml.advanced_video_prefs) {

    private var noiseReductionPref: ListPreference? = null
    private var adaptiveLumaPref: SwitchPreference? = null
    private var localContrastPref: SwitchPreference? = null
    private var hdrPref: SwitchPreference? = null

    private lateinit var pictureSettings: PictureSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pictureSettings = PictureSettings(requireContext())
        noiseReductionPref = findPreference(NOISE_REDUCTION)
        adaptiveLumaPref = findPreference(ADAPTIVE_LUMA_CONTROL)
        localContrastPref = findPreference(LOCAL_CONTRAST_CONTROL)
        hdrPref = findPreference(HDR)
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
}