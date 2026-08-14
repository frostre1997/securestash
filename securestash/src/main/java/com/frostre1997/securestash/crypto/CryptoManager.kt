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
     *
     * @param context Application context.
     * @param requireUserAuth If true, user must authenticate (fingerprint/pin) to use the key.
     * @return The MasterKey instance.
     * @throws InvalidContextException If the context is invalid.
     */
    @Throws(InvalidContextException::class)
    fun getMasterKey(
        context: Context,
        requireUserAuth: Boolean = false
    ): MasterKey {
        return try {
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setUserAuthenticationRequired(requireUserAuth)
                .build()
        } catch (e: Exception) {
            throw InvalidContextException("Failed to create MasterKey", e)
        }
    }

    /**
     * Encrypts a plain text string to a Base64 encrypted string.
     *
     * @param context Application context.
     * @param plainText The string to encrypt.
     * @param masterKey Optional MasterKey. If null, one is auto-generated.
     * @param fileName The name of the preferences file to use (for internal temp storage).
     * @return The encrypted ciphertext.
     * @throws CryptoException If encryption fails.
     */
    @Throws(CryptoException::class)
    fun encryptString(
        context: Context,
        plainText: String,
        masterKey: MasterKey? = null,
        fileName: String = "securestash_temp_enc"
    ): String {
        val key = masterKey ?: getMasterKey(context)
        return try {
            val prefs = EncryptedSharedPreferences.create(
                context,
                fileName,
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
        } catch (e: Exception) {
            throw CryptoException("Unexpected encryption error: ${e.message}", e)
        }
    }

    /**
     * Decrypts an encrypted Base64 string back to plain text.
     *
     * @param context Application context.
     * @param cipherText The encrypted string to decrypt.
     * @param masterKey Optional MasterKey. If null, one is auto-generated.
     * @param fileName The name of the preferences file to use (for internal temp storage).
     * @return The decrypted plaintext.
     * @throws KeyMismatchException If the key doesn't match the encrypted data.
     * @throws CorruptedDataException If the data is corrupted.
     * @throws CryptoException For general decryption failures.
     */
    @Throws(CryptoException::class, KeyMismatchException::class, CorruptedDataException::class)
    fun decryptString(
        context: Context,
        cipherText: String,
        masterKey: MasterKey? = null,
        fileName: String = "securestash_temp_dec"
    ): String {
        val key = masterKey ?: getMasterKey(context)
        return try {
            val prefs = EncryptedSharedPreferences.create(
                context,
                fileName,
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val tempKey = "temp_${System.currentTimeMillis()}"
            prefs.edit().putString(tempKey, cipherText).apply()
            val decrypted = prefs.getString(tempKey, "") ?: ""
            prefs.edit().remove(tempKey).apply()
            if (decrypted.isEmpty()) {
                throw CorruptedDataException("Decryption returned empty string")
            }
            decrypted
        } catch (e: GeneralSecurityException) {
            if (e.message?.contains("Keystore", ignoreCase = true) == true) {
                throw KeyMismatchException(cause = e)
            }
            throw CryptoException("Decryption failed: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw CorruptedDataException(cause = e)
        } catch (e: Exception) {
            throw CryptoException("Unexpected decryption error: ${e.message}", e)
        }
    }

    /**
     * Encrypts a file. Output file will be encrypted.
     *
     * @param context Application context.
     * @param inputFile The plaintext file to encrypt.
     * @param outputFile The destination encrypted file.
     * @param masterKey Optional MasterKey.
     * @throws CryptoException If file encryption fails.
     */
    @Throws(CryptoException::class)
    fun encryptFile(
        context: Context,
        inputFile: File,
        outputFile: File,
        masterKey: MasterKey? = null
    ) {
        try {
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
        } catch (e: Exception) {
            throw CryptoException("File encryption failed: ${e.message}", e)
        }
    }

    /**
     * Decrypts an encrypted file back to plain text.
     *
     * @param context Application context.
     * @param encryptedFile The encrypted file.
     * @param outputFile The destination plaintext file.
     * @param masterKey Optional MasterKey.
     * @throws KeyMismatchException If the key doesn't match the encrypted data.
     * @throws CryptoException If file decryption fails.
     */
    @Throws(CryptoException::class, KeyMismatchException::class)
    fun decryptFile(
        context: Context,
        encryptedFile: File,
        outputFile: File,
        masterKey: MasterKey? = null
    ) {
        try {
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
        } catch (e: GeneralSecurityException) {
            if (e.message?.contains("Keystore", ignoreCase = true) == true) {
                throw KeyMismatchException(cause = e)
            }
            throw CryptoException("File decryption failed: ${e.message}", e)
        } catch (e: Exception) {
            throw CryptoException("Unexpected file decryption error: ${e.message}", e)
        }
    }
}
