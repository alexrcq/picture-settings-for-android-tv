package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceViewHolder

class LongPressGlobalSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.seekBarPreferenceStyle
) : GlobalSeekBarPreference(context, attributeSet, defStyleAttr) {

    private var onLongClick: (() -> Unit)? = null

    fun onLongClick(action: () -> Unit) {
        onLongClick = action
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke()
            onLongClick != null
        }
    }
}