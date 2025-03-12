/*
 * build.gradle.kts
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

import org.gradle.kotlin.dsl.preBuild

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.org.jetbrains.kotlin.kapt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.org.jetbrains.kotlin.serialization)
}

android {
    compileSdk = libs.versions.compile.sdk.get().toInt()
    buildToolsVersion = libs.versions.build.tools.get()
    ndkVersion = libs.versions.ndk.get()

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "space.celestia.mobilecelestia"
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    namespace = "space.celestia.mobilecelestia"

    flavorDimensions += "distribution"
    productFlavors {
        create("sideload") {
            isDefault = true
            dimension = "distribution"
            assetPacks += emptyList()
        }
        create("unofficial") {
            dimension = "distribution"
            assetPacks += emptyList()
        }
        create("play") {
            dimension = "distribution"
            assetPacks += listOf(":base_assets")
        }
    }
}

// Custom tasks
val copyLocalizedFiles by tasks.registering(Exec::class) {
    println("Copying localized files")
    workingDir = projectDir
    executable = "/bin/sh"
    setArgs(listOf("copy_localized_files.sh"))
}

val copyGeneralData by tasks.registering(Exec::class) {
    workingDir = projectDir
    executable = "/bin/sh"
    setArgs(listOf("copy_general_data.sh"))
}

val convertPO by tasks.registering(Exec::class) {
    println("Converting PO")
    workingDir = projectDir
    executable = "/bin/sh"
    setArgs(listOf("convert_po.sh"))
}

// Task ordering
copyGeneralData {
    mustRunAfter(copyLocalizedFiles)
}

convertPO {
    mustRunAfter(copyGeneralData)
}

tasks.preBuild {
    dependsOn(copyLocalizedFiles, copyGeneralData, convertPO)
}

val playImplementation by configurations
val sideloadImplementation by configurations

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib) // Duplicate — you might want to remove one
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.drawerlayout)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)

    implementation(libs.androidx.webkit)

    implementation(project(":LinkPreview"))
    implementation(project(":Celestia"))
    implementation(project(":CelestiaFoundation"))

    playImplementation(libs.sentry.android)
    sideloadImplementation(libs.sentry.android)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    playImplementation(libs.billing.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
