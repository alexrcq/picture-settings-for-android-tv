package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.requestFocusForced
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_AUTO_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_RESET_TO_DEFAULT
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsWrapper

class ResetToDefaultDialog : DialogFragment(), DialogInterface.OnShowListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog =
            AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
                .setMessage(R.string.reset_to_default_message)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        alertDialog.setOnShowListener(this)
        return alertDialog
    }

    override fun onShow(dialog: DialogInterface?) {
        val alertDialog = dialog as AlertDialog
        alertDialog.getButton(BUTTON_POSITIVE).apply {
            setOnClickListener {
                onOkClicked()
            }
        }
        alertDialog.getButton(BUTTON_NEGATIVE).requestFocusForced()
    }

    private fun onOkClicked() {
        Toast.makeText(requireContext(), R.string.please_wait, Toast.LENGTH_LONG).show()
        resetToDefault()
        dismiss()
    }

    private fun resetToDefault() {
        // the same behavior as the system app
        with(GlobalSettingsWrapper(requireContext().contentResolver)) {
            putInt(
                PICTURE_RESET_TO_DEFAULT,
                getInt(PICTURE_RESET_TO_DEFAULT) + 1
            )
            putInt(PICTURE_AUTO_BACKLIGHT, 0)
        }
    }

    companion object {
        const val TAG = "ResetToDefaultDialog"
    }
}