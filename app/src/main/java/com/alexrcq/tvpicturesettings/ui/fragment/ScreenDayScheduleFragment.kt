package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ui.fragment.base.BasePreferenceFragment
import com.alexrcq.tvpicturesettings.ui.preference.TimePickerPreference

class ScreenDayScheduleFragment : BasePreferenceFragment(0) {

    private var day: Int = 1

    private val prefsListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key != null && key.startsWith("screen_schedule_day_${day}")) {
                    (requireActivity().application as App).screenScheduler.updateSchedule()
                }
            }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        day = requireArguments().getInt(ARG_DAY)
        val daysEntries = resources.getStringArray(R.array.days_of_week_entries)

        val screen = preferenceManager.createPreferenceScreen(requireContext())
        screen.title = daysEntries[day - 1]

        val dayEnabledPref =
                SwitchPreference(requireContext()).apply {
                    key = "screen_schedule_day_${day}_enabled"
                    title = getString(R.string.enable)
                    setDefaultValue(false)
                }
        screen.addPreference(dayEnabledPref)

        val onTimePref =
                TimePickerPreference(requireContext()).apply {
                    key = "screen_schedule_day_${day}_on"
                    title = getString(R.string.screen_on_time)
                    setDefaultValue("08:00")
                }
        screen.addPreference(onTimePref)

        val offTimePref =
                TimePickerPreference(requireContext()).apply {
                    key = "screen_schedule_day_${day}_off"
                    title = getString(R.string.screen_off_time)
                    setDefaultValue("23:00")
                }
        screen.addPreference(offTimePref)

        preferenceScreen = screen

        onTimePref.dependency = dayEnabledPref.key
        offTimePref.dependency = dayEnabledPref.key

        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
                prefsListener
        )
        super.onDestroy()
    }

    companion object {
        private const val ARG_DAY = "arg_day"

        fun newInstance(day: Int) =
                ScreenDayScheduleFragment().apply { arguments = bundleOf(ARG_DAY to day) }
    }
}
