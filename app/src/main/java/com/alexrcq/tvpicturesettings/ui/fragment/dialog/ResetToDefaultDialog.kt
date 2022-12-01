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
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ResetToDefaultDialog: DialogFragment(), DialogInterface.OnShowListener {

    @Inject lateinit var pictureSettings: PictureSettings

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
        alertDialog.getButton(BUTTON_POSITIVE).apply {
            setOnClickListener {
                onOkClicked()
            }
        }
        alertDialog.getButton(BUTTON_NEGATIVE).requestFocusForced()
    }

    private fun onOkClicked() {
        pictureSettings.resetToDefault()
        Toast.makeText(requireContext(), R.string.please_wait, Toast.LENGTH_LONG).show()
        dismiss()
    }

    companion object {
        const val TAG = "ResetToDefaultDialog"
    }
}