package com.alexrcq.tvpicturesettings.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import com.alexrcq.tvpicturesettings.R
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeParseException


class TimePickerPreference @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.dialogPreferenceStyle
) : DialogPreference(context, attributeSet, defStyleAttr) {

    var hour = 0
        private set

    var minute = 0
        private set

    fun setTime(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
        val time = String.format("%02d:%02d", hour, minute)
        persistString(time)
        callChangeListener(time)
    }

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

    override fun getDialogLayoutResource(): Int =
        R.layout.preference_dialog_timepicker

    override fun onGetDefaultValue(a: TypedArray, index: Int): String? = a.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        val timeStr = getPersistedString(defaultValue.toString())
        val time = if (timeStr.isNullOrBlank()) {
            getDefaultTime()
        } else try {
            LocalTime.parse(timeStr)
        } catch (e: DateTimeParseException) {
            Timber.d("The time pattern should be 'HH:mm'", e)
            getDefaultTime()
        }
        setTime(time.hour, time.minute)
    }

    private fun getDefaultTime(): LocalTime = LocalTime.of(0, 0)

    override fun getSummary(): String = String.format("%d:%02d", hour, minute)
}
