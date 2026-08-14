# securestash

[![](https://jitpack.io/v/frostre1997/securestash.svg)](https://jitpack.io/#frostre1997/securestash)
[![Android](https://img.shields.io/badge/Android-21%2B-brightgreen?style=flat-square&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-GPL-blue.svg?style=flat-square)](LICENSE)

---

**securestash** is a lightweight Kotlin library for Android that **encrypts your app's data without the boilerplate**.

It takes the complexity out of Android's encryption APIs (like `EncryptedSharedPreferences` and `EncryptedFile`) and wraps them into simple, type-safe one-liners.

---

## What does it do?

securestash handles three main tasks:

| Task | What it does |
| :--- | :--- |
| **Encrypt Preferences** | Stores strings, integers, booleans, etc. in `EncryptedSharedPreferences` using Kotlin delegates. No `getString()`/`putString()` boilerplate. |
| **Encrypt Files** | Encrypts or decrypts any file (PDF, image, video) with a single extension function. |
| **Reactive Updates** | Observes preference changes with `Flow` – perfect for Compose or LiveData. |

All encryption is handled automatically using **Android Keystore** with **AES-256** encryption.

---

## Why securestash?

| Without SecureStash | With SecureStash |
| :--- | :--- |
| 15 lines of `EncryptedSharedPreferences` setup | `var token by securePrefs(...)` |
| Manual `try/catch` for encryption errors | Clean exception hierarchy |
| Separate encryption for files | `.encryptInPlace()` extension function |
| Manual observation of changes | `securePrefsFlow()` for reactive UI |

---

## Example Use Cases

- **Auth Tokens** – Store JWT tokens securely without risking plaintext exposure.
- **User Preferences** – Save user settings, theme choices, or app state.
- **Sensitive Files** – Encrypt downloaded PDFs, images, or database backups.
- **Payment Data** – Store credit card hints or PINs behind biometric authentication.

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
    implementation 'com.github.frostre1997:securestash:v0.11.0'
}
```

---

## Quick Start

### 1. Encrypted Preferences (Delegate)

Store and retrieve encrypted values with Kotlin delegates. No more getString() or putString() boilerplate.

```kotlin
import com.frostre1997.securestash.prefs.securePrefs

class MainActivity : AppCompatActivity() {

    // Define your encrypted preferences as class properties
    var authToken: String by securePrefs(this, "auth_token", "guest")
    var loginCount: Int by securePrefs(this, "login_count", 0)
    var isPro: Boolean by securePrefs(this, "is_pro", false)

    fun example() {
        // Reading a value (auto-decrypted)
        Log.d("Token", "Current token: $authToken") // Output: "Current token: guest"

        // Writing a value (auto-encrypted and saved)
        authToken = "jwt_12345_abcde"
        Log.d("Token", "New token: $authToken") // Output: "New token: jwt_12345_abcde"

        // Works with other types too
        loginCount = 5
        isPro = true
    }
}
```

---

### 2. Encrypt / Decrypt Files

Encrypt any file (PDF, image, video) with one line of code.

```kotlin
import com.frostre1997.securestash.file.encryptInPlace
import com.frostre1997.securestash.file.decryptInPlace
import java.io.File

class MainActivity : AppCompatActivity() {

    fun encryptDocument() {
        val file = File(cacheDir, "report.pdf")

        // Encrypt the file (creates report.pdf.encrypted)
        file.encryptInPlace(context)
        Log.d("SecureStash", "File encrypted: ${file.exists()}") // Output: "File encrypted: true"

        // Decrypt it back to the original
        val encryptedFile = File(cacheDir, "report.pdf.encrypted")
        encryptedFile.decryptInPlace(context)
        Log.d("SecureStash", "File decrypted: ${File(cacheDir, "report.pdf").exists()}") // Output: "File decrypted: true"
    }
}
```

---

### 3. Reactive Flows (For Compose / LiveData)

Observe preference changes in real-time. Perfect for updating UI automatically.

```kotlin
import com.frostre1997.securestash.prefs.securePrefsFlow
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            // Listen to changes on the "auth_token" preference
            securePrefsFlow<String>(
                context = this@MainActivity,
                key = "auth_token",
                defaultValue = "guest",
                type = String::class.java
            ).collect { token ->
                // This runs every time the token changes
                textView.text = "Token: $token"
                Log.d("SecureStash", "Token updated: $token")
            }
        }
    }

    fun updateToken() {
        // Update the token from anywhere in your app
        var token: String by securePrefs(this, "auth_token", "guest")
        token = "new_jwt_token_456"
        // The Flow above will automatically emit the new value!
    }
}
```

---

### 4. Encrypt / Decrypt Strings Directly

For cases where you need to encrypt a single string value (like an API key or secret message).

```kotlin
import com.frostre1997.securestash.crypto.CryptoManager

class MainActivity : AppCompatActivity() {

    fun encryptAndDecryptString() {
        val secret = "MySuperSecretAPIKey"

        // Encrypt the string
        val encrypted = CryptoManager.encryptString(this, secret)
        Log.d("SecureStash", "Encrypted: $encrypted")
        // Output: "Encrypted: AES256:SIV:abc123def456ghi789..."

        // Decrypt it back
        val decrypted = CryptoManager.decryptString(this, encrypted)
        Log.d("SecureStash", "Decrypted: $decrypted")
        // Output: "Decrypted: MySuperSecretAPIKey"
    }
}
```

---

### 5. Real-World Example: Login Flow

A complete example showing how SecureStash simplifies authentication.

```kotlin
class LoginViewModel(private val context: Context) {

    // Define your preferences
    private var authToken: String by securePrefs(context, "auth_token", "")
    private var userId: String by securePrefs(context, "user_id", "")
    private var isLoggedIn: Boolean by securePrefs(context, "is_logged_in", false)

    fun login(username: String, password: String) {
        // Simulate API call
        val success = authenticateUser(username, password)

        if (success) {
            // Automatically encrypted and saved
            authToken = "jwt_${System.currentTimeMillis()}"
            userId = username
            isLoggedIn = true

            Log.d("Login", "User logged in. Token: $authToken, User: $userId")
        } else {
            Log.d("Login", "Login failed")
        }
    }

    fun logout() {
        // Clear all preferences with one line each
        authToken = ""
        userId = ""
        isLoggedIn = false

        Log.d("Login", "User logged out")
    }

    fun checkLoginStatus(): Boolean {
        return isLoggedIn
    }

    private fun authenticateUser(username: String, password: String): Boolean {
        // Your actual authentication logic here
        return username == "admin" && password == "password"
    }
}
```

---

### 6. Summary: All Types Supported

```kotlin
// String
var name: String by securePrefs(this, "name", "Guest")

// Int
var age: Int by securePrefs(this, "age", 18)

// Boolean
var darkMode: Boolean by securePrefs(this, "dark_mode", false)

// Float
var rating: Float by securePrefs(this, "rating", 4.5f)

// Long
var timestamp: Long by securePrefs(this, "timestamp", System.currentTimeMillis())

// Set<String>
var favoriteColors: Set<String> by securePrefs(
    this, 
    "favorites", 
    setOf("Red", "Blue", "Green")
)
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
