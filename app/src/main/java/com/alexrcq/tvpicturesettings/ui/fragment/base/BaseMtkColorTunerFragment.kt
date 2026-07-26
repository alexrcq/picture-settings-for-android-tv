package com.alexrcq.tvpicturesettings.ui.fragment.base

import androidx.annotation.XmlRes
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.ui.preference.ColorTunerSeekBarPreference

abstract class BaseMtkColorTunerFragment(@XmlRes prefsResId: Int = 0) : GlobalSettingsFragment(prefsResId) {

    protected fun resetTunerValues() {
        preferenceScreen.forEach { (it as? ColorTunerSeekBarPreference)?.reset() }
    }
}