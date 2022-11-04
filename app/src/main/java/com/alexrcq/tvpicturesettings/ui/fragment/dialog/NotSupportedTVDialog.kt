package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.util.requestFocusForced

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
        alertDialog.getButton(BUTTON_POSITIVE).apply {
            setOnClickListener {
                onOkClicked()
            }
            requestFocusForced()
        }
    }

    companion object {
        const val TAG = "NotSupportedTVDialog"
    }
}