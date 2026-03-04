import org.gradle.kotlin.dsl.preBuild
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
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
        prefab = true
    }

    defaultConfig {
        applicationId = "space.celestia.celestiaxr"
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments += listOf("-DANDROID_STL=c++_shared")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                abiFilters += listOf("arm64-v8a") // Meta Quest is arm64-v8a only
            }
        }
    }

    externalNativeBuild {
        cmake {
            version = libs.versions.cmake.get()
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    namespace = "space.celestia.celestiaxr"

    flavorDimensions += "distribution"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}
// Custom tasks
//val copyLocalizedFiles by tasks.registering(Exec::class) {
//    println("Copying localized files")
//    workingDir = projectDir
//    executable = "/bin/sh"
//    args = listOf("copy_localized_files.sh")
//}

val copyGeneralData by tasks.registering(Exec::class) {
    workingDir = projectDir
    executable = "/bin/sh"
    args = listOf("copy_general_data.sh", File(File(File(projectDir, "src"), "main"), "assets").absolutePath)
}

val convertPO by tasks.registering(Exec::class) {
    println("Converting PO")
    workingDir = projectDir
    executable = "/bin/sh"
    args = listOf("convert_po.sh", File(File(File(projectDir, "src"), "main"), "assets").absolutePath)
}

//// Task ordering
//copyGeneralData {
//    mustRunAfter(copyLocalizedFiles)
//}
//
//convertPO {
//    mustRunAfter(copyGeneralData)
//}
//
//tasks.preBuild {
//    dependsOn(copyLocalizedFiles, copyGeneralData, convertPO)
//}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.drawerlayout)

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
    implementation(project(":CelestiaUI"))

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.fragment.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
