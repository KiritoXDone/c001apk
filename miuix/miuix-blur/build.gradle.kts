import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
}

android {
    namespace = "top.yukonga.miuix.kmp.blur"
    compileSdkVersion("android-37.1")
    buildToolsVersion = "36.0.0"
    defaultConfig { minSdk = 33 }
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
                api(project(":miuix:material"))
                implementation(libs.androidx.compose.foundation)
            }
        }
    }
}
