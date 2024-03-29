package com.alexrcq.tvpicturesettings.adblib

import android.util.Base64
import com.tananaev.adblib.AdbBase64

class AndroidBase64 : AdbBase64 {
    override fun encodeToString(data: ByteArray): String = Base64.encodeToString(data, Base64.NO_WRAP)
}