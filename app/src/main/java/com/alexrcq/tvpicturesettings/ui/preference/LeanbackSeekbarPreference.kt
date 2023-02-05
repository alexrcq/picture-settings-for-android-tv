package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.R

open class LeanbackSeekbarPreference  @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.seekBarStyle
) : SeekBarPreference(context, attributeSet, defStyleAttr) {

    init {
        layoutResource = R.layout.leanback_seekbar_preference
    }
}