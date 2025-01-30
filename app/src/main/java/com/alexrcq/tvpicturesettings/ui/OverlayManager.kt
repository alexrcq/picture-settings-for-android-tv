package com.alexrcq.tvpicturesettings.ui

import android.content.Context
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.alexrcq.tvpicturesettings.util.hasParent

class OverlayManager(context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val overlayContainer = FrameLayout(context)

    fun initOverlay() {
        if (!overlayContainer.hasParent()) {
            val layoutParams = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )
            windowManager.addView(overlayContainer, layoutParams)
        }
    }

    fun updateViewAttachment(view: View, isAttachedToOverlay: Boolean) {
        if (isAttachedToOverlay) {
            attachToOverlay(view)
        } else {
            detachFromOverlay(view)
        }
    }

    fun destroyOverlay() {
        if (overlayContainer.hasParent()) {
            windowManager.removeView(overlayContainer)
        }
    }

    private fun attachToOverlay(view: View) {
        if (!view.hasParent()) {
            overlayContainer.addView(view)
        }
    }

    private fun detachFromOverlay(view: View) {
        overlayContainer.removeView(view)
    }
}