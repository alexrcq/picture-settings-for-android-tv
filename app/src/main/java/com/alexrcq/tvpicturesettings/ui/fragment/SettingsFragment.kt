package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.alexrcq.tvpicturesettings.ui.fragment.SettingsFragment.FragmentsKeys.ADVANCED_VIDEO
import com.alexrcq.tvpicturesettings.ui.fragment.SettingsFragment.FragmentsKeys.BRIGHTNESS_TUNE
import com.alexrcq.tvpicturesettings.ui.fragment.SettingsFragment.FragmentsKeys.COLOR_TUNER
import com.alexrcq.tvpicturesettings.ui.fragment.SettingsFragment.FragmentsKeys.HUE_TUNE
import com.alexrcq.tvpicturesettings.ui.fragment.SettingsFragment.FragmentsKeys.OFFSET_TUNE
import com.alexrcq.tvpicturesettings.ui.fragment.SettingsFragment.FragmentsKeys.SATURATION_TUNE
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
            ADVANCED_VIDEO -> startPreferenceFragment(AdvancedVideoPreferenceFragment())
            COLOR_TUNER -> startPreferenceFragment(ColorTunerPreferenceFragment())
            SATURATION_TUNE -> startPreferenceFragment(SaturationTunePreferenceFragment())
            HUE_TUNE -> startPreferenceFragment(HueTunePreferenceFragment())
            BRIGHTNESS_TUNE -> startPreferenceFragment(BrightnessTunePreferenceFragment())
            OFFSET_TUNE -> startPreferenceFragment(OffsetTunePreferenceFragment())
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
        const val SATURATION_TUNE = "saturation_tune"
        const val HUE_TUNE = "hue_tune"
        const val BRIGHTNESS_TUNE = "brightness_tune"
        const val OFFSET_TUNE = "offset_tune"
    }
}