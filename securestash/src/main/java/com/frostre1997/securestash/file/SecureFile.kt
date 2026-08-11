package com.frostre1997.securestash.file

import android.content.Context
import com.frostre1997.securestash.crypto.CryptoManager
import java.io.File

/**
 * Extension function to encrypt a file.
 */
fun File.encrypt(context: Context, outputFile: File) {
    CryptoManager.encryptFile(context, this, outputFile)
}

/**
 * Extension function to decrypt a file.
 */
fun File.decrypt(context: Context, outputFile: File) {
    CryptoManager.decryptFile(context, this, outputFile)
}

/**
 * Extension to encrypt a file in-place (creates a .encrypted version).
 */
fun File.encryptInPlace(context: Context) {
    val output = File(this.parent, "${this.name}.encrypted")
    CryptoManager.encryptFile(context, this, output)
}
