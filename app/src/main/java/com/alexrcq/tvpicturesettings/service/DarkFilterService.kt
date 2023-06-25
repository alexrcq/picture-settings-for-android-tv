package com.alexrcq.tvpicturesettings.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import timber.log.Timber

class DarkFilterService : AccessibilityService() {

    lateinit var darkFilter: DarkFilter

    override fun onServiceConnected() {
        Timber.d("onServiceConnected")
        darkFilter = DarkFilter()
        sharedInstance = this
        sendBroadcast(Intent(ACTION_SERVICE_CONNECTED))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        sharedInstance = null
        return super.onUnbind(intent)
    }

    inner class DarkFilter {

        private val view = View(applicationContext).apply {
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
            get() = view.alpha
            set(value) {
                view.alpha = value
            }

        private fun enable() {
            val layoutParams = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            if (view.parent == null) {
                windowManager.addView(view, layoutParams)
            }
        }

        private fun disable() {
            if (view.parent != null) {
                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                windowManager.removeView(view)
            }
        }
    }

    companion object {
        const val ACTION_SERVICE_CONNECTED =
            "com.alexrcq.tvpicturesettings.ACTION_DARK_FILTER_SERVICE_CONNECTED"

        var sharedInstance: DarkFilterService? = null
    }
}