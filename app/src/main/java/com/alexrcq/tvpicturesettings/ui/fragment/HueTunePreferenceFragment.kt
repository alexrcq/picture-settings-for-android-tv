package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_HUE_BLUE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_HUE_CVAN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_HUE_FLESH_TONE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_HUE_GREEN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_HUE_MAGENTA
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_HUE_RED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_HUE_RESET
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_HUE_YELLOW
import com.alexrcq.tvpicturesettings.storage.ColorTuner

class HueTunePreferenceFragment : LeanbackPreferenceFragmentCompat(),
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
        setPreferencesFromResource(R.xml.hue_tune_prefs, rootKey)
        colorTuner = ColorTuner(requireContext())
        bluePref = findPreference(TUNER_HUE_BLUE)
        cvanPref = findPreference(TUNER_HUE_CVAN)
        fleshTonePref = findPreference(TUNER_HUE_FLESH_TONE)
        greenPref = findPreference(TUNER_HUE_GREEN)
        magentaPref = findPreference(TUNER_HUE_MAGENTA)
        redPref = findPreference(TUNER_HUE_RED)
        yellowPref = findPreference(TUNER_HUE_YELLOW)
        findPreference<Preference>(TUNER_HUE_RESET)?.setOnPreferenceClickListener {
            colorTuner.resetHue()
            updateUi()
            true
        }
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        greenPref?.value = colorTuner.greenHue
        bluePref?.value = colorTuner.blueHue
        cvanPref?.value = colorTuner.cyanHue
        yellowPref?.value = colorTuner.yellowHue
        magentaPref?.value = colorTuner.magentaHue
        redPref?.value = colorTuner.redHue
        fleshTonePref?.value = colorTuner.fleshToneHue
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            TUNER_HUE_BLUE -> {
                colorTuner.blueHue = newValue as Int
            }
            TUNER_HUE_CVAN -> {
                colorTuner.cyanHue = newValue as Int
            }
            TUNER_HUE_YELLOW -> {
                colorTuner.yellowHue = newValue as Int
            }
            TUNER_HUE_MAGENTA -> {
                colorTuner.magentaHue = newValue as Int
            }
            TUNER_HUE_RED -> {
                colorTuner.redHue = newValue as Int
            }
            TUNER_HUE_FLESH_TONE -> {
                colorTuner.fleshToneHue = newValue as Int
            }
            TUNER_HUE_GREEN -> {
                colorTuner.greenHue = newValue as Int
            }
        }
        return true
    }
}