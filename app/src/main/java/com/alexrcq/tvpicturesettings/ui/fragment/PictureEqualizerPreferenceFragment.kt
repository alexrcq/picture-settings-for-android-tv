package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commitNow
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.PictureSettings

class PictureEqualizerPreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private var brightnessPref: SeekBarPreference? = null
    private var contrastPref: SeekBarPreference? = null
    private var saturationPref: SeekBarPreference? = null
    private var huePref: SeekBarPreference? = null
    private var sharpnessPref: SeekBarPreference? = null

    private lateinit var pictureSettings: PictureSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.picture_equalizer_prefs, rootKey)
        pictureSettings = PictureSettings.getInstance(requireContext())
        brightnessPref = findPreference(AppPreferences.Keys.BRIGHTNESS)
        contrastPref = findPreference(AppPreferences.Keys.CONTRAST)
        saturationPref = findPreference(AppPreferences.Keys.SATURATION)
        huePref = findPreference(AppPreferences.Keys.HUE)
        sharpnessPref = findPreference(AppPreferences.Keys.SHARPNESS)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        brightnessPref?.value = pictureSettings.brightness
        contrastPref?.value = pictureSettings.contrast
        saturationPref?.value = pictureSettings.saturation
        huePref?.value = pictureSettings.hue
        sharpnessPref?.value = pictureSettings.sharpness
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            AppPreferences.Keys.BRIGHTNESS -> {
                pictureSettings.brightness = newValue as Int
            }
            AppPreferences.Keys.CONTRAST -> {
                pictureSettings.contrast = newValue as Int
            }
            AppPreferences.Keys.SATURATION -> {
                pictureSettings.saturation = newValue as Int
            }
            AppPreferences.Keys.HUE -> {
                pictureSettings.hue = newValue as Int
            }
            AppPreferences.Keys.SHARPNESS -> {
                pictureSettings.sharpness = newValue as Int
            }
        }
        return true
    }


    private val onBackPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showPicturePreferenceFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressCallback
        )
    }

    private fun showPicturePreferenceFragment() {
        parentFragmentManager.popBackStackImmediate()
        parentFragmentManager.commitNow {
            replace(
                androidx.leanback.preference.R.id.settings_preference_fragment_container,
                PicturePreferenceFragment()
            )
        }
    }
}