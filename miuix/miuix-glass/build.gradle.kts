import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
}

android {
    namespace = "top.yukonga.miuix.kmp.glass"
    compileSdkVersion("android-37.0")
    buildToolsVersion = "36.0.0"
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(project(":miuix:blur"))
                api(project(":miuix:material"))
                api(project(":miuix:ui"))
                implementation(libs.androidx.compose.foundation)
                implementation(libs.androidx.navigationevent.compose)
            }
        }
    }
}
