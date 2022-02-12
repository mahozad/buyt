import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("de.mannodermaus.android-junit5")
}

val appId = "com.pleon.buyt"
val minSDKVersion = 21
val kotlinVersion: String by rootProject.extra
val databaseSchemaLocation = "$projectDir/schemas"
val compileAndTargetSDKVersion = 31
val versionNumber = 12
val versionString = "2.2.0" // alpha -> beta -> rc -> final

// See https://stackoverflow.com/q/60474010
fun getLocalProperty(key: String) = gradleLocalProperties(rootDir).getProperty(key)
fun String?.toFile() = file(this!!)
// Could also use System.getenv("VARIABLE_NAME") to get each variable individually
val environment: Map<String, String> = System.getenv()

android {

    signingConfigs {
        create("BuytMainSigningConfig") {
            // These are read from the local.properties file which is intentionally ignored in VCS.
            // See README.md for more information.
            // For storeFile we could either set its relative path in repository to the env variable
            // and use "${rootProject.projectDir}/${environment["SIGNING_STORE_FILE_PATH"]}" here,
            // or we could set the env variable absolute path as $GITHUB_WORKSPACE/path/in/repo/to/myTemp.jks
            // and just use environment["SIGNING_STORE_PATH"] here (like now)
            keyAlias = getLocalProperty("signing.keyAlias") ?: environment["SIGNING_KEY_ALIAS"]
            storeFile = (getLocalProperty("signing.storeFile") ?: environment["SIGNING_STORE_FILE"]).toFile()
            keyPassword = getLocalProperty("signing.keyPassword") ?: environment["SIGNING_KEY_PASSWORD"]
            storePassword = getLocalProperty("signing.storePassword") ?: environment["SIGNING_STORE_PASSWORD"]
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    sourceSets {
        // We have some .java files in kotlin directory; add them to src as well
        getByName("main").java.srcDirs("src/main/kotlin")
        // Add database schema files to assets, so we can test database migrations
        getByName("androidTest").assets.srcDirs(databaseSchemaLocation)
    }

    // For sliding tutorial
    buildFeatures.dataBinding = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
        // Specify default settings that will be shared across all different product flavors

        applicationId = appId
        minSdk = minSDKVersion
        targetSdk = compileAndTargetSDKVersion
        compileSdk = compileAndTargetSDKVersion
        versionCode = versionNumber
        versionName = versionString

        /* This flag prevents the Android Gradle Plugin from generating PNG versions of
         * vector assets if minSdk is < 21 */
        // vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                // Set the location where Room exports database schema info
                // Effective only if `exportSchema = true` in @Database annotation of database class
                arguments["room.schemaLocation"] = databaseSchemaLocation
            }
        }

        /* For JUnit 5, Make sure to use the AndroidJUnitRunner, or a subclass of it.
         * This requires a dependency on androidx.test:runner */
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Connect JUnit 5 to the runner
        testInstrumentationRunnerArguments += "runnerBuilder" to "de.mannodermaus.junit5.AndroidJUnit5Builder"
        // For cleaning up after every test;
        //  requires androidTestUtil("androidx.test:orchestrator:1.3.0");
        //  see testOptions block for required settings
        // testInstrumentationRunnerArgument("clearPackageData", "true")

        /* The Gradle resource shrinker removes only resources that are not referenced by your app code,
         * which means it will not remove alternative resources for different device configurations.
         * If necessary, you can use the Android Gradle plugin's resourceConfigurations property to
         * remove alternative resource files that your app does not need.
         * For example, if you are using a library that includes language resources (such as AppCompat or
         * Google Play Services), then your APK includes all translated language strings for the messages
         * in those libraries whether the rest of your app is translated to the same languages or not.
         * If you'd like to keep only the languages that your app officially supports, you can specify those
         * languages using the resourceConfigurations property. Any resources for languages not specified are removed. */
        resourceConfigurations += setOf("en", "fa")
    }

    // This is specifically required for ActivityScenarioExtension in instrumentation tests
    kotlinOptions { jvmTarget = "11" }

    // JUnit 5 will bundle in files with identical paths; exclude them
    packagingOptions {
        resources.excludes += "META-INF/LICENSE*"
    }

    lint {
        // Enable/Disable lint checking for errors
        isCheckReleaseBuilds = false
        // If you prefer, you can enable checking for errors in release builds,
        //  but continue the build even when errors are found:
        isAbortOnError = false
    }

    buildTypes {
        getByName("debug") {
            // Change the package name for the debug variant of the app,
            // so it could be installed along with the release variant (if any)
            applicationIdSuffix = ".debug"
        }
        // defines multiple different build types-typically debug and release
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            // "proguard-android-optimize.txt" reduces size more than "proguard-android.txt"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["BuytMainSigningConfig"]
        }
    }
}

//—————————————————————————————————————————————————————————————————————————————————————————————————
// DEPENDENCIES
//—————————————————————————————————————————————————————————————————————————————————————————————————

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    /* If you're targeting JDK 8, you can use extended versions of the Kotlin standard library
     * which contain additional extension functions for APIs added in new JDK versions.
     * So instead of "kotlin-stdlib", use "kotlin-stdlib-jdk8": */
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    /* The AndroidX version of support library "com.android.support:appcompat-v7".
     * Support libraries provide capabilities of newer android versions to older devices and also
     * some of them provide new features on their own. They are separated into several libraries so
     * you don't have to add ONE big library (and include unwanted libs) and make your apk huge.
     * The following library for example adds support for ActionBar, AppCompatActivity and
     * some other for devices down to api v7. */
    implementation(libs.appcompat)
    /* The AndroidX version of "com.android.support:design"
     * Another support library that adds support for material components such as NavigationDrawer,
     * SnackBar, FAB and Tab for older android versions. */
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodelKtx)
    implementation(libs.lifecycle.livedataKtx)
    implementation(libs.lifecycle.commonJava8)
    implementation(libs.lifecycle.runtimeKtx)
    implementation(libs.lifecycle.process)
    implementation(libs.coordinatorLayout)
    implementation(libs.constraintLayout)
    implementation(libs.recyclerview)
    implementation(libs.coroutines)
    implementation(libs.preference)
    implementation(libs.viewpager2)
    implementation(libs.fragment)
    implementation(libs.koin)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)

    implementation(libs.persianDatepicker)
    implementation(libs.multiWaveHeader)
    implementation(libs.jalaliCalendar)
    implementation(libs.williamChart)
    implementation(libs.jBcrypt)
    implementation(libs.anko) // For using doAsync{} and other features

    // For inspecting the database and network in Chrome. In Iran, use VPN due to sanctions.
    debugImplementation(libs.stetho)
    debugImplementation(libs.stetho.jsRhino)
    // Another library for debugging android databases and shared preferences
    debugImplementation(libs.debugDB)

    // Dependencies for local unit tests (JUnit 5 framework)
    testImplementation(libs.junit5)
    testImplementation(libs.assertj)
    testImplementation(libs.hamcrest)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.truth.java8Extension)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.androidx.test.core)

    // Dependencies for instrumented tests
    androidTestImplementation(libs.junit5)
    androidTestImplementation(libs.assertj)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.uiAutomator)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockito.junit5)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.junit5ForAndroid.core)
    androidTestRuntimeOnly(libs.junit5ForAndroid.runner)
}
