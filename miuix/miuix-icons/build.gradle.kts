import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
}

android {
    namespace = "top.yukonga.miuix.kmp.icon"
    compileSdkVersion("android-37.1")
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
                api(project(":miuix:core"))
                implementation(libs.androidx.compose.foundation)
            }
        }
    }
}
