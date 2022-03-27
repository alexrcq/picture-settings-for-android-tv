package com.alexrcq.tvpicturesettings.util

import android.app.AlertDialog

fun AlertDialog.makeButtonFocused(@DialogButton button: Int) {
    this.getButton(button).apply {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }
}