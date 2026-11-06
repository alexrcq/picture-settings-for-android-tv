package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.platform.VideoSettings
import com.alexrcq.tvpicturesettings.platform.mtk.MtkVideoSettings
import com.alexrcq.tvpicturesettings.service.MtkWhiteBalanceLocker
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.storage.VideoPreferences
import com.alexrcq.tvpicturesettings.ui.fragment.base.GlobalSettingsFragment
import com.alexrcq.tvpicturesettings.util.onClick

class MtkWhiteBalanceFragment : GlobalSettingsFragment(R.xml.white_balance_prefs) {

    private lateinit var videoSettings: VideoSettings
    private lateinit var videoPreferences: VideoPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val app = requireActivity().application as App
        videoSettings = app.videoSettings
        videoPreferences = app.videoPreferences
        setupPreferences()
    }

    private fun setupPreferences() {
        findPreference<Preference>(PreferencesKeys.RESET_VALUES)?.onClick(videoSettings::resetWhiteBalance)
        if (videoPreferences.isWhiteBalanceLocked) {
            setControlsEnabled(false)
            scrollToPreference(PreferencesKeys.WHITE_BALANCE_LOCKED)
        }
        findPreference<Preference>(PreferencesKeys.WHITE_BALANCE_LOCKED)?.setOnPreferenceChangeListener { _, newValue ->
            val isWhiteBalanceLocked = newValue as Boolean
            setControlsEnabled(!isWhiteBalanceLocked)
            if (isWhiteBalanceLocked) {
                saveWhiteBalanceValues()
                MtkWhiteBalanceLocker.startForeground(requireContext())
            } else {
                MtkWhiteBalanceLocker.stop(requireContext())
            }
            true
        }
    }

    private fun setControlsEnabled(isEnabled: Boolean) {
        preferenceScreen.forEach { preference ->
            if (preference.key != PreferencesKeys.WHITE_BALANCE_LOCKED) {
                preference.isEnabled = isEnabled
            }
        }
    }

    private fun saveWhiteBalanceValues() {
        MtkVideoSettings.COLOR_GAIN_KEYS.forEach { key ->
            videoPreferences.put(key, globalSettings.getInt(key, MtkVideoSettings.DEFAULT_COLOR_GAIN))
        }
    }
}