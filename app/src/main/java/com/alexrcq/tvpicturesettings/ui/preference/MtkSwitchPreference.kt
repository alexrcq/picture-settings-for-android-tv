package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.storage.MtkGlobalSettingsStore

class MtkSwitchPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.switchPreferenceStyle
) : SwitchPreference(context, attributeSet, defStyleAttr) {

    init {
        preferenceDataStore = MtkGlobalSettingsStore(context.contentResolver)
    }
}