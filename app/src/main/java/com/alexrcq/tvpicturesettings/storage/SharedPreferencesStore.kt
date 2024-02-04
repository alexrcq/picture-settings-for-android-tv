package com.alexrcq.tvpicturesettings.storage

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferencesStore(private val sharedPreferences: SharedPreferences) {

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String, defValue: T): T {
        val value = when (defValue) {
            is String -> sharedPreferences.getString(key, defValue) as T
            is Int -> sharedPreferences.getInt(key, defValue) as T
            is Boolean -> sharedPreferences.getBoolean(key, defValue) as T
            is Float -> sharedPreferences.getFloat(key, defValue) as T
            is Long -> sharedPreferences.getLong(key, defValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
        Timber.d("get '$key', value: $value, defValue: $defValue")
        return value
    }

    fun <T : Any> put(key: String, value: T) {
        Timber.d("put $key = $value")
        sharedPreferences.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }

    fun <T : Any> preference(key: String, defValue: T): ReadWriteProperty<Any?, T> =
        object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T = get(key, defValue)

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit = put(key, value)
        }

    fun <T: Any> preferenceFlow(key: String, defValue: T) = callbackFlow {
        val listener = OnSharedPreferenceChangeListener { _, changedPrefKey ->
            if (key == changedPrefKey) {
                trySend(get(changedPrefKey, defValue))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        send(get(key, defValue))
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}