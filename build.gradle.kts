// Top-level build file where you can add configuration options common to all sub-projects/modules.

/* The buildscript node is used to indicate the repositories and dependencies that are used
 by Gradle itself–not for your application */
buildscript {
    var kotlinVersion: String by extra
    kotlinVersion = "1.3.30"

    repositories {
        google()
        jcenter()
    }
    dependencies {
        // This dependency version should be the same as the Android Studio version
        classpath("com.android.tools.build:gradle:3.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

/* In allprojects block, specify repositories and dependencies used by all sub-projects/modules */
allprojects {
    repositories {
        google()
        jcenter()
        // maven("https://maven.google.com")
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
