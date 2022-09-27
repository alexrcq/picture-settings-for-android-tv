package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.ColorTuner
import com.alexrcq.tvpicturesettings.ui.fragment.OffsetTunePreferenceFragment.PreferencesKeys.TUNER_OFFSET_BLUE
import com.alexrcq.tvpicturesettings.ui.fragment.OffsetTunePreferenceFragment.PreferencesKeys.TUNER_OFFSET_GREEN
import com.alexrcq.tvpicturesettings.ui.fragment.OffsetTunePreferenceFragment.PreferencesKeys.TUNER_OFFSET_RED

class OffsetTunePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private var bluePref: SeekBarPreference? = null
    private var greenPref: SeekBarPreference? = null
    private var redPref: SeekBarPreference? = null

    private lateinit var colorTuner: ColorTuner

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.offset_tune_prefs, rootKey)
        colorTuner = ColorTuner(requireContext())
        bluePref = findPreference(TUNER_OFFSET_BLUE)
        greenPref = findPreference(TUNER_OFFSET_GREEN)
        redPref = findPreference(TUNER_OFFSET_RED)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        greenPref?.value = colorTuner.greenOffset
        bluePref?.value = colorTuner.blueOffset
        redPref?.value = colorTuner.redOffset
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            TUNER_OFFSET_BLUE -> {
                colorTuner.blueOffset = newValue as Int
            }
            TUNER_OFFSET_RED -> {
                colorTuner.redOffset = newValue as Int
            }
            TUNER_OFFSET_GREEN -> {
                colorTuner.greenOffset = newValue as Int
            }
        }
        return true
    }

    private object PreferencesKeys {
        const val TUNER_OFFSET_BLUE = "tuner_offset_blue"
        const val TUNER_OFFSET_GREEN = "tuner_offset_green"
        const val TUNER_OFFSET_RED = "tuner_offset_red"
    }
}