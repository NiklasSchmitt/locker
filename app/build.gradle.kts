import com.android.build.api.variant.impl.VariantOutputImpl
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Single source of truth for both defaultConfig and the release APK's output filename below,
// so the two can never drift apart (the F-Droid Binaries: URL depends on both matching).
val appVersionCode = 1
val appVersionName = "1.0.1"

// Release signing: reads from `keystore.properties` (git-ignored, for local builds) or from
// SIGNING_* environment variables (for the release GitHub Action). Neither is present for a
// plain checkout - e.g. F-Droid building from source - so the release build type simply stays
// unsigned in that case instead of failing.
val keystoreProperties = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

fun signingValue(propertyName: String, envName: String): String? =
    keystoreProperties.getProperty(propertyName) ?: System.getenv(envName)

val releaseStoreFile = signingValue("storeFile", "SIGNING_STORE_FILE")

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
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = signingValue("storePassword", "SIGNING_STORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "SIGNING_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            isShrinkResources = true
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Name the release APK "Locker-<versionName>.apk" (matches the %v placeholder used in the
// F-Droid Binaries: URL) instead of AGP's default "app-release.apk".
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                output.outputFileName.set("Locker-$appVersionName.apk")
            }
        }
    }
}

// No dependencies on purpose: the app is a bare Activity + AccessibilityService with no UI,
// so it needs nothing beyond the Android platform SDK - no AppCompat, no Material, no core-ktx.
