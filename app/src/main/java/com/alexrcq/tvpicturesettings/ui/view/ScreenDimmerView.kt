package com.alexrcq.tvpicturesettings.ui.view

import android.content.Context
import android.graphics.Color

class ScreenDimmerView(context: Context): ScreenFilterView(context) {

    init {
        setBackgroundColor(Color.BLACK)
        z = 1.0f
    }
}