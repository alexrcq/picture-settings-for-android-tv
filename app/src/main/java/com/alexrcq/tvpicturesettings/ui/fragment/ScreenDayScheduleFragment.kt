package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ui.fragment.base.BasePreferenceFragment
import com.alexrcq.tvpicturesettings.ui.preference.TimePickerPreference

class ScreenDayScheduleFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val day = requireArguments().getInt(ARG_DAY)
        val daysEntries = resources.getStringArray(R.array.days_of_week_entries)

        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).apply {
            title = daysEntries[day - 1]
            addEnablePreference(day)
            addOnTimePreference(day)
            addOffTimePreference(day)
        }
        setupDependencies(day)
    }

    private fun PreferenceScreen.addEnablePreference(day: Int) {
        val preference = SwitchPreference(context).apply {
            key = "screen_schedule_day_${day}_enabled"
            title = getString(R.string.enable)
            setDefaultValue(false)
        }
        addPreference(preference)
    }

    private fun PreferenceScreen.addOnTimePreference(day: Int) {
        val preference = TimePickerPreference(context).apply {
            key = "screen_schedule_day_${day}_on"
            title = getString(R.string.screen_on_time)
            setDefaultValue(getString(R.string.def_screen_on_time))
        }
        addPreference(preference)
    }

    private fun PreferenceScreen.addOffTimePreference(day: Int) {
        val preference = TimePickerPreference(context).apply {
            key = "screen_schedule_day_${day}_off"
            title = getString(R.string.screen_off_time)
            setDefaultValue(getString(R.string.def_screen_off_time))
        }
        addPreference(preference)
    }

    private fun setupDependencies(day: Int) {
        val enabledKey = "screen_schedule_day_${day}_enabled"
        findPreference<Preference>("screen_schedule_day_${day}_on")?.dependency = enabledKey
        findPreference<Preference>("screen_schedule_day_${day}_off")?.dependency = enabledKey
    }

    companion object {
        private const val ARG_DAY = "arg_day"

        fun newInstance(day: Int) = ScreenDayScheduleFragment().apply { arguments = bundleOf(ARG_DAY to day) }
    }
}
