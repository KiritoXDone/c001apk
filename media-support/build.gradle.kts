import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.JavaVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "com.example.c001apk.media"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(project(":image-viewer"))
    implementation(project(":SketchImageViewLoader"))
    testImplementation(libs.junit)
}
