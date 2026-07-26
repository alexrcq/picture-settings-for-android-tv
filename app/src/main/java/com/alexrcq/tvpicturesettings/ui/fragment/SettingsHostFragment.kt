package com.alexrcq.tvpicturesettings.ui.fragment

import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.TimePickerDialog
import com.alexrcq.tvpicturesettings.ui.preference.TimePickerPreference

class SettingsHostFragment : LeanbackSettingsFragmentCompat() {

    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(PictureFragment())
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = when (pref.key) {
            PreferencesKeys.MSSP_VIDEO_PREFERENCES -> MsspVideoPreferencesFragment()
            PreferencesKeys.MTK_WHITE_BALANCE -> MtkWhiteBalanceFragment()
            PreferencesKeys.MTK_COLOR_TUNER -> MtkColorTunerCategoriesFragment()
            PreferencesKeys.MTK_BRIGHTNESS_TUNE -> MtkColorTunerCategoryFragment.newInstance(
                MtkColorTunerCategoryFragment.TunerCategory.BRIGHTNESS
            )
            PreferencesKeys.MTK_SATURATION_TUNE -> MtkColorTunerCategoryFragment.newInstance(
                MtkColorTunerCategoryFragment.TunerCategory.SATURATION
            )
            PreferencesKeys.MTK_HUE_TUNE -> MtkColorTunerCategoryFragment.newInstance(MtkColorTunerCategoryFragment.TunerCategory.HUE)
            PreferencesKeys.MTK_OFFSET_TUNE -> MtkColorTunerCategoryFragment.newInstance(MtkColorTunerCategoryFragment.TunerCategory.OFFSET)
            PreferencesKeys.MTK_GAIN_TUNE -> MtkColorTunerCategoryFragment.newInstance(MtkColorTunerCategoryFragment.TunerCategory.GAIN)
            PreferencesKeys.SCREEN_CONTROL -> ScreenControlFragment()
            PreferencesKeys.SCREEN_SCHEDULE_PREFERENCES -> ScreenScheduleFragment()
            PreferencesKeys.DARK_MODE_PREFERENCES -> DarkModePreferencesFragment()
            PreferencesKeys.DARK_MODE_SCHEDULE -> DarkModeScheduleFragment()
            PreferencesKeys.ADVANCED_DARK_MODE_PREFERENCES -> AdvancedDarkModePreferencesFragment()
            PreferencesKeys.BLUE_LIGHT_FILTER -> BlueLightFilterFragment()
            else -> {
                if (pref.key?.startsWith("screen_schedule_day_") == true) {
                    val day = pref.key.removePrefix("screen_schedule_day_").toIntOrNull()
                    if (day != null) ScreenDayScheduleFragment.newInstance(day) else null
                } else null
            }
        } ?: return false

        startPreferenceFragment(fragment)
        return true
    }

    override fun onPreferenceDisplayDialog(caller: PreferenceFragmentCompat, preference: Preference): Boolean {
        if (preference is TimePickerPreference) {
            val dialog = TimePickerDialog.newInstance(preference.key).apply {
                @Suppress("DEPRECATION") setTargetFragment(caller, 0)
            }
            startPreferenceFragment(dialog)
            return true
        }
        return super.onPreferenceDisplayDialog(caller, preference)
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen) = false
}