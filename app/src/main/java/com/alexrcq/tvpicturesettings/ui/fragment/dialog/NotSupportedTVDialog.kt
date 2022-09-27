package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.util.DialogButton
import com.alexrcq.tvpicturesettings.util.setButtonFocused

class NotSupportedTVDialog: DialogFragment(), DialogInterface.OnShowListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(requireActivity(), android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.not_supported_tv)
            .setPositiveButton(android.R.string.ok, null)
            .setOnCancelListener {
                requireActivity().finish()
            }
            .create()
        alertDialog.setOnShowListener(this@NotSupportedTVDialog)
        return alertDialog
    }

    private fun onOkClicked() {
        requireActivity().finish()
    }

    override fun onShow(dialog: DialogInterface?) {
        val alertDialog = dialog as AlertDialog
        alertDialog.setButtonFocused(DialogButton.POSITIVE_BUTTON)
        alertDialog.getButton(DialogButton.POSITIVE_BUTTON).setOnClickListener {
            onOkClicked()
        }
    }

    companion object {
        const val TAG = "NotSupportedTVDialog"
    }
}