package com.alexrcq.tvpicturesettings.helper

import android.content.Context
import com.alexrcq.tvpicturesettings.helper.GlobalSettings.Keys.PICTURE_BLUE_GAIN
import com.alexrcq.tvpicturesettings.helper.GlobalSettings.Keys.PICTURE_GREEN_GAIN
import com.alexrcq.tvpicturesettings.helper.GlobalSettings.Keys.PICTURE_RED_GAIN

private const val WB_STANDARD_VALUE = 1024

class WhiteBalanceHelper(context: Context) {

    private val globalSettings: GlobalSettings = GlobalSettingsWrapper(context.contentResolver)

    fun setWhiteBalance(redGain: Int, greenGain: Int, blueGain: Int) {
        globalSettings.putInt(PICTURE_RED_GAIN, redGain)
        globalSettings.putInt(PICTURE_GREEN_GAIN, greenGain)
        globalSettings.putInt(PICTURE_BLUE_GAIN, blueGain)
    }

    fun resetWhiteBalance() {
        setWhiteBalance(WB_STANDARD_VALUE, WB_STANDARD_VALUE, WB_STANDARD_VALUE)
    }
}