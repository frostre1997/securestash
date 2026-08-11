package com.frostre1997.securestash.file

import android.content.Context
import com.frostre1997.securestash.crypto.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Encrypt this file and save it as a new file.
 *
 * @param context The Android Context.
 * @param outputFile The destination encrypted file.
 * @throws CryptoException If encryption fails.
 */
@Throws(CryptoException::class)
fun File.encrypt(context: Context, outputFile: File) {
    CryptoManager.encryptFile(context, this, outputFile)
}

/**
 * Decrypt this file and save it as a new file.
 *
 * @param context The Android Context.
 * @param outputFile The destination plaintext file.
 * @throws KeyMismatchException If the key doesn't match.
 * @throws CryptoException If decryption fails.
 */
@Throws(CryptoException::class, KeyMismatchException::class)
fun File.decrypt(context: Context, outputFile: File) {
    CryptoManager.decryptFile(context, this, outputFile)
}

/**
 * Encrypt this file in-place, creating [filename].encrypted.
 *
 * @param context The Android Context.
 * @throws CryptoException If encryption fails.
 */
@Throws(CryptoException::class)
fun File.encryptInPlace(context: Context) {
    val output = File(this.parent, "${this.name}.encrypted")
    CryptoManager.encryptFile(context, this, output)
}

/**
 * Decrypt an encrypted file in-place, removing the .encrypted extension.
 *
 * @param context The Android Context.
 * @throws KeyMismatchException If the key doesn't match.
 * @throws CryptoException If decryption fails.
 */
@Throws(CryptoException::class, KeyMismatchException::class)
fun File.decryptInPlace(context: Context) {
    val output = File(this.parent, this.name.removeSuffix(".encrypted"))
    CryptoManager.decryptFile(context, this, output)
}

// ========== ASYNC VERSIONS (Coroutines) ==========

/**
 * Encrypt this file asynchronously on the IO dispatcher.
 *
 * @param context The Android Context.
 * @param outputFile The destination encrypted file.
 * @throws CryptoException If encryption fails.
 */
suspend fun File.encryptAsync(context: Context, outputFile: File) {
    withContext(Dispatchers.IO) {
        CryptoManager.encryptFile(context, this@encryptAsync, outputFile)
    }
}

/**
 * Decrypt this file asynchronously on the IO dispatcher.
 *
 * @param context The Android Context.
 * @param outputFile The destination plaintext file.
 * @throws KeyMismatchException If the key doesn't match.
 * @throws CryptoException If decryption fails.
 */
suspend fun File.decryptAsync(context: Context, outputFile: File) {
    withContext(Dispatchers.IO) {
        CryptoManager.decryptFile(context, this@decryptAsync, outputFile)
    }
}

/**
 * Encrypt this file in-place asynchronously on the IO dispatcher.
 * Creates [filename].encrypted.
 *
 * @param context The Android Context.
 * @throws CryptoException If encryption fails.
 */
suspend fun File.encryptInPlaceAsync(context: Context) {
    withContext(Dispatchers.IO) {
        val output = File(this@encryptInPlaceAsync.parent, "${this@encryptInPlaceAsync.name}.encrypted")
        CryptoManager.encryptFile(context, this@encryptInPlaceAsync, output)
    }
}

/**
 * Decrypt an encrypted file in-place asynchronously on the IO dispatcher.
 * Removes the .encrypted extension.
 *
 * @param context The Android Context.
 * @throws KeyMismatchException If the key doesn't match.
 * @throws CryptoException If decryption fails.
 */
suspend fun File.decryptInPlaceAsync(context: Context) {
    withContext(Dispatchers.IO) {
        val output = File(this@decryptInPlaceAsync.parent, this@decryptInPlaceAsync.name.removeSuffix(".encrypted"))
        CryptoManager.decryptFile(context, this@decryptInPlaceAsync, output)
    }
}
