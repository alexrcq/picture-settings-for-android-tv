package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore

open class GlobalSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.seekBarPreferenceStyle
) : LeanbackSeekBarPreference(context, attributeSet, defStyleAttr) {

    init {
        preferenceDataStore = GlobalSettingsStore(context.contentResolver)
    }
}