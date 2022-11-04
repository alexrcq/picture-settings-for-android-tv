package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commitNow
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.BRIGHTNESS
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.CONTRAST
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.HUE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.SATURATION
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.SHARPNESS
import com.alexrcq.tvpicturesettings.storage.PictureSettings

class PictureEqualizerPreferenceFragment : BasePreferenceFragment(R.xml.picture_equalizer_prefs) {

    private var brightnessPref: SeekBarPreference? = null
    private var contrastPref: SeekBarPreference? = null
    private var saturationPref: SeekBarPreference? = null
    private var huePref: SeekBarPreference? = null
    private var sharpnessPref: SeekBarPreference? = null

    private lateinit var pictureSettings: PictureSettings

    private val onBackPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showPicturePreferenceFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pictureSettings = PictureSettings(requireContext())
        brightnessPref = findPreference(BRIGHTNESS)
        contrastPref = findPreference(CONTRAST)
        saturationPref = findPreference(SATURATION)
        huePref = findPreference(HUE)
        sharpnessPref = findPreference(SHARPNESS)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressCallback
        )
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
            BRIGHTNESS -> {
                pictureSettings.brightness = newValue as Int
            }
            CONTRAST -> {
                pictureSettings.contrast = newValue as Int
            }
            SATURATION -> {
                pictureSettings.saturation = newValue as Int
            }
            HUE -> {
                pictureSettings.hue = newValue as Int
            }
            SHARPNESS -> {
                pictureSettings.sharpness = newValue as Int
            }
        }
        return true
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