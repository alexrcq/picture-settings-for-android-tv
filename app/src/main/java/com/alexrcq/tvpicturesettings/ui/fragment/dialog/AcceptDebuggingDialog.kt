package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.alexrcq.tvpicturesettings.DarkModeManager
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.enableAccessibilityService
import com.alexrcq.tvpicturesettings.requestFocusForced
import kotlinx.coroutines.launch
import timber.log.Timber

class AcceptDebuggingDialog: DialogFragment(), DialogInterface.OnShowListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(requireActivity(), android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.wait_for_debug_window)
            .setPositiveButton(android.R.string.ok, null)
            .setOnCancelListener {
                requireActivity().finish()
            }
            .create()
        alertDialog.setOnShowListener(this@AcceptDebuggingDialog)
        return alertDialog
    }

    private fun onOkClicked() {
        dialog?.hide()
        lifecycleScope.launch {
            with(AdbShell.getInstance(requireActivity())) {
                try {
                    connect()
                } catch (e: Exception) {
                    Timber.e("adb timeout")
                    Toast.makeText(context, getString(R.string.сonnection_timeout), Toast.LENGTH_LONG).show()
                    requireActivity().finish()
                    return@launch
                }
                grantPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                disconnect()
            }
            requireActivity().enableAccessibilityService(DarkModeManager::class.java)
            dismiss()
        }
    }

    override fun onShow(dialog: DialogInterface?) {
        val alertDialog = dialog as AlertDialog
        alertDialog.getButton(BUTTON_POSITIVE).apply {
            setOnClickListener {
                onOkClicked()
            }
            requestFocusForced()
        }
    }

    companion object {
        const val TAG = "AcceptDebuggingDialog"
    }
}