package com.alexrcq.tvpicturesettings.platform.android

import android.annotation.SuppressLint

@SuppressLint("PrivateApi")
object SystemProperties {

    private val getLongMethod by lazy {
        Class.forName("android.os.SystemProperties")
            .getMethod("getLong", String::class.java, Long::class.javaPrimitiveType)
    }

    private val getMethod by lazy {
        Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long = try {
        getLongMethod.invoke(null, key, defaultValue) as Long
    } catch (e: Exception) {
        defaultValue
    }

    fun containsKey(key: String): Boolean = try {
        (getMethod.invoke(null, key) as String).isNotEmpty()
    } catch (e: Exception) {
        false
    }
}