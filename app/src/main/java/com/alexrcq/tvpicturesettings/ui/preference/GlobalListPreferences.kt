package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.preference.ListPreference
import androidx.preference.PreferenceViewHolder

open class GlobalListPreferences @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.dialogPreferenceStyle
) : ListPreference(context, attributeSet, defStyleAttr) {

    override fun isPersistent(): Boolean = false

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        with(TypedValue()) {
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                this,
                true
            )
            holder.itemView.setBackgroundResource(resourceId)
        }
    }
}
