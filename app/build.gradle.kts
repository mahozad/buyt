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
val databaseSchemaLocation = "$projectDir/schemas"
val compileAndTargetSDKVersion = 31
val versionNumber = 12
val versionString = "2.2.0" // alpha -> beta -> rc -> final

// See https://stackoverflow.com/q/60474010
fun getLocalProperty(key: String) = gradleLocalProperties(rootDir).getProperty(key)
fun String.toFile() = file(this)
// Could also use System.getenv("VARIABLE_NAME") one by one for each variable
val environment: Map<String, String> = System.getenv()

val versionOf = mapOf(
        "appcompat"    to "1.4.1",
        "material"     to "1.4.0",
        "lifecycle"    to "2.4.0",
        "constraint"   to "2.1.3",
        "fragment"     to "1.4.0",
        "coordinator"  to "1.2.0",
        "coroutines"   to "1.6.0",
        "recyclerview" to "1.2.1",
        "preference"   to "1.1.1",
        "viewpager"    to "1.0.0",
        "koin"         to "2.2.3",
        "room"         to "2.4.1",
        "persiandate"  to "1.2.1",
        "jalalical"    to "1.3.3",
        "stetho"       to "1.6.0",
        "debug-db"     to "1.0.6"
)

android {

    signingConfigs {
        create("buyt") {
            // These are read from the local.properties file which is intentionally ignored in VCS.
            // See README.md for more information.
            val storePath = "${rootProject.projectDir}/${environment["SIGNING_STORE_FILE_PATH"]}"
            keyAlias = getLocalProperty("signing.keyAlias") ?: environment["SIGNING_KEY_ALIAS"]
            storeFile = (getLocalProperty("signing.storePath") ?: storePath).toFile()
            keyPassword = getLocalProperty("signing.keyPassword") ?: environment["SIGNING_KEY_PASSWORD"]
            storePassword = getLocalProperty("signing.storePassword") ?: environment["SIGNING_STORE_PASSWORD"]
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("release").java.srcDirs("src/release/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
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
        // specifies default settings that will be shared across all different product flavors

        applicationId = appId
        minSdk = minSDKVersion
        targetSdk = compileAndTargetSDKVersion
        compileSdk = compileAndTargetSDKVersion
        versionCode = versionNumber
        versionName = versionString

        /* This flag prevents the Android Gradle Plugin from generating PNG versions of
         * vector assets if minSdkVersion is < 21 */
        // vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                // Set the location where Room exports database schema info
                // Effective if `exportSchema = true` in @Database annotation of database class
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
         * If necessary, you can use the Android Gradle plugin's resConfigs property to
         * remove alternative resource files that your app does not need.
         * For example, if you are using a library that includes language resources (such as AppCompat or
         * Google Play Services), then your APK includes all translated language strings for the messages
         * in those libraries whether the rest of your app is translated to the same languages or not.
         * If you'd like to keep only the languages that your app officially supports, you can specify those
         * languages using the resConfig property. Any resources for languages not specified are removed. */
        resourceConfigurations += setOf("en", "fa")
    }

    // Specifically, required for ActivityScenarioExtension in instrumentation tests
    kotlinOptions { jvmTarget = "11" }

    // JUnit 5 will bundle in files with identical paths; exclude them
    packagingOptions {
        resources.excludes += "META-INF/LICENSE*"
    }

    lint {
        // Disable lint checking for errors
        isCheckReleaseBuilds = false
        // Or, if you prefer, you can continue to check for errors in release builds,
        // but continue the build even when errors are found:
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
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${versionOf["lifecycle"]}")
    implementation("androidx.lifecycle:lifecycle-process:${versionOf["lifecycle"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${versionOf["coroutines"]}")
    implementation("androidx.fragment:fragment-ktx:${versionOf["fragment"]}")
    implementation("androidx.recyclerview:recyclerview:${versionOf["recyclerview"]}")
    implementation("androidx.preference:preference-ktx:${versionOf["preference"]}")
    implementation("androidx.viewpager2:viewpager2:${versionOf["viewpager"]}")
    implementation("io.insert-koin:koin-androidx-viewmodel:${versionOf["koin"]}")
    implementation("androidx.room:room-runtime:${versionOf["room"]}")
    implementation("androidx.room:room-ktx:${versionOf["room"]}")
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
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("androidx.test:core-ktx:1.4.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("org.robolectric:robolectric:4.7.3")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1.3")
    // Dependencies for instrumented tests
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.room:room-testing:${versionOf["room"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("org.assertj:assertj-core:3.22.0")
    androidTestImplementation("io.mockk:mockk-android:1.12.2")
    androidTestImplementation("org.mockito:mockito-android:4.2.0")
    androidTestImplementation("org.mockito:mockito-junit-jupiter:4.2.0")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
}
