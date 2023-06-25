package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.BRIGHTNESS_TUNE
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.COLOR_TUNER
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.DARK_MODE_PREFERENCES
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.HUE_TUNE
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.OFFSET_TUNE
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.SATURATION_TUNE
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.SCHEDULED_DARK_MODE
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.VIDEO_PREFERENCES
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.WHITE_BALANCE
import com.alexrcq.tvpicturesettings.ui.preference.TimePickerPreference

class SettingsFragment : LeanbackSettingsFragmentCompat() {

    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(PictureFragment())
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        when (pref.key) {
            VIDEO_PREFERENCES -> startPreferenceFragment(VideoPreferencesFragment())
            COLOR_TUNER -> startPreferenceFragment(ColorTunerFragment())
            SATURATION_TUNE -> startPreferenceFragment(SaturationTuneFragment())
            HUE_TUNE -> startPreferenceFragment(HueTuneFragment())
            BRIGHTNESS_TUNE -> startPreferenceFragment(BrightnessTuneFragment())
            OFFSET_TUNE -> startPreferenceFragment(OffsetTuneFragment())
            WHITE_BALANCE -> startPreferenceFragment(WhiteBalanceFragment())
            SCHEDULED_DARK_MODE -> startPreferenceFragment(ScheduledDarkModeFragment())
            DARK_MODE_PREFERENCES -> startPreferenceFragment(DarkModePreferencesFragment())
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