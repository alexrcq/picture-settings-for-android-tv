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
import com.alexrcq.tvpicturesettings.util.AdbUtils
import com.alexrcq.tvpicturesettings.util.DialogButton.POSITIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.Utils
import com.alexrcq.tvpicturesettings.util.makeButtonFocused
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : FragmentActivity() {

    private var dialogsToShow: LinkedBlockingQueue<Dialog> = LinkedBlockingQueue()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Utils.isCurrentTVSupported(this)) {
            showDialog(createNotSupportedTVDialog())
        }
        if (!Utils.isDebuggingEnabled(this)) {
            showDialog(createUsbDebuggingRequiredDialog())
        }
        if (!Utils.hasPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            showDialog(createWriteSecureSettingsPermissionRequiredDialog())
        }
        setContentView(R.layout.activity_main)
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

    private fun createNotSupportedTVDialog(): Dialog {
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

    private fun createWriteSecureSettingsPermissionRequiredDialog(): Dialog {
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

    private fun createUsbDebuggingRequiredDialog(): Dialog {
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
}