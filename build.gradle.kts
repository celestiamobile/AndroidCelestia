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

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.android.tools.build.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.google.services)
    }
}

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete("${rootProject.projectDir}/app/src/main/assets/CelestiaResources")
    delete(rootProject.layout.buildDirectory)
}