plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("de.mannodermaus.android-junit5")
}

android {

    signingConfigs {
        create("buyt") {
            storeFile = file(project.findProperty("signing.storeFilePath") as String? ?: "$rootProject.projectDir/${System.getenv("SIGNING_STORE_FILE_PATH")}")
            keyAlias = project.findProperty("signing.keyAlias") as String? ?: System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = project.findProperty("signing.keyPassword") as String? ?: System.getenv("SIGNING_KEY_PASSWORD")
            storePassword = project.findProperty("signing.storePassword") as String? ?: System.getenv("SIGNING_STORE_PASSWORD")
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
    }

    // for sliding tutorial
    dataBinding {
        isEnabled = true
    }

    buildToolsVersion = "28.0.3"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    compileSdkVersion(28)
    defaultConfig {
        // specifies default settings that will be shared across all different product flavors
        applicationId = "com.pleon.buyt"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0.0-beta" // alpha -> beta -> rc -> final
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            // Tell Room to export database schema info to keep a history of it
            annotationProcessorOptions {
                arguments = mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }

        /* For JUnit 5, Make sure to use the AndroidJUnitRunner, or a subclass of it.
         * This requires a dependency on androidx.test:runner */
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Connect JUnit 5 to the runner
        testInstrumentationRunnerArgument("runnerBuilder", "de.mannodermaus.junit5.AndroidJUnit5Builder")

        /* The Gradle resource shrinker removes only resources that are not referenced by your app code,
         * which means it will not remove alternative resources for different device configurations.
         * If necessary, you can use the Android Gradle plugin's resConfigs property to
         * remove alternative resource files that your app does not need.
         * For example, if you are using a library that includes language resources (such as AppCompat or
         * Google Play Services), then your APK includes all translated language strings for the messages
         * in those libraries whether the rest of your app is translated to the same languages or not.
         * If you'd like to keep only the languages that your app officially supports, you can specify those
         * languages using the resConfig property. Any resources for languages not specified are removed. */
        resConfigs("en", "fa")
    }

    // JUnit 5 will bundle in files with identical paths; exclude them
    packagingOptions {
        exclude("META-INF/LICENSE*")
    }

    lintOptions {
        // Disable lint checking for errors
        isCheckReleaseBuilds = false
        // Or, if you prefer, you can continue to check for errors in release builds,
        // but continue the build even when errors are found:
        isAbortOnError = false
    }

    buildTypes {
        // defines multiple different build types-typically debug and release
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            // "proguard-android-optimize.txt" reduces size more than "proguard-android.txt"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("buyt")
        }
    }
}

//—————————————————————————————————————————————————————————————————————————————————————————————————
// DEPENDENCIES
//—————————————————————————————————————————————————————————————————————————————————————————————————

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    /* The AndroidX version of support library "com.android.support:appcompat-v7".
     * Support libraries provide capabilities of newer android versions to older devices and also
     * some of them provide new features on their own. They are separated into several libraries so
     * you don't have to add ONE big library (and include unwanted libs) and make your apk huge.
     * The following library for example adds support for ActionBar, AppCompatActivity and
     * some other for devices down to api v7. */
    implementation("androidx.appcompat:appcompat:1.1.0-alpha05")
    /* The AndroidX version of "com.android.support:design"
     * Another support library that adds support for material components such as NavigationDrawer,
     * SnackBar, FAB and Tab for older android versions. */
    implementation("com.google.android.material:material:1.1.0-alpha06")
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.2.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0-alpha01")
    implementation("androidx.recyclerview:recyclerview:1.1.0-alpha05")
    implementation("androidx.preference:preference:1.1.0-alpha05")
    implementation("androidx.viewpager2:viewpager2:1.0.0-alpha04")
    implementation("androidx.room:room-runtime:2.1.0-beta01")
    kapt("androidx.room:room-compiler:2.1.0-beta01")
    implementation("com.google.dagger:dagger:2.22.1")
    implementation("com.google.dagger:dagger-android-support:2.22.1")
    kapt("com.google.dagger:dagger-compiler:2.22.1")
    kapt("com.google.dagger:dagger-android-processor:2.22.1")

    /* If you're targeting JDK 8, you can use extended versions of the Kotlin standard library
     * which contain additional extension functions for APIs added in new JDK versions.
     * So instead of "kotlin-stdlib", use "kotlin-stdlib-jdk8": */
    implementation(embeddedKotlin("stdlib-jdk8"))

    implementation("com.mohamadamin:persianmaterialdatetimepicker:1.2.1")
    implementation("com.diogobernardino:williamchart:2.5.0")
    implementation("ir.huri:JalaliCalendar:1.3.3")
    implementation("com.scwang.wave:MultiWaveHeader:1.0.0-alpha-1")
    // For using doAsync{} and other features
    implementation("org.jetbrains.anko:anko-commons:0.10.8")

    // For inspecting the database and network in Chrome. In Iran, use VPN due to sanctions.
    debugImplementation("com.facebook.stetho:stetho:1.5.1")
    debugImplementation("com.facebook.stetho:stetho-js-rhino:1.5.1")
    // Another library for debugging android databases and shared preferences
    debugImplementation("com.amitshekhar.android:debug-db:1.0.6")

    // Dependencies for local unit tests (JUnit 5 framework)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
    testImplementation("org.mockito:mockito-core:2.27.0")
    testImplementation("org.mockito:mockito-junit-jupiter:2.27.0")
    testImplementation("org.hamcrest:hamcrest-library:2.1")
    // Dependencies for instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.1-beta01")
    androidTestImplementation("androidx.test:runner:1.2.0-beta01")
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    androidTestImplementation("org.mockito:mockito-android:2.27.0")
    androidTestImplementation("org.mockito:mockito-junit-jupiter:2.27.0")
    androidTestImplementation("androidx.room:room-testing:2.1.0-beta01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0-beta01")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.0.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.0.0")
}
