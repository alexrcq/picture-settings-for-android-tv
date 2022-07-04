package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.alexrcq.tvpicturesettings.ui.preference.TimePickerPreference

class SettingsFragment : LeanbackSettingsFragmentCompat() {

    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(PicturePreferenceFragment())
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        when (pref.key) {
            FragmentsKeys.ADVANCED_VIDEO -> startPreferenceFragment(
                AdvancedVideoPreferenceFragment()
            )
            FragmentsKeys.COLOR_TUNER -> startPreferenceFragment(
                ColorTunerPreferenceFragment()
            )
        }
        return true
    }

    override fun onPreferenceDisplayDialog(
        caller: PreferenceFragmentCompat,
        preference: Preference
    ): Boolean {
        if (preference is TimePickerPreference) {
            startPreferenceFragment(
                TimePickerDialog(preference.key).apply {
                    @Suppress("DEPRECATION")
                    setTargetFragment(caller, 0)
                }
            )
            return true
        }
        return super.onPreferenceDisplayDialog(caller, preference)
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen) =
        false

    object FragmentsKeys {
        const val ADVANCED_VIDEO = "advanced_video"
        const val COLOR_TUNER = "color_tuner"
    }
}