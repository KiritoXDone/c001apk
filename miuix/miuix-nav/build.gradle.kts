import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "top.yukonga.miuix.kmp.nav"
    compileSdkVersion("android-37.0")
    buildToolsVersion = "36.0.0"
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

androidComponents {
    onVariants(selector().all()) { variant ->
        variant.sources.baselineProfiles?.addStaticSourceDirectory("src/androidMain/baselineProfiles")
    }
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":miuix:squircle"))
                implementation(libs.androidx.navigationevent.compose)
                implementation(libs.androidx.compose.foundation)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.lifecycle.viewModel.compose)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
