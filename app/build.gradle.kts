plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "de.niklasschmitt.locker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "de.niklasschmitt.locker"
        // GLOBAL_ACTION_LOCK_SCREEN (the whole point of this app) only exists from API 28 on -
        // below that the service would install but silently do nothing, so there's no point
        // pretending to support it.
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            isShrinkResources = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// No dependencies on purpose: the app is a bare Activity + AccessibilityService with no UI,
// so it needs nothing beyond the Android platform SDK - no AppCompat, no Material, no core-ktx.
