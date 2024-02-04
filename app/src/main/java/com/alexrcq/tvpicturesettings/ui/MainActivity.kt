package com.alexrcq.tvpicturesettings.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.DarkModeService
import com.alexrcq.tvpicturesettings.util.DeviceUtils
import com.alexrcq.tvpicturesettings.util.requestFocusForced

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DeviceUtils.isCurrentTvSupported(this)) {
            showTvNotSupported()
            return
        }
        setContentView(R.layout.activity_main)
        DarkModeService.startForeground(this)
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

    private fun showTvNotSupported() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.not_supported_tv)
            .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .create().apply {
                setOnShowListener { getButton(DialogInterface.BUTTON_POSITIVE).requestFocusForced() }
            }.show()
    }
}