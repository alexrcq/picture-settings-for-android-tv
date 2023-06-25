package com.alexrcq.tvpicturesettings.ui

import android.Manifest
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.alexrcq.tvpicturesettings.*
import com.alexrcq.tvpicturesettings.service.PictureSettingsService
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.AcceptDebuggingDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.AdbRequiredDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.NotSupportedTVDialog

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
        PictureSettingsService.start(this)
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