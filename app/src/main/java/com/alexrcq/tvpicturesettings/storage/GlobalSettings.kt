package com.alexrcq.tvpicturesettings.storage

interface GlobalSettings {
    fun putInt(key: String, value: Int)
    fun getInt(key: String): Int
}