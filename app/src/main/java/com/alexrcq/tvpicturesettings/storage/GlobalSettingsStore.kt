package com.alexrcq.tvpicturesettings.storage

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

open class GlobalSettingsStore(private val contentResolver: ContentResolver) : PreferenceDataStore() {

    override fun putString(key: String, value: String?) {
        try {
            Timber.d("putString %s %s", key, value)
            Settings.Global.putString(contentResolver, key, value)
        } catch (e: SecurityException) {
            Timber.w("$key: WRITE_SECURE_PERMISSION required")
        }
    }

    override fun putInt(key: String, value: Int) {
        try {
            Timber.d("putInt %s %d", key, value)
            Settings.Global.putInt(contentResolver, key, value)
        } catch (e: SecurityException) {
            Timber.w("$key: WRITE_SECURE_PERMISSION required")
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        putInt(key, if (value) 1 else 0)
    }

    override fun getString(key: String, defValue: String?): String? {
        val value = Settings.Global.getString(contentResolver, key) ?: defValue
        Timber.d("getString %s: %s", key, value)
        return value
    }

    override fun getInt(key: String, defValue: Int): Int {
        val value = Settings.Global.getInt(contentResolver, key, defValue)
        Timber.d("getInt %s: %d", key, value)
        return value
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean = getInt(key, if (defValue) 1 else 0) == 1

    fun getBooleanFlow(key: String, defValue: Boolean): Flow<Boolean> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(getBoolean(key, defValue))
            }
        }
        trySend(getBoolean(key, defValue))
        contentResolver.registerContentObserver(Settings.Global.getUriFor(key), false, observer)
        awaitClose { contentResolver.unregisterContentObserver(observer) }
    }
}