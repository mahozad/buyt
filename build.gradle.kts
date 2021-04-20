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
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // For JUnit 5
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.7.1.1")
    }
}

tasks.wrapper {
    // Add a gradle wrapper script to your source folders (by running the wrapper task).
    // The wrapper script when invoked, downloads the defined gradle version, and executes it.
    // By distributing the wrapper with your project, anyone can work with it without needing to install Gradle beforehand
    gradleVersion = "7.0"
    // Download the full version of the Gradle (with sources and documentation)
    distributionType = Wrapper.DistributionType.ALL
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
