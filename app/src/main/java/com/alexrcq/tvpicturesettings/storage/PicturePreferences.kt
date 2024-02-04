package com.alexrcq.tvpicturesettings.storage

private const val DEFAULT_COLOR_GAIN = 1024

class PicturePreferences(preferencesStore: SharedPreferencesStore) {
    var isWhiteBalanceFixed: Boolean by preferencesStore.preference(PreferencesKeys.IS_WHITE_BALANCE_FIXED, false)
    var redGain: Int by preferencesStore.preference(PreferencesKeys.RED_GAIN, DEFAULT_COLOR_GAIN)
    var greenGain: Int by preferencesStore.preference(PreferencesKeys.GREEN_GAIN, DEFAULT_COLOR_GAIN)
    var blueGain: Int by preferencesStore.preference(PreferencesKeys.BLUE_GAIN, DEFAULT_COLOR_GAIN)
}