package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.util.DialogButton
import com.alexrcq.tvpicturesettings.util.setButtonFocused

class ResetToDefaultDialog: DialogFragment(), DialogInterface.OnShowListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.reset_to_default_message)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        alertDialog.setOnShowListener(this)
        return alertDialog
    }

    override fun onShow(dialog: DialogInterface?) {
        val alertDialog = dialog as AlertDialog
        alertDialog.setButtonFocused(DialogButton.POSITIVE_BUTTON)
        alertDialog.getButton(DialogButton.POSITIVE_BUTTON).setOnClickListener {
            onOkClicked()
        }
    }

    private fun onOkClicked() {
        PictureSettings(requireContext()).resetToDefault()
        Toast.makeText(requireContext(), R.string.please_wait, Toast.LENGTH_LONG).show()
        dismiss()
    }

    companion object {
        const val TAG = "ResetToDefaultDialog"
    }
}