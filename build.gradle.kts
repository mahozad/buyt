// Top-level build file where you can add configuration options common to all sub-projects/modules.

/* The buildscript node is used to indicate the repositories and dependencies that are used
 by Gradle itself–not for your application */
buildscript {
    var kotlinVersion: String by extra
    kotlinVersion = "1.3.50"

    repositories {
        google()
        jcenter() // REMOVEME: Deprecated
        mavenCentral()
    }
    dependencies {
        // This dependency version should be the same as the Android Studio version
        classpath("com.android.tools.build:gradle:3.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // For JUnit 5
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.4.2.0")
    }
}

/* In allprojects block, specify repositories and dependencies used by all sub-projects/modules */
allprojects {
    repositories {
        google()
        jcenter() // REMOVEME: Deprecated
        mavenCentral()
        maven("https://jitpack.io")
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
