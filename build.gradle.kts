plugins {
    // Define the plugin versions used across ALL modules
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

// Optional: Common repositories for all modules
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Optional: Clean task (helps JitPack)
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
