package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ui.preference.TimePickerPreference
import com.alexrcq.tvpicturesettings.ui.fragment.main.MainFragment

class SettingsFragment : LeanbackSettingsFragmentCompat() {

    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(MainFragment())
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        when (pref.key) {
            PreferencesKeys.VIDEO_PREFERENCES -> startPreferenceFragment(VideoPreferencesFragment())
            PreferencesKeys.WHITE_BALANCE -> startPreferenceFragment(WhiteBalanceFragment())
            PreferencesKeys.SCHEDULED_DARK_MODE -> startPreferenceFragment(ScheduledDarkModeFragment())
            PreferencesKeys.DARK_MODE_PREFERENCES -> startPreferenceFragment(DarkModePreferencesFragment())
            PreferencesKeys.COLOR_TUNER -> startPreferenceFragment(MainColorTunerFragment())
            PreferencesKeys.HUE_TUNE -> startPreferenceFragment(ColorTunerFragment(R.xml.hue_tune_prefs))
            PreferencesKeys.OFFSET_TUNE -> startPreferenceFragment(ColorTunerFragment(R.xml.offset_tune_prefs))
            PreferencesKeys.SATURATION_TUNE -> startPreferenceFragment(ColorTunerFragment(R.xml.saturation_tune_prefs))
            PreferencesKeys.BRIGHTNESS_TUNE -> startPreferenceFragment(ColorTunerFragment(R.xml.brightness_tune_prefs))
        }
        return true
    }

    override fun onPreferenceDisplayDialog(caller: PreferenceFragmentCompat, preference: Preference): Boolean {
        if (preference is TimePickerPreference) {
            startPreferenceFragment(TimePickerDialog(preference.key).apply {
                @Suppress("DEPRECATION") setTargetFragment(caller, 0)
            })
            return true
        }
        return super.onPreferenceDisplayDialog(caller, preference)
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen) = false
}