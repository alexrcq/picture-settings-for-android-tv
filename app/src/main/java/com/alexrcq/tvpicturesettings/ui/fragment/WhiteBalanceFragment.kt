package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.WhiteBalanceFixerService
import com.alexrcq.tvpicturesettings.storage.PicturePreferences
import com.alexrcq.tvpicturesettings.storage.TvSettings
import com.alexrcq.tvpicturesettings.util.onClick

class WhiteBalanceFragment : GlobalSettingsFragment(R.xml.white_balance_prefs) {

    private lateinit var pictureSettings: TvSettings.Picture
    private lateinit var picturePreferences: PicturePreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val application = (requireActivity().application as App)
        pictureSettings = application.tvSettingsRepository.getPictureSettings()
        picturePreferences = application.picturePreferences
        findPreference<Preference>(PreferencesKeys.RESET_VALUES)?.onClick {
            pictureSettings.resetWhiteBalance()
        }
        if (picturePreferences.isWhiteBalanceFixed) {
            setWhiteBalancePrefsEnabled(false)
            scrollToPreference(PreferencesKeys.IS_WHITE_BALANCE_FIXED)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key == PreferencesKeys.IS_WHITE_BALANCE_FIXED) {
            val isWhiteBalanceFixed = newValue as Boolean
            setWhiteBalancePrefsEnabled(!isWhiteBalanceFixed)
            if (isWhiteBalanceFixed) {
                WhiteBalanceFixerService.startForeground(requireContext())
            } else {
                WhiteBalanceFixerService.stop(requireContext())
            }
        }
        return super.onPreferenceChange(preference, newValue)
    }

    override fun updatePreference(preference: Preference) {
        if (picturePreferences.isWhiteBalanceFixed) return
        super.updatePreference(preference)
    }

    private fun setWhiteBalancePrefsEnabled(isEnabled: Boolean) {
        preferenceScreen.forEach { preference ->
            if (preference.key != PreferencesKeys.IS_WHITE_BALANCE_FIXED) {
                preference.isEnabled = isEnabled
            }
        }
    }
}