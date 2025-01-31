package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.WhiteBalanceLocker
import com.alexrcq.tvpicturesettings.storage.PicturePreferences
import com.alexrcq.tvpicturesettings.domain.PictureSettings
import com.alexrcq.tvpicturesettings.util.onClick

class WhiteBalanceFragment : GlobalSettingsFragment(R.xml.white_balance_prefs) {

    private lateinit var pictureSettings: PictureSettings
    private lateinit var picturePreferences: PicturePreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val application = (requireActivity().application as App)
        pictureSettings = application.tvSettings.picture
        picturePreferences = application.picturePreferences
        findPreference<Preference>(PreferencesKeys.RESET_VALUES)?.onClick {
            pictureSettings.resetWhiteBalance()
        }
        if (picturePreferences.isWhiteBalanceLocked) {
            setWhiteBalancePrefsEnabled(false)
            scrollToPreference(PreferencesKeys.IS_WHITE_BALANCE_LOCKED)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key == PreferencesKeys.IS_WHITE_BALANCE_LOCKED) {
            val isWhiteBalanceLocked = newValue as Boolean
            setWhiteBalancePrefsEnabled(!isWhiteBalanceLocked)
            if (isWhiteBalanceLocked) {
                WhiteBalanceLocker.startForeground(requireContext())
            } else {
                WhiteBalanceLocker.stop(requireContext())
            }
        }
        return super.onPreferenceChange(preference, newValue)
    }

    override fun updatePreference(preference: Preference) {
        if (picturePreferences.isWhiteBalanceLocked) return
        super.updatePreference(preference)
    }

    private fun setWhiteBalancePrefsEnabled(isEnabled: Boolean) {
        preferenceScreen.forEach { preference ->
            if (preference.key != PreferencesKeys.IS_WHITE_BALANCE_LOCKED) {
                preference.isEnabled = isEnabled
            }
        }
    }
}