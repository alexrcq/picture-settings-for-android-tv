package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences

private const val DEFAULT_COLOR_GAIN = 1024

class PicturePreferences(sharedPreferences: SharedPreferences) : SharedPreferencesStore(sharedPreferences) {
    var isWhiteBalanceFixed: Boolean by preference(PreferencesKeys.IS_WHITE_BALANCE_FIXED, false)
    var showAboutVideoPrefsRuLocalization: Boolean by preference(
        PreferencesKeys.SHOW_ABOUT_VIDEO_PREFS_RU_LOCALIZATION,
        true
    )
    var redGain: Int by preference(PreferencesKeys.RED_GAIN, DEFAULT_COLOR_GAIN)
    var greenGain: Int by preference(PreferencesKeys.GREEN_GAIN, DEFAULT_COLOR_GAIN)
    var blueGain: Int by preference(PreferencesKeys.BLUE_GAIN, DEFAULT_COLOR_GAIN)
}