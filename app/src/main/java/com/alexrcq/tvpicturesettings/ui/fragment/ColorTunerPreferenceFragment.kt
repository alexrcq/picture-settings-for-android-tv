package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.widget.Toast
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.PictureSettings

class ColorTunerPreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private var colorTunerEnabledPref: SwitchPreference? = null
    private var redGainPref: SeekBarPreference? = null
    private var greenGainPref: SeekBarPreference? = null
    private var blueGainPref: SeekBarPreference? = null

    private lateinit var pictureSettings: PictureSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.color_tuner_prefs, rootKey)
        pictureSettings = PictureSettings.getInstance(requireContext())
        colorTunerEnabledPref = findPreference(AppPreferences.Keys.COLOR_TUNER_ENABLED)
        redGainPref = findPreference(AppPreferences.Keys.COLOR_TUNER_RED_GAIN)
        greenGainPref = findPreference(AppPreferences.Keys.COLOR_TUNER_GREEN_GAIN)
        blueGainPref = findPreference(AppPreferences.Keys.COLOR_TUNER_BLUE_GAIN)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        colorTunerEnabledPref?.isChecked = pictureSettings.isColorTuneEnabled
        redGainPref?.value = pictureSettings.colorTuneRedGain
        greenGainPref?.value = pictureSettings.colorTuneGreenGain
        blueGainPref?.value = pictureSettings.colorTuneBlueGain
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            AppPreferences.Keys.COLOR_TUNER_ENABLED -> {
                val isColorTuneEnabled = newValue as Boolean
                if (!isColorTuneEnabled) {
                    Toast.makeText(requireContext(), R.string.please_wait, Toast.LENGTH_LONG).show()
                }
                pictureSettings.isColorTuneEnabled = isColorTuneEnabled
            }
            AppPreferences.Keys.COLOR_TUNER_RED_GAIN -> {
                pictureSettings.colorTuneRedGain = newValue as Int
            }
            AppPreferences.Keys.COLOR_TUNER_GREEN_GAIN -> {
                pictureSettings.colorTuneGreenGain = newValue as Int
            }
            AppPreferences.Keys.COLOR_TUNER_BLUE_GAIN -> {
                pictureSettings.colorTuneBlueGain = newValue as Int
            }
        }
        return true
    }
}