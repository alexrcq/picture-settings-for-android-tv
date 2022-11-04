package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_BRIGHTNESS_BLUE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_BRIGHTNESS_CVAN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_BRIGHTNESS_FLESH_TONE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_BRIGHTNESS_GREEN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_BRIGHTNESS_MAGENTA
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_BRIGHTNESS_RED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_BRIGHTNESS_RESET
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TUNER_BRIGHTNESS_YELLOW
import com.alexrcq.tvpicturesettings.storage.ColorTuner

class BrightnessTunePreferenceFragment : BasePreferenceFragment(R.xml.brightness_tune_prefs) {

    private var bluePref: SeekBarPreference? = null
    private var cyanPref: SeekBarPreference? = null
    private var fleshTonePref: SeekBarPreference? = null
    private var greenPref: SeekBarPreference? = null
    private var magentaPref: SeekBarPreference? = null
    private var redPref: SeekBarPreference? = null
    private var yellowPref: SeekBarPreference? = null

    private lateinit var colorTuner: ColorTuner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorTuner = ColorTuner(requireContext())
        bluePref = findPreference(TUNER_BRIGHTNESS_BLUE)
        cyanPref = findPreference(TUNER_BRIGHTNESS_CVAN)
        fleshTonePref = findPreference(TUNER_BRIGHTNESS_FLESH_TONE)
        greenPref = findPreference(TUNER_BRIGHTNESS_GREEN)
        magentaPref = findPreference(TUNER_BRIGHTNESS_MAGENTA)
        redPref = findPreference(TUNER_BRIGHTNESS_RED)
        yellowPref = findPreference(TUNER_BRIGHTNESS_YELLOW)
        findPreference<Preference>(TUNER_BRIGHTNESS_RESET)?.setOnPreferenceClickListener {
            colorTuner.resetBrightness()
            updateUi()
            true
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
}