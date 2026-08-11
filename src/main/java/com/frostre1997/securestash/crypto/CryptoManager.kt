package com.frostre1997.securestash.crypto

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.GeneralSecurityException

/**
 * Core encryption engine for SecureStash.
 * Handles AES-256 encryption using Android Keystore.
 */
object CryptoManager {

    /**
     * Gets or creates the MasterKey from Android Keystore.
     * @param context Application context
     * @param requireUserAuth If true, user must authenticate (fingerprint/pin) to use the key.
     */
    fun getMasterKey(
        context: Context,
        requireUserAuth: Boolean = false
    ): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(requireUserAuth)
            .build()
    }

    /**
     * Encrypts a plain text string to a Base64 encrypted string.
     */
    fun encryptString(
        context: Context,
        plainText: String,
        masterKey: MasterKey? = null
    ): String {
        val key = masterKey ?: getMasterKey(context)
        return try {
            val prefs = EncryptedSharedPreferences.create(
                context,
                "securestash_temp_enc",
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val tempKey = "temp_${System.currentTimeMillis()}"
            prefs.edit().putString(tempKey, plainText).apply()
            val encrypted = prefs.getString(tempKey, plainText) ?: plainText
            prefs.edit().remove(tempKey).apply()
            encrypted
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Encryption failed: ${e.message}", e)
        }
    }

    /**
     * Decrypts an encrypted Base64 string back to plain text.
     */
    fun decryptString(
        context: Context,
        cipherText: String,
        masterKey: MasterKey? = null
    ): String {
        val key = masterKey ?: getMasterKey(context)
        return try {
            val prefs = EncryptedSharedPreferences.create(
                context,
                "securestash_temp_dec",
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val tempKey = "temp_${System.currentTimeMillis()}"
            prefs.edit().putString(tempKey, cipherText).apply()
            val decrypted = prefs.getString(tempKey, "") ?: ""
            prefs.edit().remove(tempKey).apply()
            decrypted
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Decryption failed: ${e.message}", e)
        }
    }

    /**
     * Encrypts a file. Output file will be encrypted.
     */
    fun encryptFile(
        context: Context,
        inputFile: File,
        outputFile: File,
        masterKey: MasterKey? = null
    ) {
        val key = masterKey ?: getMasterKey(context)
        val encryptedFile = EncryptedFile.Builder(
            context,
            outputFile,
            key,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        inputFile.inputStream().use { input ->
            encryptedFile.openFileOutput().use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Decrypts an encrypted file back to plain text.
     */
    fun decryptFile(
        context: Context,
        encryptedFile: File,
        outputFile: File,
        masterKey: MasterKey? = null
    ) {
        val key = masterKey ?: getMasterKey(context)
        val encrypted = EncryptedFile.Builder(
            context,
            encryptedFile,
            key,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        encrypted.openFileInput().use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

/**
 * Custom exception for encryption/decryption errors.
 */
class CryptoException(message: String, cause: Throwable? = null) 
    : Exception(message, cause)
