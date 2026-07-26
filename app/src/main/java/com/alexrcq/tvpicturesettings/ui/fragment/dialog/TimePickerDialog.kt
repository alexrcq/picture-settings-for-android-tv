package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.leanback.preference.LeanbackPreferenceDialogFragmentCompat
import androidx.leanback.widget.picker.TimePicker
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ui.preference.TimePickerPreference

class TimePickerDialog : LeanbackPreferenceDialogFragmentCompat() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.preference_dialog_timepicker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timePickerPreference = preference as TimePickerPreference

        view.findViewById<TimePicker>(R.id.time_picker).apply {
            setIs24Hour(true)
            isActivated = true
            hour = timePickerPreference.hour
            minute = timePickerPreference.minute

            setOnClickListener {
                timePickerPreference.setTime(hour, minute)
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
        fun newInstance(key: String) = TimePickerDialog().apply {
            arguments = bundleOf(ARG_KEY to key)
        }
    }
}