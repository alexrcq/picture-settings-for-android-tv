package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore

open class GlobalListPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dialogPreferenceStyle
) : ListPreference(context, attributeSet, defStyleAttr) {

    init {
        preferenceDataStore = GlobalSettingsStore(context.contentResolver)
    }
}
