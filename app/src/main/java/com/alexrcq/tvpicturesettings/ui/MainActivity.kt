package com.alexrcq.tvpicturesettings.ui

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.util.DialogButton.POSITIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.Utils
import com.alexrcq.tvpicturesettings.util.makeButtonFocused

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Utils.isCurrentTVSupported(this)) {
            showNotSupportedTVDialog()
        }
        if (!Utils.isDebuggingEnabled(this)) {
            showUsbDebuggingRequiredDialog()
        }
        if (Utils.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            setContentView(R.layout.activity_main)
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    setContentView(R.layout.activity_main)
                } else {
                    showStoragePermissionRequiredDialog()
                }
            }.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        alertDialog.makeButtonFocused(POSITIVE_BUTTON)
    }

    private fun showStoragePermissionRequiredDialog() {
        val onOkClickListener = DialogInterface.OnClickListener { _, _ ->
            finish()
        }
        val alertDialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.storage_permission_required)
            .setPositiveButton(android.R.string.ok, onOkClickListener)
            .create()
        alertDialog.show()
        alertDialog.makeButtonFocused(POSITIVE_BUTTON)
    }

    private fun showUsbDebuggingRequiredDialog() {
        val onOkClickListener = DialogInterface.OnClickListener { _, _ ->
            finish()
        }
        val alertDialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setMessage("Для работы приложения требуется включить отладку по USB в настройках для разработчиков")
            .setPositiveButton(android.R.string.ok, onOkClickListener)
            .create()
        alertDialog.show()
        alertDialog.makeButtonFocused(POSITIVE_BUTTON)
    }
}