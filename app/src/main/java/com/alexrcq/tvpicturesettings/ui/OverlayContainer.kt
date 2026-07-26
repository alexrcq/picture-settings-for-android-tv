package com.alexrcq.tvpicturesettings.ui

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout

class OverlayContainer(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val frameLayout = FrameLayout(context)

    fun addView(view: View) {
        if (view.parent == null) {
            frameLayout.addView(view)
        }
    }

    fun attachToWindow(): Boolean {
        if (frameLayout.parent != null) {
            return true
        }
        if (!Settings.canDrawOverlays(context)) {
            return false
        }
        val layoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        return try {
            windowManager.addView(frameLayout, layoutParams)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun detachFromWindow() {
        if (frameLayout.parent != null) {
            runCatching { windowManager.removeView(frameLayout) }
        }
    }

    fun destroy() {
        detachFromWindow()
        frameLayout.removeAllViews()
    }
}