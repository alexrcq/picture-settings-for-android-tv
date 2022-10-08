package com.alexrcq.tvpicturesettings.ui

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import timber.log.Timber

class FullScreenDarkFilter(val context: AccessibilityService) {

    private val darkFilterView = View(context).apply {
        setBackgroundColor(Color.BLACK)
    }

    var isEnabled = false
        set(enabled) {
            field = enabled
            if (enabled) {
                enable()
                return
            }
            disable()
        }

    var alpha: Float
        get() = darkFilterView.alpha
        set(value) {
            darkFilterView.alpha = value
        }

    private fun enable() {
        val layoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        val windowManager =
            context.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
        try {
            windowManager.addView(darkFilterView, layoutParams)
        } catch (e: IllegalStateException) {
            Timber.d("the dark filter view has already added", e)
        }
    }

    private fun disable() {
        if (darkFilterView.windowToken != null) {
            val windowManager =
                context.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(darkFilterView)
        }
    }
}