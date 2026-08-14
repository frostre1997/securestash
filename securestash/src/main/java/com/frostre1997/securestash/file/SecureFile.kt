package com.frostre1997.securestash.file

import android.content.Context
import com.frostre1997.securestash.crypto.CryptoManager
import com.frostre1997.securestash.crypto.CryptoException
import com.frostre1997.securestash.crypto.KeyMismatchException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Throws(CryptoException::class)
fun File.encrypt(context: Context, outputFile: File) {
    CryptoManager.encryptFile(context, this, outputFile)
}

@Throws(CryptoException::class, KeyMismatchException::class)
fun File.decrypt(context: Context, outputFile: File) {
    CryptoManager.decryptFile(context, this, outputFile)
}

@Throws(CryptoException::class)
fun File.encryptInPlace(context: Context) {
    val output = File(this.parent, "${this.name}.encrypted")
    CryptoManager.encryptFile(context, this, output)
}

@Throws(CryptoException::class, KeyMismatchException::class)
fun File.decryptInPlace(context: Context) {
    val output = File(this.parent, this.name.removeSuffix(".encrypted"))
    CryptoManager.decryptFile(context, this, output)
}

@Throws(CryptoException::class)
suspend fun File.encryptAsync(context: Context, outputFile: File) {
    withContext(Dispatchers.IO) {
        CryptoManager.encryptFile(context, this@encryptAsync, outputFile)
    }
}

@Throws(CryptoException::class, KeyMismatchException::class)
suspend fun File.decryptAsync(context: Context, outputFile: File) {
    withContext(Dispatchers.IO) {
        CryptoManager.decryptFile(context, this@decryptAsync, outputFile)
    }
}

@Throws(CryptoException::class)
suspend fun File.encryptInPlaceAsync(context: Context) {
    withContext(Dispatchers.IO) {
        val output = File(this@encryptInPlaceAsync.parent, "${this@encryptInPlaceAsync.name}.encrypted")
        CryptoManager.encryptFile(context, this@encryptInPlaceAsync, output)
    }
}

@Throws(CryptoException::class, KeyMismatchException::class)
suspend fun File.decryptInPlaceAsync(context: Context) {
    withContext(Dispatchers.IO) {
        val output = File(this@decryptInPlaceAsync.parent, this@decryptInPlaceAsync.name.removeSuffix(".encrypted"))
        CryptoManager.decryptFile(context, this@decryptInPlaceAsync, output)
    }
}
