package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ui.fragment.base.GlobalSettingsFragment
import com.alexrcq.tvpicturesettings.ui.preference.ColorTunerSeekBarPreference
import com.alexrcq.tvpicturesettings.util.onClick

class MtkColorTunerCategoryFragment : GlobalSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val category = TunerCategory.valueOf(
            requireArguments().getString(ARG_CATEGORY_NAME) ?: error("ARG_CATEGORY_NAME required")
        )

        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).apply {
            title = getString(category.titleResId)
            if (category == TunerCategory.GAIN) {
                addGainWarningPreference()
            }
            addColorPreferences(category)
            addResetPreference()
        }
    }

    private fun PreferenceScreen.addGainWarningPreference() {
        val warningPreference = Preference(context).apply {
            isSelectable = false
            summary = getString(R.string.color_gain_warning)
        }
        addPreference(warningPreference)
    }

    private fun PreferenceScreen.addColorPreferences(category: TunerCategory) {
        for (color in category.supportedColors) {
            val colorPreference = ColorTunerSeekBarPreference(context).apply {
                key = "tv_picture_color_tune_${category.keySegment}_${color.keySegment}"
                title = getString(color.titleResId)
            }
            addPreference(colorPreference)
        }
    }

    private fun PreferenceScreen.addResetPreference() {
        val resetPreference = Preference(context).apply {
            title = getString(R.string.reset)
            onClick(::resetTunerValues)
        }
        addPreference(resetPreference)
    }

    private fun resetTunerValues() {
        preferenceScreen.forEach { (it as? ColorTunerSeekBarPreference)?.reset() }
    }

    enum class TunerCategory(
        val keySegment: String,
        @StringRes val titleResId: Int,
        val supportedColors: List<TunerColor> = TunerColor.entries
    ) {
        BRIGHTNESS("brightness", R.string.brightness),
        SATURATION("saturation", R.string.saturation),
        HUE("hue", R.string.hue),
        OFFSET("offset", R.string.offset, RGB_TUNER_COLORS),
        GAIN("gain", R.string.color_gain, RGB_TUNER_COLORS)
    }

    enum class TunerColor(val keySegment: String, @StringRes val titleResId: Int) {
        RED("red", R.string.red),
        GREEN("green", R.string.green),
        BLUE("blue", R.string.blue),
        CYAN("cvan", R.string.cyan), // vendor typo
        MAGENTA("megenta", R.string.magenta), // vendor typo
        YELLOW("yellow", R.string.yellow),
        SKIN_TONE("flesh_tone", R.string.skin_tone)
    }

    companion object {
        private const val ARG_CATEGORY_NAME = "tuner_category_name"

        private val RGB_TUNER_COLORS = listOf(TunerColor.RED, TunerColor.GREEN, TunerColor.BLUE)

        fun newInstance(category: TunerCategory): MtkColorTunerCategoryFragment = MtkColorTunerCategoryFragment().apply {
            arguments = bundleOf(ARG_CATEGORY_NAME to category.name)
        }
    }
}