package com.frostre1997.securestash.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.yourname.securestash.crypto.CryptoManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A property delegate that stores values securely in EncryptedSharedPreferences.
 * Usage:
 *   var token: String by securePrefs(context, "token", "default")
 *   var count: Int by securePrefs(context, "count", 0)
 */
class SecurePrefsDelegate<T>(
    private val context: Context,
    private val key: String,
    private val defaultValue: T,
    private val type: Class<T>,
    private val masterKey: MasterKey? = null
) : ReadWriteProperty<Any?, T> {

    private val prefs by lazy {
        val key = masterKey ?: CryptoManager.getMasterKey(context)
        EncryptedSharedPreferences.create(
            context,
            "secure_stash_prefs",
            key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (type) {
            String::class.java -> prefs.getString(key, defaultValue as? String) as T
            Int::class.java -> prefs.getInt(key, defaultValue as Int) as T
            Boolean::class.java -> prefs.getBoolean(key, defaultValue as Boolean) as T
            Float::class.java -> prefs.getFloat(key, defaultValue as Float) as T
            Long::class.java -> prefs.getLong(key, defaultValue as Long) as T
            Set::class.java -> prefs.getStringSet(key, defaultValue as? Set<String>) as T
            else -> throw IllegalArgumentException("Unsupported type: ${type.simpleName}")
        } ?: defaultValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val editor = prefs.edit()
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            is Set<*> -> editor.putStringSet(key, value as? Set<String>)
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java.simpleName}")
        }
        editor.apply()
    }
}

/**
 * Top-level function to create a secure preference delegate.
 */
inline fun <reified T> securePrefs(
    context: Context,
    key: String,
    defaultValue: T,
    masterKey: MasterKey? = null
): SecurePrefsDelegate<T> {
    return SecurePrefsDelegate(
        context,
        key,
        defaultValue,
        T::class.java,
        masterKey
    )
}
