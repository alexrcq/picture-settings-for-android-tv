package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences
import android.content.res.Resources
import com.alexrcq.tvpicturesettings.R

class PicturePreferences(sharedPreferences: SharedPreferences, resources: Resources) :
    SharedPreferencesStore(sharedPreferences) {

    var isWhiteBalanceLocked: Boolean by preference(
        PreferencesKeys.IS_WHITE_BALANCE_LOCKED,
        resources.getBoolean(R.bool.is_white_balance_locked)
    )
    var showAboutVideoPrefsRuLocalization: Boolean by preference(
        PreferencesKeys.SHOW_ABOUT_VIDEO_PREFS_RU_LOCALIZATION,
        resources.getBoolean(R.bool.show_about_video_prefs_ru_localization)
    )
    var redGain: Int by preference(PreferencesKeys.RED_GAIN, resources.getInteger(R.integer.color_gain))
    var greenGain: Int by preference(PreferencesKeys.GREEN_GAIN, resources.getInteger(R.integer.color_gain))
    var blueGain: Int by preference(PreferencesKeys.BLUE_GAIN, resources.getInteger(R.integer.color_gain))
}