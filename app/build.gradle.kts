plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "app.alquran.kareem"
    compileSdk = 36
    defaultConfig {
        applicationId = "app.alquran.kareem"
        minSdk = 26
        targetSdk = 35
        // Stable release identity: all future signed updates increment versionCode.
        versionCode = 100
        versionName = "1.0.0"
    }
    buildTypes {
        debug {
            // Internal CI build only; do not distribute debug-signed APKs.
        }
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // Kotlin 2.3+ no longer allows the deprecated kotlinOptions.jvmtarget syntax.
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(platform("androidx.compose:compose-bom:2026.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
