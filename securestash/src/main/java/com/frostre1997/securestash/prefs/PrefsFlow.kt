package com.frostre1997.securestash.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.frostre1997.securestash.crypto.CryptoManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Creates a Flow that emits the current value and updates whenever the preference changes.
 * Perfect for Compose UI or LiveData integration.
 *
 * @param context The Android Context.
 * @param key The unique identifier for this preference.
 * @param defaultValue The fallback value if the key does not exist.
 * @param type The class type of the value.
 * @param fileName The name of the encrypted preferences file (default: "secure_stash_prefs").
 * @param requireUserAuth If true, reading requires biometric/pin authentication.
 * @return A Flow<T> that emits the current value and subsequent updates.
 */
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
