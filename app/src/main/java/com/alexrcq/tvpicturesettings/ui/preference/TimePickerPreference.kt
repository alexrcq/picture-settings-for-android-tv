package com.alexrcq.tvpicturesettings.ui.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import java.time.LocalTime

class TimePickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dialogPreferenceStyle
) : DialogPreference(context, attrs, defStyleAttr) {

    private var time: LocalTime = LocalTime.MIDNIGHT

    val hour: Int get() = time.hour
    val minute: Int get() = time.minute

    fun setTime(hour: Int, minute: Int) {
        val time = LocalTime.of(hour, minute)
        val timeStr = time.toString()

        if (callChangeListener(timeStr)) {
            this.time = time
            persistString(timeStr)
            notifyChanged()
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): String? = a.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        time = runCatching {
            LocalTime.parse(getPersistedString(defaultValue as? String))
        }.getOrDefault(LocalTime.MIDNIGHT)
    }

    override fun getSummary(): String = time.toString()
}
