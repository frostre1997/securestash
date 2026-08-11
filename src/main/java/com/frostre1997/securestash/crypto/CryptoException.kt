package com.frostre1997.securestash.crypto

/**
 * Base exception for all SecureStash encryption/decryption failures.
 * 
 * This is a checked-equivalent exception (unchecked in Kotlin) that wraps
 * underlying Android Security exceptions into a clean, meaningful message.
 * 
 * @param message A human-readable error description.
 * @param cause The underlying throwable (e.g., GeneralSecurityException, IOException).
 */
open class CryptoException(
    message: String,
    val cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when the user attempts to decrypt data with the wrong master key.
 * This usually happens when the Keystore has been wiped (e.g., after app uninstall)
 * or when switching between debug/release signing keys.
 */
class KeyMismatchException(
    message: String = "Decryption failed: The encryption key does not match this device.",
    cause: Throwable? = null
) : CryptoException(message, cause)

/**
 * Thrown when encrypted data is corrupted or malformed (e.g., Base64 decoding fails).
 */
class CorruptedDataException(
    message: String = "The encrypted data appears to be corrupted or incomplete.",
    cause: Throwable? = null
) : CryptoException(message, cause)

/**
 * Thrown when a required context is missing or the app is in an invalid state.
 */
class InvalidContextException(
    message: String = "The provided Context is null or not properly initialized.",
    cause: Throwable? = null
) : CryptoException(message, cause)

/**
 * Thrown when trying to encrypt/decrypt an unsupported data type.
 */
class UnsupportedTypeException(
    message: String = "SecureStash does not support this data type.",
    cause: Throwable? = null
) : CryptoException(message, cause)
