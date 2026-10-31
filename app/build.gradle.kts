import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

val gitCommitCount =
    providers
        .exec {
            commandLine("git", "rev-list", "HEAD", "--count")
        }
        .standardOutput
        .asText
        .map { it.trim().toInt() }
        .get()
val appVersion = providers.gradleProperty("appVersion").orElse("1.4.10").get()

require(Regex("[0-9]+\\.[0-9]+\\.[0-9]+").matches(appVersion))

val updateRepository =
    providers.gradleProperty("updateRepository").orElse("KiritoXDone/c001apk").get()

require(
    Regex("[A-Za-z0-9-]+/[A-Za-z0-9_.-]+").matches(updateRepository),
)

android {
    namespace = "com.example.c001apk.compose"
    compileSdkVersion("android-37.0")
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.example.c001apk.compose"
        minSdk = 24
        ndk { abiFilters += "arm64-v8a" }
        targetSdk = 35
        versionCode = gitCommitCount
        versionName = appVersion
        buildConfigField("String", "APP_VERSION", "\"$appVersion\"")
        buildConfigField("String", "UPDATE_REPOSITORY", "\"$updateRepository\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val localProperties =
        Properties().also {
            val properties = rootProject.file("local.properties")
            if (properties.exists()) it.load(properties.inputStream())
        }

    fun signingValue(key: String): String? =
        providers.environmentVariable("C001APK_$key").orNull ?: localProperties.getProperty(key)
    val config =
        signingValue("KEYSTORE_PATH")?.let {
            signingConfigs.create("release") {
                storeFile = file(it)
                storePassword = signingValue("KEYSTORE_PASSWORD")
                keyAlias = signingValue("KEY_ALIAS")
                keyPassword = signingValue("KEY_PASSWORD")
                enableV2Signing = true
                enableV3Signing = true
            }
        }

    if (providers.gradleProperty("requireReleaseSigning").orNull == "true") {
        require(config != null) { "A release signing key is required for distribution" }
    }
    packaging { jniLibs.useLegacyPackaging = true }

    val disableR8 = (project.findProperty("disableR8") as? String) == "true"

    buildTypes {
        debug {
            resValue("string", "app_name", "c001apk")
        }
        all {
            signingConfig = config ?: signingConfigs["debug"]
        }
        release {
            isMinifyEnabled = !disableR8
            isShrinkResources = !disableR8
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    buildFeatures {
        resValues = true
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packagingOptions.resources.excludes +=
        setOf(
            "META-INF/**",
            "kotlin/**",
            "**/*.proto",
        )

    dependenciesInfo.includeInApk = false

    applicationVariants.configureEach {
        outputs.configureEach {
            if (baseName == "release")
                (this as? ApkVariantOutputImpl)?.outputFileName =
                    "c001apk_$versionName($versionCode).apk"
        }
    }
}

ksp {
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
    arg("room.schemaLocation", "$projectDir/schemas")
}

protobuf {
    protoc {
        artifact = libs.google.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.nav)
    implementation(libs.kotlinx.serialization.json)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigationevent.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewModel.compose)
    implementation("androidx.savedstate:savedstate:1.2.1")
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.webkit)

    implementation(libs.google.accompanist.drawablepainter)
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.android.compiler)
    implementation(libs.google.protobuf.kotlin.lite)

    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.gson)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.jp.wasabeef.transformers.coil)
    implementation(libs.me.zhanghai.android.appiconloader.coil)

    implementation(libs.jbcrypt)
    implementation(libs.jsoup)
    implementation("org.commonmark:commonmark:0.30.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.30.0")
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.30.0")
    implementation("org.commonmark:commonmark-ext-task-list-items:0.30.0")
    implementation(libs.toolbar.compose) {
        // This library uses only Foundation; its published UI demo dependencies are unused.
        exclude(group = "androidx.compose.material")
        exclude(group = "androidx.compose.ui", module = "ui-tooling")
        exclude(group = "androidx.compose", module = "compose-bom")
    }
    implementation(libs.oss.android.sdk)

    implementation(project(":mojito"))
    implementation(project(":SketchImageViewLoader"))
    implementation(project(":media-support"))
}
