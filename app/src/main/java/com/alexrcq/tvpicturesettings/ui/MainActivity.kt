package com.alexrcq.tvpicturesettings.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.AutoBacklightService
import com.alexrcq.tvpicturesettings.util.AdbUtils
import com.alexrcq.tvpicturesettings.util.DialogButton.POSITIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.Utils
import com.alexrcq.tvpicturesettings.util.makeButtonFocused
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : FragmentActivity(R.layout.activity_main) {

    private val dialogsToShow: LinkedBlockingQueue<Dialog> by lazy(LazyThreadSafetyMode.NONE) {
        LinkedBlockingQueue()
    }
    private val notSupportedTVDialog: Dialog
        get() {
            val onOkClickListener = DialogInterface.OnClickListener { _, _ ->
                finish()
            }
            return AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setMessage(R.string.not_supported_tv)
                .setPositiveButton(android.R.string.ok, onOkClickListener)
                .create()
                .apply {
                    setOnShowListener {
                        makeButtonFocused(POSITIVE_BUTTON)
                    }
                }
        }
    private val writeSecureSettingsPermissionRequiredDialog: Dialog
        get() {
            val onOkClickListener = DialogInterface.OnClickListener { _, _ ->
                AdbUtils.grantWriteSecureSettingsPermission()
            }
            return AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setMessage(R.string.wait_for_debug_window)
                .setPositiveButton(android.R.string.ok, onOkClickListener)
                .create()
                .apply {
                    setOnShowListener {
                        makeButtonFocused(POSITIVE_BUTTON)
                    }
                }
        }
    private val usbDebuggingRequiredDialog: Dialog
        get() {
            val onOkClickListener = DialogInterface.OnClickListener { _, _ ->
                finish()
            }
            return AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setMessage(R.string.adb_debugging_required)
                .setPositiveButton(android.R.string.ok, onOkClickListener)
                .create()
                .apply {
                    setOnShowListener {
                        makeButtonFocused(POSITIVE_BUTTON)
                    }
                }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AutoBacklightService.start(this, false)
        if (!Utils.isCurrentTVSupported(this)) {
            showDialog(notSupportedTVDialog)
        }
        if (!Utils.isDebuggingEnabled(this)) {
            showDialog(usbDebuggingRequiredDialog)
        }
        if (!Utils.hasPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            showDialog(writeSecureSettingsPermissionRequiredDialog)
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

    private fun showDialog(dialog: Dialog) {
        if (dialogsToShow.isEmpty()) {
            dialog.show()
        }
        dialogsToShow.offer(dialog)
        dialog.setOnDismissListener {
            dialogsToShow.remove(dialog)
            if (!dialogsToShow.isEmpty()) {
                dialogsToShow.peek()?.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AutoBacklightService.stop(this)
    }
}