package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.BRIGHTNESS_TUNE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.COLOR_TUNER
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.HUE_TUNE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.OFFSET_TUNE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.SATURATION_TUNE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.SCHEDULED_DARK_MODE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.VIDEO_PREFERENCES
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
            VIDEO_PREFERENCES -> startPreferenceFragment(VideoPreferencesFragment())
            COLOR_TUNER -> startPreferenceFragment(ColorTunerPreferenceFragment())
            SATURATION_TUNE -> startPreferenceFragment(SaturationTunePreferenceFragment())
            HUE_TUNE -> startPreferenceFragment(HueTunePreferenceFragment())
            BRIGHTNESS_TUNE -> startPreferenceFragment(BrightnessTunePreferenceFragment())
            OFFSET_TUNE -> startPreferenceFragment(OffsetTunePreferenceFragment())
            SCHEDULED_DARK_MODE -> startPreferenceFragment(ScheduledDarkModePreferenceFragment())
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
}