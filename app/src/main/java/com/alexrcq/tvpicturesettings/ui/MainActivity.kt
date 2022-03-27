package com.alexrcq.tvpicturesettings.ui

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.AutoBacklightService
import com.alexrcq.tvpicturesettings.util.DialogButton.NEGATIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.DialogButton.POSITIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.Utils
import com.alexrcq.tvpicturesettings.util.makeButtonFocused

class MainActivity : FragmentActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Utils.isCurrentTVSupported(this)) {
            showNotSupportedTVDialog()
        } else if (!Utils.canWriteSecureSettings(this)) {
            showPermissionRequiredDialog()
        }
    }

    private var isAutoBacklightServiceBound = false

    private val autoBacklightServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {}
        override fun onServiceDisconnected(arg0: ComponentName) {}
    }

    override fun onStart() {
        super.onStart()
        if (!isAutoBacklightServiceBound) {
            bindService(
                Intent(this, AutoBacklightService::class.java),
                autoBacklightServiceConnection,
                Context.BIND_AUTO_CREATE
            )
            isAutoBacklightServiceBound = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (isAutoBacklightServiceBound) {
            unbindService(autoBacklightServiceConnection)
            isAutoBacklightServiceBound = false
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

    private fun showNotSupportedTVDialog() {
        val onOkClickListener = DialogInterface.OnClickListener { _, _ ->
            finish()
        }
        val alertDialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.not_supported_tv)
            .setPositiveButton(android.R.string.ok, onOkClickListener)
            .create()
        alertDialog.show()
        alertDialog.makeButtonFocused(NEGATIVE_BUTTON)
    }

    private fun showPermissionRequiredDialog() {
        val onOkClickListener = DialogInterface.OnClickListener { _, _ ->
            if (!Utils.canWriteSecureSettings(this)) {
                finish()
            }
        }
        val alertDialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.write_settings_permission_required)
            .setPositiveButton(android.R.string.ok, onOkClickListener)
            .create()
        alertDialog.show()
        alertDialog.makeButtonFocused(POSITIVE_BUTTON)
    }
}