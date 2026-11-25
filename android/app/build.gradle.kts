//
// OpenMCX Licence Information : AGPL-3.0-or-later
//

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.openmcx.client.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.virtuser.mcx"
        minSdk = 30
        targetSdk = 36
        // Virtuser MCX: each GitHub build numbers itself (run 6 = versionCode 6, 0.1.6), so Android sees each as newer.
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "0.1.${System.getenv("GITHUB_RUN_NUMBER") ?: "0"}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // Virtuser MCX: one fixed TEST signing key, so each build from GitHub installs over the last.
    // Demo use only - replace with a private release key before any real deployment.
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("virtuser-test.keystore")
            storePassword = "android"
            keyAlias = "virtuser-test"
            keyPassword = "android"
        }
    }
    buildTypes {
        // The develop APK (apk/openmcx-client-develop.apk): the debug build, with its debug control for the tests and the
        // same signature, but the libraries shrunk by R8 and unused resources removed.
        create("develop") {
            initWith(getByName("debug"))
            isMinifyEnabled = true
            isShrinkResources = true
            versionNameSuffix = "-develop"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-develop.pro")
            matchingFallbacks += "debug"
        }
        // The screens with sample data and no engine, installed beside the client to try the look (see src/uiPreview).
        create("uiPreview") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".uipreview"
            versionNameSuffix = "-uipreview"
            matchingFallbacks += "debug"
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        // Robolectric's Android runtime (~200 MB) is kept beside the Gradle cache, so it is fetched only once.
        unitTests.all { test ->
            val dir = gradle.gradleUserHomeDir.resolve("robolectric-home")
            test.systemProperty("user.home", dir.path)
            test.doFirst { dir.mkdirs() }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.coroutines.android)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.service)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.core.telecom)
    implementation(libs.browser)
    implementation(libs.zxing.embedded)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.junit)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
}
