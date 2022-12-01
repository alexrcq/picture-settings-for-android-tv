package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.requestFocusForced

class UsbDebuggingRequiredDialog : DialogFragment(), DialogInterface.OnShowListener{
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.adb_debugging_required)
            .setPositiveButton(R.string.open_settings, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        alertDialog.setOnShowListener(this)
        return alertDialog
    }

    override fun onShow(dialog: DialogInterface?) {
        val alertDialog = dialog as AlertDialog
        alertDialog.getButton(BUTTON_POSITIVE).apply {
            setOnClickListener {
                onOpenSettingsClicked()
            }
            requestFocusForced()
        }
        alertDialog.getButton(BUTTON_NEGATIVE).setOnClickListener {
            onCancelClicked()
        }
    }

    private fun onCancelClicked() {
        requireActivity().finish()
    }

    private fun onOpenSettingsClicked() {
        startActivity(Intent(Settings.ACTION_SETTINGS))
        requireActivity().finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        requireActivity().finish()
    }

    companion object {
        const val TAG = "UsbDebuggingRequiredDialog"
    }
}