package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.leanback.preference.LeanbackPreferenceDialogFragmentCompat
import androidx.leanback.widget.picker.TimePicker
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ui.preference.TimePickerPreference


class TimePickerDialog private constructor() : LeanbackPreferenceDialogFragmentCompat() {

    constructor(key: String) : this() {
        arguments = Bundle(1).apply { putString(ARG_KEY, key) }
    }

    private lateinit var timePicker: TimePicker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.preference_dialog_timepicker,
            container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        timePicker = view.findViewById(R.id.time_picker)
        val timePickerPreference = preference as TimePickerPreference
        timePicker.apply {
            setIs24Hour(true)
            isActivated = true
            hour = timePickerPreference.hour
            minute = timePickerPreference.minute
        }.setOnClickListener {
            timePickerPreference.setTime(timePicker.hour, timePicker.minute)
            requireActivity().onBackPressed()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressedCallback
        )
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            parentFragmentManager.popBackStackImmediate()
        }
    }
}