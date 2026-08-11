pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // mavenLocal()
    }
}

rootProject.name = "securestash-root"
include(":securestash")

// include(":sample")
