package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet

class ColorTunerSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.seekBarPreferenceStyle
) : GlobalSeekBarPreference(context, attributeSet, defStyleAttr) {

    fun reset() {
        value = DEFAULT_VALUE
    }

    companion object {
        private const val DEFAULT_VALUE = 50
    }
}