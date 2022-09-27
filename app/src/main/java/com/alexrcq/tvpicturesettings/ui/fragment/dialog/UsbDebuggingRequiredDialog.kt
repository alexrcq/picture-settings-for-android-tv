package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.util.DialogButton.NEGATIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.DialogButton.POSITIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.setButtonFocused

class UsbDebuggingRequiredDialog : DialogFragment(), DialogInterface.OnShowListener{
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.adb_debugging_required)
            .setPositiveButton(R.string.open_settings, null)
            .setNegativeButton(android.R.string.cancel, null)
            .setOnCancelListener {
                requireActivity().finish()
            }
            .create()
        alertDialog.setOnShowListener(this)
        return alertDialog
    }

    override fun onShow(dialog: DialogInterface?) {
        (dialog as AlertDialog).apply {
            setButtonFocused(POSITIVE_BUTTON)
            getButton(NEGATIVE_BUTTON).setOnClickListener {
                onCancelClicked()
            }
            getButton(POSITIVE_BUTTON).setOnClickListener {
                onOpenSettingsClicked()
            }
        }
    }

    private fun onCancelClicked() {
        requireActivity().finish()
    }

    private fun onOpenSettingsClicked() {
        startActivity(Intent(Settings.ACTION_SETTINGS))
        requireActivity().finish()
    }

    companion object {
        const val TAG = "UsbDebuggingRequiredDialog"
    }
}