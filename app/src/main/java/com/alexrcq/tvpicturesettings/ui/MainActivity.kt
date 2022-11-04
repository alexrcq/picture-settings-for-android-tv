package com.alexrcq.tvpicturesettings.ui

import android.Manifest
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.alexrcq.tvpicturesettings.DarkModeManager
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.AcceptDebuggingDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.NotSupportedTVDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.UsbDebuggingRequiredDialog
import com.alexrcq.tvpicturesettings.util.*


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isCurrentTvSupported) {
            NotSupportedTVDialog().show(supportFragmentManager, NotSupportedTVDialog.TAG)
            return
        }
        if (!isDebuggingEnabled) {
            UsbDebuggingRequiredDialog().show(
                supportFragmentManager,
                UsbDebuggingRequiredDialog.TAG
            )
            return
        }
        setContentView(R.layout.activity_main)
        if (!hasPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
            AcceptDebuggingDialog().show(supportFragmentManager, AcceptDebuggingDialog.TAG)
            return
        }
        if (!isDarkModeManagerEnabled) {
            enableAccessibilityService(DarkModeManager::class.java)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val view = window.decorView
        val layoutParams = view.layoutParams as WindowManager.LayoutParams
        layoutParams.apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.END
        }
        windowManager.updateViewLayout(view, layoutParams)
    }
}