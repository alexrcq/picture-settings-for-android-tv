package com.alexrcq.tvpicturesettings.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.alexrcq.tvpicturesettings.R

class LoadingDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireActivity(), R.style.Theme_AppTheme).setView(R.layout.loading_dialog).create()

    override fun onCancel(dialog: DialogInterface) {
        requireActivity().finish()
    }

    companion object {
        const val TAG = "LoadingDialog"
    }
}