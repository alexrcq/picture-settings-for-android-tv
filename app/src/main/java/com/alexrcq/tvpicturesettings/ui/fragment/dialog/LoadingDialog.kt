package com.alexrcq.tvpicturesettings.ui.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.alexrcq.tvpicturesettings.R

class LoadingDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity(), R.style.Theme_AppTheme)
            .setView(R.layout.loading_dialog)
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    companion object {
        const val TAG = "LoadingDialog"
    }
}