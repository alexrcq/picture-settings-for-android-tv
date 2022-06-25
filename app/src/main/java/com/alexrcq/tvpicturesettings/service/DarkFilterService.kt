package com.alexrcq.tvpicturesettings.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent


class DarkFilterService : AccessibilityService() {

    private var darkFilterView: View? = null
    var isDarkFilterActivated = false
        private set

    override fun onServiceConnected() {
        super.onServiceConnected()
        sharedInstance = this
        darkFilterView = View(this).apply {
            setBackgroundColor(Color.BLACK)
            alpha = 0.5f
        }
        application.sendBroadcast(Intent(ACTION_DARK_FILTER_SERVICE_INSTANTIATED))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun enableDarkFilter() {
        if (!isDarkFilterActivated) {
            val layoutParams = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            try {
                windowManager.addView(darkFilterView, layoutParams)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "the dark filter view has already added", e)
            }
            isDarkFilterActivated = true
        }
    }

    fun disableDarkFilter() {
        if (!isDarkFilterActivated) return
        if (darkFilterView?.windowToken != null) {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(darkFilterView)
            isDarkFilterActivated = false
        }
    }

    fun setDarkFilterAlpha(alpha: Float) {
        darkFilterView?.alpha = alpha
    }

    override fun onUnbind(intent: Intent?): Boolean {
        sharedInstance = null
        return super.onUnbind(intent)
    }

    companion object {
        const val TAG = "DarkFilterService"
        const val ACTION_DARK_FILTER_SERVICE_INSTANTIATED =
            "com.alexrcq.tvpicturesettings.ACTION_DARK_FILTER_SERVICE_INSTANTIATED"

        @SuppressLint("StaticFieldLeak")
        var sharedInstance: DarkFilterService? = null
    }
}