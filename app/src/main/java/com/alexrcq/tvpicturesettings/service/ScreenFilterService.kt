package com.alexrcq.tvpicturesettings.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import timber.log.Timber

class ScreenFilterService : AccessibilityService() {

    lateinit var screenFilter: ScreenFilter

    override fun onServiceConnected() {
        Timber.d("onServiceConnected")
        screenFilter = ScreenFilter()
        sharedInstance = this
        sendBroadcast(Intent(ACTION_SERVICE_CONNECTED))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        sharedInstance = null
        return super.onUnbind(intent)
    }

    inner class ScreenFilter {

        private val view = View(applicationContext)

        fun setEnabled(enabled: Boolean) {
            if (enabled) enable() else disable()
        }

        fun setColor(color: Int) {
            view.setBackgroundColor(color)
        }

        fun setPower(power: Int) {
            view.alpha = power.coerceIn(MIN_POWER, MAX_POWER) / SCALE_FACTOR.toFloat()
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
        private const val MIN_POWER = 0
        private const val MAX_POWER = 98
        private const val SCALE_FACTOR = 100

        const val ACTION_SERVICE_CONNECTED = "com.alexrcq.tvpicturesettings.ACTION_SCREEN_FILTER_SERVICE_CONNECTED"

        var sharedInstance: ScreenFilterService? = null

        fun isServiceConnected(): Boolean = sharedInstance != null
    }
}