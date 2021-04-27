plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("de.mannodermaus.android-junit5")
}

val appId = "com.pleon.buyt"
val minSDKVersion = 21
val compileAndTargetSDKVersion = 30
val versionNumber = 9
val versionString = "1.4.0" // alpha -> beta -> rc -> final
val versionOf = mapOf(
        "appcompat"    to "1.2.0",
        "material"     to "1.3.0",
        "lifecycle"    to "2.3.1",
        "constraint"   to "2.0.4",
        "fragment"     to "1.3.2",
        "coordinator"  to "1.1.0",
        "recyclerview" to "1.2.0",
        "preference"   to "1.1.1",
        "viewpager"    to "1.0.0",
        "koin"         to "2.2.2",
        "room"         to "2.2.6",
        "persiandate"  to "1.2.1",
        "jalalical"    to "1.3.3",
        "stetho"       to "1.6.0",
        "debug-db"     to "1.0.6"
)

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
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("release").java.srcDirs("src/release/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    // for sliding tutorial
    buildFeatures.dataBinding = true

    compileSdkVersion(compileAndTargetSDKVersion)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    testOptions {
        // For Robolectric unit tests to be able to access resources
        unitTests.isIncludeAndroidResources = true

        // To use Android orchestrator for cleaning up tests;
        //  see testInstrumentationRunnerArgument in defaultConfig block
        // execution = "ANDROIDX_TEST_ORCHESTRATOR"

        // NOTE: Espresso suggests to disable all animations on the device
        //  (can also be disabled manually in *Settings* 🡲 *Developer Settings*)
        //  but this resulted in the tests to not complete and keep running forever.
        // animationsDisabled = true
    }

    defaultConfig {
        // specifies default settings that will be shared across all different product flavors

        applicationId = appId
        minSdkVersion(minSDKVersion)
        targetSdkVersion(compileAndTargetSDKVersion)
        versionCode = versionNumber
        versionName = versionString

        /* This flag prevents the Android Gradle Plugin from generating PNG versions of
         * vector assets if minSdkVersion is < 21 */
        // vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                // Tell Room to export database schema info to keep a history of it
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }

        /* For JUnit 5, Make sure to use the AndroidJUnitRunner, or a subclass of it.
         * This requires a dependency on androidx.test:runner */
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Connect JUnit 5 to the runner
        testInstrumentationRunnerArgument("runnerBuilder", "de.mannodermaus.junit5.AndroidJUnit5Builder")
        // For cleaning up after every test;
        //  requires androidTestUtil("androidx.test:orchestrator:1.3.0");
        //  see testOptions block for required settings
        // testInstrumentationRunnerArgument("clearPackageData", "true")

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

    // Specifically, required for ActivityScenarioExtension in instrumentation tests
    kotlinOptions { jvmTarget = "1.8" }

    // JUnit 5 will bundle in files with identical paths; exclude them
    packagingOptions { exclude("META-INF/LICENSE*") }

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
    implementation(fileTree("libs") { include("*.jar") })

    /* The AndroidX version of support library "com.android.support:appcompat-v7".
     * Support libraries provide capabilities of newer android versions to older devices and also
     * some of them provide new features on their own. They are separated into several libraries so
     * you don't have to add ONE big library (and include unwanted libs) and make your apk huge.
     * The following library for example adds support for ActionBar, AppCompatActivity and
     * some other for devices down to api v7. */
    implementation(group = "androidx.appcompat", name = "appcompat", version = versionOf["appcompat"])
    /* The AndroidX version of "com.android.support:design"
     * Another support library that adds support for material components such as NavigationDrawer,
     * SnackBar, FAB and Tab for older android versions. */
    implementation("com.google.android.material:material:${versionOf["material"]}")
    implementation("androidx.constraintlayout:constraintlayout:${versionOf["constraint"]}")
    implementation("androidx.coordinatorlayout:coordinatorlayout:${versionOf["coordinator"]}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${versionOf["lifecycle"]}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${versionOf["lifecycle"]}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${versionOf["lifecycle"]}")
    implementation("androidx.lifecycle:lifecycle-process:${versionOf["lifecycle"]}")
    implementation("androidx.fragment:fragment-ktx:${versionOf["fragment"]}")
    implementation("androidx.recyclerview:recyclerview:${versionOf["recyclerview"]}")
    implementation("androidx.preference:preference-ktx:${versionOf["preference"]}")
    implementation("androidx.viewpager2:viewpager2:${versionOf["viewpager"]}")
    implementation("io.insert-koin:koin-androidx-viewmodel:${versionOf["koin"]}")
    implementation("androidx.room:room-runtime:${versionOf["room"]}")
    kapt("androidx.room:room-compiler:${versionOf["room"]}")

    /* If you're targeting JDK 8, you can use extended versions of the Kotlin standard library
     * which contain additional extension functions for APIs added in new JDK versions.
     * So instead of "kotlin-stdlib", use "kotlin-stdlib-jdk8": */
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.mohamadamin:persianmaterialdatetimepicker:${versionOf["persiandate"]}")
    implementation("ir.huri:JalaliCalendar:${versionOf["jalalical"]}")
    implementation("com.diogobernardino:williamchart:2.5.0")
    implementation("com.scwang.wave:MultiWaveHeader:1.0.0")
    implementation("org.mindrot:jbcrypt:0.4")
    // For using doAsync{} and other features
    implementation("org.jetbrains.anko:anko-commons:0.10.8")

    // For inspecting the database and network in Chrome. In Iran, use VPN due to sanctions.
    debugImplementation("com.facebook.stetho:stetho:${versionOf["stetho"]}")
    debugImplementation("com.facebook.stetho:stetho-js-rhino:${versionOf["stetho"]}")
    // Another library for debugging android databases and shared preferences
    debugImplementation("com.amitshekhar.android:debug-db:${versionOf["debug-db"]}")

    // Dependencies for local unit tests (JUnit 5 framework)
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("androidx.test:core-ktx:1.3.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.2")
    testImplementation("org.robolectric:robolectric:4.5.1")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    testImplementation("com.google.truth:truth:1.1.2")
    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1.2")
    // Dependencies for instrumented tests
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.2")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.room:room-testing:${versionOf["room"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("org.assertj:assertj-core:3.19.0")
    androidTestImplementation("io.mockk:mockk-android:1.11.0")
    androidTestImplementation("org.mockito:mockito-android:3.9.0")
    androidTestImplementation("org.mockito:mockito-junit-jupiter:3.9.0")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.2.2")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.2.2")
}
