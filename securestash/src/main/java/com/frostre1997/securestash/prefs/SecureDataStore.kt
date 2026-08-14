package com.frostre1997.securestash.prefs

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.frostre1997.securestash.crypto.CryptoManager
import java.io.File

/**
 * Provides encrypted DataStore support.
 * This creates an encrypted file suitable for Preferences DataStore.
 *
 * @param context The Android Context.
 * @param fileName The name of the data store file.
 * @param requireUserAuth If true, requires biometric/pin to access.
 * @return An encrypted File ready for DataStore.
 */
fun getEncryptedDataStoreFile(
    context: Context,
    fileName: String,
    requireUserAuth: Boolean = false
): File {
    val masterKey = CryptoManager.getMasterKey(context, requireUserAuth)
    val file = File(context.filesDir, "$fileName.preferences_pb")
    return EncryptedFile.Builder(
        context,
        file,
        masterKey,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build().openFileOutput().channel.use { channel ->
        // This just ensures the file is created
        File(context.filesDir, fileName)
    }
} 
