package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet

open class GlobalSeekbarPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.seekBarStyle
) : LeanbackSeekbarPreference(context, attributeSet, defStyleAttr) {

    final override fun isPersistent() = false
}