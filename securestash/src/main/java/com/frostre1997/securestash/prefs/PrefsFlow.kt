package com.frostre1997.securestash.prefs
import android.content.SharedPreferences
import androidx.security.crypto.MasterKey

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import com.frostre1997.securestash.crypto.CryptoManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Creates a Flow that emits the current value and updates whenever the preference changes.
 * Perfect for Compose UI.
 */
fun <T> securePrefsFlow(
    context: Context,
    key: String,
    defaultValue: T,
    type: Class<T>,
    masterKey: MasterKey? = null
): Flow<T> = callbackFlow {
    val key = masterKey ?: CryptoManager.getMasterKey(context)
    val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_stash_prefs",
        key,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // Emit initial value
    fun emitCurrent() {
        val value = when (type) {
            String::class.java -> prefs.getString(key, defaultValue as? String) as T
            Int::class.java -> prefs.getInt(key, defaultValue as Int) as T
            Boolean::class.java -> prefs.getBoolean(key, defaultValue as Boolean) as T
            else -> defaultValue
        }
        trySend(value)
    }
    emitCurrent()

    // Listen to changes
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
