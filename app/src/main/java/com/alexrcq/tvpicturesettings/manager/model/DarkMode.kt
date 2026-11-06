package com.alexrcq.tvpicturesettings.manager.model

import androidx.annotation.StringRes
import com.alexrcq.tvpicturesettings.R

enum class DarkMode(@StringRes val message: Int) {
    OFF(R.string.dark_mode_off),
    ONLY_BACKLIGHT(R.string.dark_mode_only_backlight),
    FULL(R.string.dark_mode_max)
}
