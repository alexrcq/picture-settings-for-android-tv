package com.alexrcq.tvpicturesettings.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.DarkModeManager
import com.alexrcq.tvpicturesettings.util.TvUtils

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!TvUtils.isCurrentTvSupported(this)) {
            showTvNotSupported()
            return
        }
        setContentView(R.layout.activity_main)
        DarkModeManager.startForeground(this)
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
        val dialog = AlertDialog.Builder(this)
            .setMessage(R.string.not_supported_tv)
            .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus()
    }
}