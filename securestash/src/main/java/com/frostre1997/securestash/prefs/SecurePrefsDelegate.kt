package com.frostre1997.securestash.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.frostre1997.securestash.crypto.CryptoManager
import com.frostre1997.securestash.crypto.CryptoException
import com.frostre1997.securestash.crypto.KeyMismatchException
import com.frostre1997.securestash.crypto.UnsupportedTypeException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SecurePrefsDelegate<T : Any>(
    private val context: Context,
    private val key: String,
    private val defaultValue: T,
    private val type: Class<T>,
    private val masterKey: MasterKey? = null,
    private val fileName: String = "secure_stash_prefs",
    private val requireUserAuth: Boolean = false,
    private val clearOnMismatch: Boolean = false
) : ReadWriteProperty<Any?, T> {

    private val prefs by lazy {
        val key = masterKey ?: CryptoManager.getMasterKey(context, requireUserAuth)
        EncryptedSharedPreferences.create(
            context,
            fileName,
            key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return try {
            when (type) {
                String::class.java -> prefs.getString(key, defaultValue as? String) as T
                Int::class.java -> prefs.getInt(key, defaultValue as Int) as T
                Boolean::class.java -> prefs.getBoolean(key, defaultValue as Boolean) as T
                Float::class.java -> prefs.getFloat(key, defaultValue as Float) as T
                Long::class.java -> prefs.getLong(key, defaultValue as Long) as T
                Set::class.java -> prefs.getStringSet(key, defaultValue as? Set<String>) as T
                else -> throw UnsupportedTypeException("Unsupported type: ${type.simpleName}")
            } ?: defaultValue
        } catch (e: KeyMismatchException) {
            if (clearOnMismatch) {
                setValue(thisRef, property, defaultValue)
                defaultValue
            } else {
                throw e
            }
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val editor = prefs.edit()
        try {
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Long -> editor.putLong(key, value)
                is Set<*> -> editor.putStringSet(key, value as? Set<String>)
                else -> throw UnsupportedTypeException("Unsupported type: ${value::class.java.simpleName}")
            }
            editor.apply()
        } catch (e: Exception) {
            editor.apply()
            throw CryptoException("Failed to save preference: ${e.message}", e)
        }
    }
}

inline fun <reified T : Any> securePrefs(
    context: Context,
    key: String,
    defaultValue: T,
    fileName: String = "secure_stash_prefs",
    requireUserAuth: Boolean = false,
    clearOnMismatch: Boolean = false
): SecurePrefsDelegate<T> {
    return SecurePrefsDelegate(
        context,
        key,
        defaultValue,
        T::class.java,
        fileName = fileName,
        requireUserAuth = requireUserAuth,
        clearOnMismatch = clearOnMismatch
    )
}
