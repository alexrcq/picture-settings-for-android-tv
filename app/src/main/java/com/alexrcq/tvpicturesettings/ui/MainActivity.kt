package com.alexrcq.tvpicturesettings.ui

import android.Manifest
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.alexrcq.tvpicturesettings.*
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.AcceptDebuggingDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.AdbRequiredDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.NotSupportedTVDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isCurrentTvSupported) {
            NotSupportedTVDialog().show(supportFragmentManager, NotSupportedTVDialog.TAG)
            return
        }
        if (!isAdbEnabled && !hasPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
            AdbRequiredDialog().show(
                supportFragmentManager, AdbRequiredDialog.TAG
            )
            return
        }
        setContentView(R.layout.activity_main)
        if (!hasPermission(Manifest.permission.WRITE_SECURE_SETTINGS)) {
            AcceptDebuggingDialog().show(supportFragmentManager, AcceptDebuggingDialog.TAG)
            return
        }
        if (!isAccessibilityServiceEnabled(DarkModeManager::class.java)) {
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