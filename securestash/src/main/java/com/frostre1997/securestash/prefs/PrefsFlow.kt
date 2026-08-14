package com.frostre1997.securestash.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.frostre1997.securestash.crypto.CryptoManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T> securePrefsFlow(
    context: Context,
    key: String,
    defaultValue: T,
    type: Class<T>,
    fileName: String = "secure_stash_prefs",
    requireUserAuth: Boolean = false
): Flow<T> = callbackFlow {
    val masterKey = CryptoManager.getMasterKey(context, requireUserAuth)
    val prefs = EncryptedSharedPreferences.create(
        context,
        fileName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun emitCurrent() {
        val value = when (type) {
            String::class.java -> prefs.getString(key, defaultValue as? String) as T
            Int::class.java -> prefs.getInt(key, defaultValue as Int) as T
            Boolean::class.java -> prefs.getBoolean(key, defaultValue as Boolean) as T
            Float::class.java -> prefs.getFloat(key, defaultValue as Float) as T
            Long::class.java -> prefs.getLong(key, defaultValue as Long) as T
            else -> defaultValue
        }
        trySend(value)
    }
    emitCurrent()

    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == key) {
            emitCurrent()
        }
    }
    prefs.registerOnSharedPreferenceChangeListener(listener)

    awaitClose {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
