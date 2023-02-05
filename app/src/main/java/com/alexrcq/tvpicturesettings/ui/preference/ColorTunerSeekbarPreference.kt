package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet

class ColorTunerSeekbarPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.seekBarStyle
) : GlobalSeekbarPreference(context, attributeSet, defStyleAttr)