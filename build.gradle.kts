
// Top-level build file where you can add configuration options common to all sub-projects/modules.

/* The buildscript node is used to indicate the repositories and dependencies that are used
 by Gradle itself–not for your application */
buildscript {
    var kotlinVersion: String by extra
    kotlinVersion = "1.6.10"

    repositories {
        google()
        jcenter() // REMOVEME: Deprecated
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        // For JUnit 5
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.8.2.0")
    }
}

tasks.wrapper {
    // Add a gradle wrapper script to your source folders (by running the wrapper task).
    // The wrapper script when invoked, downloads the defined gradle version, and executes it.
    // By distributing the wrapper with your project, anyone can work with it without needing to install Gradle beforehand
    gradleVersion = "7.3.3"
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

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
