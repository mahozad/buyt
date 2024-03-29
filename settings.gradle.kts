// This file is used to tell Gradle which modules it should include when building the application
// If the project expands to use multiple modules, they need to be added here.
rootProject.name = "Buyt"
include(":app")

// Configure the repositories that Gradle will use to look for plugins
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter() // REMOVEME: Deprecated
        google()
    }
}

// Configure dependencies aspects applied to all projects
dependencyResolutionManagement {
    // By default, repositories declared by a project will override whatever is declared in settings.
    // You can change this behavior with the repositoriesMode property.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        jcenter() // REMOVEME: Deprecated
        mavenCentral()
        maven("https://jitpack.io")
    }

    versionCatalogs {
        create("libs") {
            version("room", "2.4.1")
            version("mockk", "1.12.2")
            version("stetho", "1.6.0")
            version("mockito", "4.2.0")
            version("espresso", "3.4.0")
            version("lifecycle", "2.4.0")
            version("androidTest", "1.4.0")
            version("mannodermaus", "1.3.0")

            library("material", "com.google.android.material:material:1.4.0")
            library("appcompat", "androidx.appcompat:appcompat:1.4.1")
            library("coordinatorLayout", "androidx.coordinatorlayout:coordinatorlayout:1.2.0")
            library("constraintLayout", "androidx.constraintlayout:constraintlayout:2.1.3")
            library("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
            library("fragment", "androidx.fragment:fragment-ktx:1.4.0")
            library("recyclerview", "androidx.recyclerview:recyclerview:1.2.1")
            library("preference", "androidx.preference:preference-ktx:1.1.1")
            library("viewpager2", "androidx.viewpager2:viewpager2:1.0.0")
            library("koin", "io.insert-koin:koin-androidx-viewmodel:2.2.3")
            library("persianDatepicker", "com.mohamadamin:persianmaterialdatetimepicker:1.2.1")
            library("jalaliCalendar", "ir.huri:JalaliCalendar:1.3.3")
            library("williamChart", "com.diogobernardino:williamchart:2.5.0")
            library("multiWaveHeader", "com.scwang.wave:MultiWaveHeader:1.0.0")
            library("jBcrypt", "org.mindrot:jbcrypt:0.4")
            library("anko", "org.jetbrains.anko:anko-commons:0.10.8")
            library("debugDB", "com.amitshekhar.android:debug-db:1.0.6")
            library("stetho", "com.facebook.stetho", "stetho").versionRef("stetho")
            library("stetho.jsRhino", "com.facebook.stetho", "stetho-js-rhino").versionRef("stetho")
            library("lifecycle-livedataKtx", "androidx.lifecycle", "lifecycle-livedata-ktx").versionRef("lifecycle")
            library("lifecycle-viewmodelKtx", "androidx.lifecycle", "lifecycle-viewmodel-ktx").versionRef("lifecycle")
            library("lifecycle-commonJava8", "androidx.lifecycle", "lifecycle-common-java8").versionRef("lifecycle")
            library("lifecycle-runtimeKtx", "androidx.lifecycle", "lifecycle-runtime-ktx").versionRef("lifecycle")
            library("lifecycle-process", "androidx.lifecycle", "lifecycle-process").versionRef("lifecycle")
            library("room-ktx", "androidx.room", "room-ktx").versionRef("room")
            library("room-runtime", "androidx.room", "room-runtime").versionRef("room")
            library("room-compiler", "androidx.room", "room-compiler").versionRef("room")
            library("room-testing", "androidx.room", "room-testing").versionRef("room")
            library("junit5", "org.junit.jupiter:junit-jupiter:5.8.2")
            library("androidx-test-core", "androidx.test", "core-ktx").versionRef("androidTest")
            library("androidx-test-runner", "androidx.test", "runner").versionRef("androidTest")
            library("androidx-test-junit", "androidx.test.ext:junit-ktx:1.1.3")
            library("robolectric", "org.robolectric:robolectric:4.7.3")
            library("assertj", "org.assertj:assertj-core:3.22.0")
            library("mockk", "io.mockk", "mockk").version("mockk")
            library("mockk-android", "io.mockk", "mockk-android").version("mockk")
            library("mockito-junit5", "org.mockito", "mockito-junit-jupiter").version("mockito")
            library("mockito-android", "org.mockito", "mockito-android").version("mockito")
            library("hamcrest", "org.hamcrest:hamcrest-library:2.2")
            library("truth", "com.google.truth:truth:1.1.3")
            library("uiAutomator", "androidx.test.uiautomator:uiautomator:2.2.0")
            library("truth-java8Extension", "com.google.truth.extensions:truth-java8-extension:1.1.3")
            library("espresso-core", "androidx.test.espresso", "espresso-core").versionRef("espresso")
            library("espresso-intents", "androidx.test.espresso", "espresso-intents").versionRef("espresso")
            library("espresso-contrib", "androidx.test.espresso", "espresso-contrib").versionRef("espresso")
            library("junit5ForAndroid-core", "de.mannodermaus.junit5", "android-test-core").versionRef("mannodermaus")
            library("junit5ForAndroid-runner", "de.mannodermaus.junit5", "android-test-runner").versionRef("mannodermaus")
        }
    }
}
