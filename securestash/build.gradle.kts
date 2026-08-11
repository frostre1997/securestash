plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.frostre1997.securestash"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // AndroidX Security Crypto (handles Keystore & AES)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Core KTX for SharedPreferences
    implementation("androidx.core:core-ktx:1.13.1")
    
    // Kotlin Coroutines (for Flow support)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}

// Publishing configuration for JitPack
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.frostre1997"
            artifactId = "securestash"
            version = "0.10.5"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
