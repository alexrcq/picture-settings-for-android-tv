package com.alexrcq.tvpicturesettings.domain

interface WhiteBalanceSettings {
    fun setWhiteBalance(redGain: Int, greenGain: Int, blueGain: Int)

    fun resetWhiteBalance()
}