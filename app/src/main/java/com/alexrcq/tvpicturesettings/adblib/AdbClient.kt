package com.alexrcq.tvpicturesettings.adblib

import java.io.File

private const val LOCAL_HOST = "127.0.0.1"
private const val DEFAULT_PORT = 5555

interface AdbClient {
    suspend fun connect(host: String = LOCAL_HOST, port: Int = DEFAULT_PORT)
    suspend fun execute(command: String)
    suspend fun grantPermissions(permissions: List<String>)
    suspend fun captureScreen(saveDir: File)
    fun disconnect()
}