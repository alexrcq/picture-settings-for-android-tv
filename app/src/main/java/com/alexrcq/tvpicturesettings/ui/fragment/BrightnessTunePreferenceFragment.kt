package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.ColorTuner
import com.alexrcq.tvpicturesettings.ui.fragment.BrightnessTunePreferenceFragment.PreferencesKeys.TUNER_BRIGHTNESS_BLUE
import com.alexrcq.tvpicturesettings.ui.fragment.BrightnessTunePreferenceFragment.PreferencesKeys.TUNER_BRIGHTNESS_CVAN
import com.alexrcq.tvpicturesettings.ui.fragment.BrightnessTunePreferenceFragment.PreferencesKeys.TUNER_BRIGHTNESS_FLESH_TONE
import com.alexrcq.tvpicturesettings.ui.fragment.BrightnessTunePreferenceFragment.PreferencesKeys.TUNER_BRIGHTNESS_GREEN
import com.alexrcq.tvpicturesettings.ui.fragment.BrightnessTunePreferenceFragment.PreferencesKeys.TUNER_BRIGHTNESS_MAGENTA
import com.alexrcq.tvpicturesettings.ui.fragment.BrightnessTunePreferenceFragment.PreferencesKeys.TUNER_BRIGHTNESS_RED
import com.alexrcq.tvpicturesettings.ui.fragment.BrightnessTunePreferenceFragment.PreferencesKeys.TUNER_BRIGHTNESS_YELLOW

class BrightnessTunePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private var bluePref: SeekBarPreference? = null
    private var cyanPref: SeekBarPreference? = null
    private var fleshTonePref: SeekBarPreference? = null
    private var greenPref: SeekBarPreference? = null
    private var magentaPref: SeekBarPreference? = null
    private var redPref: SeekBarPreference? = null
    private var yellowPref: SeekBarPreference? = null

    private lateinit var colorTuner: ColorTuner

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.brightness_tune_prefs, rootKey)
        colorTuner = ColorTuner(requireContext())
        bluePref = findPreference(TUNER_BRIGHTNESS_BLUE)
        cyanPref = findPreference(TUNER_BRIGHTNESS_CVAN)
        fleshTonePref = findPreference(TUNER_BRIGHTNESS_FLESH_TONE)
        greenPref = findPreference(TUNER_BRIGHTNESS_GREEN)
        magentaPref = findPreference(TUNER_BRIGHTNESS_MAGENTA)
        redPref = findPreference(TUNER_BRIGHTNESS_RED)
        yellowPref = findPreference(TUNER_BRIGHTNESS_YELLOW)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        greenPref?.value = colorTuner.greenBrightness
        bluePref?.value = colorTuner.blueBrightness
        cyanPref?.value = colorTuner.cyanBrightness
        yellowPref?.value = colorTuner.yellowBrightness
        magentaPref?.value = colorTuner.magentaBrightness
        redPref?.value = colorTuner.redBrightness
        fleshTonePref?.value = colorTuner.fleshToneBrightness
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            TUNER_BRIGHTNESS_BLUE -> {
                colorTuner.blueBrightness = newValue as Int
            }
            TUNER_BRIGHTNESS_CVAN -> {
                colorTuner.cyanBrightness = newValue as Int
            }
            TUNER_BRIGHTNESS_YELLOW -> {
                colorTuner.yellowBrightness = newValue as Int
            }
            TUNER_BRIGHTNESS_MAGENTA -> {
                colorTuner.magentaBrightness = newValue as Int
            }
            TUNER_BRIGHTNESS_RED -> {
                colorTuner.redBrightness = newValue as Int
            }
            TUNER_BRIGHTNESS_FLESH_TONE -> {
                colorTuner.fleshToneBrightness = newValue as Int
            }
            TUNER_BRIGHTNESS_GREEN -> {
                colorTuner.greenBrightness = newValue as Int
            }
        }
        return true
    }

    private object PreferencesKeys {
        const val TUNER_BRIGHTNESS_BLUE = "tuner_brightness_blue"
        const val TUNER_BRIGHTNESS_CVAN = "tuner_brightness_cvan"
        const val TUNER_BRIGHTNESS_FLESH_TONE = "tuner_brightness_flesh_tone"
        const val TUNER_BRIGHTNESS_GREEN = "tuner_brightness_green"
        const val TUNER_BRIGHTNESS_MAGENTA = "tuner_brightness_magenta"
        const val TUNER_BRIGHTNESS_RED = "tuner_brightness_red"
        const val TUNER_BRIGHTNESS_YELLOW = "tuner_brightness_yellow"
    }
}