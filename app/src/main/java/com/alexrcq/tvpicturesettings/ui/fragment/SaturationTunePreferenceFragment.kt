package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_SATURATION_BLUE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_SATURATION_CVAN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_SATURATION_FLESH_TONE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_SATURATION_GREEN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_SATURATION_MAGENTA
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_SATURATION_RED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_SATURATION_YELLOW
import com.alexrcq.tvpicturesettings.storage.ColorTuner

class SaturationTunePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private var bluePref: SeekBarPreference? = null
    private var cvanPref: SeekBarPreference? = null
    private var fleshTonePref: SeekBarPreference? = null
    private var greenPref: SeekBarPreference? = null
    private var magentaPref: SeekBarPreference? = null
    private var redPref: SeekBarPreference? = null
    private var yellowPref: SeekBarPreference? = null

    private lateinit var colorTuner: ColorTuner

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.saturation_tune_prefs, rootKey)
        colorTuner = ColorTuner(requireContext())
        bluePref = findPreference(TUNER_SATURATION_BLUE)
        cvanPref = findPreference(TUNER_SATURATION_CVAN)
        fleshTonePref = findPreference(TUNER_SATURATION_FLESH_TONE)
        greenPref = findPreference(TUNER_SATURATION_GREEN)
        magentaPref = findPreference(TUNER_SATURATION_MAGENTA)
        redPref = findPreference(TUNER_SATURATION_RED)
        yellowPref = findPreference(TUNER_SATURATION_YELLOW)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        greenPref?.value = colorTuner.greenSaturation
        bluePref?.value = colorTuner.blueSaturation
        cvanPref?.value = colorTuner.cyanSaturation
        yellowPref?.value = colorTuner.yellowSaturation
        magentaPref?.value = colorTuner.magentaSaturation
        redPref?.value = colorTuner.redSaturation
        fleshTonePref?.value = colorTuner.fleshToneSaturation
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            TUNER_SATURATION_BLUE -> {
                colorTuner.blueSaturation = newValue as Int
            }
            TUNER_SATURATION_CVAN -> {
                colorTuner.cyanSaturation = newValue as Int
            }
            TUNER_SATURATION_YELLOW -> {
                colorTuner.yellowSaturation = newValue as Int
            }
            TUNER_SATURATION_MAGENTA -> {
                colorTuner.magentaSaturation = newValue as Int
            }
            TUNER_SATURATION_RED -> {
                colorTuner.redSaturation = newValue as Int
            }
            TUNER_SATURATION_FLESH_TONE -> {
                colorTuner.fleshToneSaturation = newValue as Int
            }
            TUNER_SATURATION_GREEN -> {
                colorTuner.greenSaturation = newValue as Int
            }
        }
        return true
    }
}