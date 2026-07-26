package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences
import android.content.res.Resources
import com.alexrcq.tvpicturesettings.R

class VideoPreferences(sharedPreferences: SharedPreferences, resources: Resources) :
    SharedPreferencesStore(sharedPreferences) {

    var isWhiteBalanceLocked: Boolean by preference(
        PreferencesKeys.WHITE_BALANCE_LOCKED,
        resources.getBoolean(R.bool.def_white_balance_locked)
    )
    var showAboutVideoPrefsRuLocalization: Boolean by preference(
        PreferencesKeys.SHOW_ABOUT_VIDEO_PREFS_RU_LOCALIZATION,
        resources.getBoolean(R.bool.def_show_about_video_prefs_ru_localization)
    )
}