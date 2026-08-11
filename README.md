# securestash

[![](https://jitpack.io/v/frostre1997/securestash.svg)](https://jitpack.io/#frostre1997/securestash)
[![Android](https://img.shields.io/badge/Android-21%2B-brightgreen?style=flat-square&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

SecureStash is a lightweight Kotlin library for Android that adds encrypted SharedPreferences, file encryption, and reactive flows with minimal boilerplate.

---

## Features

- Type-safe encrypted preferences with Kotlin delegates
- One-line file encryption and decryption
- Reactive Flow support for observing preference changes
- Automatic Android Keystore management (AES-256)
- Clear exception hierarchy for graceful error handling
- Supports String, Int, Boolean, Float, Long, and Set<String>

---

## Installation

### Step 1: Add JitPack to your project

In your root `settings.gradle` or `settings.gradle.kts`:

```groovy
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the dependency

In your `app/build.gradle` or `app/build.gradle.kts`:

```groovy
dependencies {
    implementation 'com.github.frostre1997:securestash:v0.10.0'
}
```

---

## Quick Start

### 1. Encrypted Preferences (Delegate)

```kotlin
import com.frostre1997.securestash.prefs.securePrefs

class MainActivity : AppCompatActivity() {

    var authToken: String by securePrefs(this, "auth_token", "guest")
    var loginCount: Int by securePrefs(this, "login_count", 0)
    var isPro: Boolean by securePrefs(this, "is_pro", false)

    fun login() {
        authToken = "new_jwt_token_12345" // Auto-encrypted and saved
        loginCount = loginCount + 1
    }
}
```

### 2. Encrypt / Decrypt Files

```kotlin
import com.frostre1997.securestash.file.encryptInPlace
import com.frostre1997.securestash.file.decryptInPlace

val file = File(cacheDir, "report.pdf")

// Encrypt the file (creates report.pdf.encrypted)
file.encryptInPlace(context)

// Decrypt it back
file.decryptInPlace(context)
```

### 3. Reactive Flows (For Compose / LiveData)

```kotlin
import com.frostre1997.securestash.prefs.securePrefsFlow

lifecycleScope.launch {
    securePrefsFlow<String>(this@MainActivity, "auth_token", "guest", String::class.java)
        .collect { token ->
            textView.text = "Current token: $token"
        }
}
```

### 4. Encrypt / Decrypt Strings Directly

```kotlin
import com.frostre1997.securestash.crypto.CryptoManager

val secret = "MySuperSecretAPIKey"
val encrypted = CryptoManager.encryptString(context, secret)
val decrypted = CryptoManager.decryptString(context, encrypted)

println(decrypted) // Outputs: MySuperSecretAPIKey
```

---

## Exception Handling

SecureStash provides specific exception types for clean error handling:

```kotlin
import com.frostre1997.securestash.crypto.CryptoException
import com.frostre1997.securestash.crypto.KeyMismatchException
import com.frostre1997.securestash.crypto.CorruptedDataException

try {
    val decrypted = CryptoManager.decryptString(context, corruptedData)
} catch (e: KeyMismatchException) {
    // The encryption key does not match this device
    // This usually happens after app reinstall or debug/release key mismatch
    logoutUser()
} catch (e: CorruptedDataException) {
    // The encrypted data is corrupted or malformed
    showError("Data corrupted, please try again.")
} catch (e: CryptoException) {
    // Generic encryption/decryption error
    Log.e("SecureStash", "Encryption failed", e)
}
```

---

## Requirements

- Minimum SDK: 21 (Android 5.0)
- Target SDK: 34
- Kotlin 1.9.22 or higher

---

## Proguard / R8

If you are using ProGuard or R8, the library includes its own consumer ProGuard rules. No additional configuration is required.

---

## Contributing

Contributions are welcome. Please open an issue or submit a pull request on GitHub.

---

## License

This project is licensed under the MIT License. See the [LICENSE](https://github.com/frostre1997/securestash/blob/main/LICENSE) file for details.

---

## Links

- GitHub: https://github.com/frostre1997/securestash
- JitPack: https://jitpack.io/#frostre1997/securestash
- Issues: https://github.com/frostre1997/securestash/issues
