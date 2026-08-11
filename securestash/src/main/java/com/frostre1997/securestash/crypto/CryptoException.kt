package com.frostre1997.securestash.crypto

open class CryptoException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class KeyMismatchException(
    message: String = "Decryption failed: The encryption key does not match this device.",
    cause: Throwable? = null
) : CryptoException(message, cause)

class CorruptedDataException(
    message: String = "The encrypted data appears to be corrupted or incomplete.",
    cause: Throwable? = null
) : CryptoException(message, cause)

class InvalidContextException(
    message: String = "The provided Context is null or not properly initialized.",
    cause: Throwable? = null
) : CryptoException(message, cause)

class UnsupportedTypeException(
    message: String = "SecureStash does not support this data type.",
    cause: Throwable? = null
) : CryptoException(message, cause)
