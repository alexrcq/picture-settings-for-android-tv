package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_OFFSET_BLUE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_OFFSET_GREEN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_OFFSET_RED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_OFFSET_RESET
import com.alexrcq.tvpicturesettings.storage.ColorTuner

class OffsetTunePreferenceFragment : BasePreferenceFragment(R.xml.offset_tune_prefs) {

    private var bluePref: SeekBarPreference? = null
    private var greenPref: SeekBarPreference? = null
    private var redPref: SeekBarPreference? = null

    private lateinit var colorTuner: ColorTuner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorTuner = ColorTuner(requireContext())
        bluePref = findPreference(TUNER_OFFSET_BLUE)
        greenPref = findPreference(TUNER_OFFSET_GREEN)
        redPref = findPreference(TUNER_OFFSET_RED)
        findPreference<Preference>(TUNER_OFFSET_RESET)?.setOnPreferenceClickListener {
            colorTuner.resetOffset()
            updateUi()
            true
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
}