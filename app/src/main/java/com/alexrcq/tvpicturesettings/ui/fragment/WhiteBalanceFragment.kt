package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.RESET_VALUES
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_BLUE_GAIN
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_GREEN_GAIN
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_RED_GAIN

private const val WB_STANDARD_VALUE = 1024

class WhiteBalanceFragment : GlobalSettingsFragment(R.xml.white_balance_prefs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(RESET_VALUES)?.setOnPreferenceClickListener {
            resetWhiteBalance()
            true
        }
    }

    private fun resetWhiteBalance() {
        globalSettings.putInt(PICTURE_RED_GAIN, WB_STANDARD_VALUE)
        globalSettings.putInt(PICTURE_GREEN_GAIN, WB_STANDARD_VALUE)
        globalSettings.putInt(PICTURE_BLUE_GAIN, WB_STANDARD_VALUE)
    }
}
