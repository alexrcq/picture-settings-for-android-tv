package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceViewHolder

class LongPressGlobalSeekbarPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.seekBarStyle
) : GlobalSeekbarPreference(context, attributeSet, defStyleAttr) {

    private var onLongClickCallback: (() -> Unit)? = null

    fun onLongClick(onLongClick: () -> Unit) {
        onLongClickCallback = onLongClick
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setOnLongClickListener { performLongClick() }
    }

    private fun performLongClick(): Boolean {
        if (!isEnabled || !isSelectable) {
            return false
        }
        return onLongClickCallback?.let { it(); true } ?: false
    }
}