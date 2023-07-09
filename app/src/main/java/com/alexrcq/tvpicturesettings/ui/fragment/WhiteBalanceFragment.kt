package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.FIX_WB_VALUES
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.RESET_VALUES
import com.alexrcq.tvpicturesettings.helper.WhiteBalanceHelper
import com.alexrcq.tvpicturesettings.onClick

class WhiteBalanceFragment : GlobalSettingsFragment(R.xml.white_balance_prefs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(RESET_VALUES)?.onClick {
            WhiteBalanceHelper(requireContext()).resetWhiteBalance()
        }
        if (appSettings.isWhiteBalanceFixed) {
            setWhiteBalancePrefsEnabled(false)
            scrollToPreference(FIX_WB_VALUES)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key == FIX_WB_VALUES) {
            val isWhiteBalanceFixed = newValue as Boolean
            setWhiteBalancePrefsEnabled(!isWhiteBalanceFixed)
        }
        return super.onPreferenceChange(preference, newValue)
    }

    override fun updatePreference(preference: Preference) {
        if (appSettings.isWhiteBalanceFixed) {
            return
        }
        super.updatePreference(preference)
    }

    private fun setWhiteBalancePrefsEnabled(isEnabled: Boolean) {
        preferenceScreen.forEach { preference ->
            if (preference.key != FIX_WB_VALUES) {
                preference.isEnabled = isEnabled
            }
        }
    }
}
