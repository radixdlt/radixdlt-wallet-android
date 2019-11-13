plugins {
    id(BuildPlugins.androidApplication)
    kotlin(BuildPlugins.android)
    kotlin(BuildPlugins.androidExtensions)
    kotlin(BuildPlugins.kapt)
    id(BuildPlugins.safeArgs)
    id(BuildPlugins.androidJunit5)
}

android {
    compileSdkVersion(AndroidSdk.compile)
    defaultConfig {
        applicationId = "com.radixdlt.android.apps.wallet"
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)
        versionCode = 4
        versionName = "0.20.01"

        testInstrumentationRunnerArgument(
            "runnerBuilder",
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments = mapOf("clearPackageData" to "true")
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${System.getProperties()["user.home"]}/.android/debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }

    buildTypes {
        getByName("release") {
            resValue("string", "app_name", "Radix")
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            resValue("string", "app_name", "Radix")
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
        create("dev") {
            versionNameSuffix = "-betanet"
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/LICENSE-notice.md")
    }

    flavorDimensions("sdk")
    productFlavors {
        create("real") {
            setDimension("sdk")
        }
        create("dummy") {
            setDimension("sdk")
            minSdkVersion(26)
        }
        create("betanet") {
            resValue("string", "app_name", "Radix (betanet)")
            setDimension("sdk")
            minSdkVersion(26)
            applicationIdSuffix = ".betanet"
        }
    }

    variantFilter {
        when {
            (buildType.name == "release" && flavors[0].name == "dummy") ||
            (buildType.name == "release" && flavors[0].name == "betanet") ||
            (buildType.name == "dev" && flavors[0].name == "dummy") ||
            (buildType.name == "debug" && flavors[0].name == "betanet") ||
            (buildType.name == "dev" && flavors[0].name == "real") -> setIgnore(true)
        }
    }

    lintOptions {
        isCheckReleaseBuilds = false
    }

    dataBinding {
        isEnabled = true
    }
}

androidExtensions {
    isExperimental = true
}

val ktlint: Configuration by configurations.creating

tasks {

    check {
        dependsOn(ktlint)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType(Test::class) {
        @Suppress("UnstableApiUsage")
        useJUnitPlatform()
    }

    create("ktlint", JavaExec::class) {
        group = "verification"
        description = "Check Kotlin code style."
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"
        args("src/**/*.kt")
        // to generate report in checkstyle format prepend following args:
        // "--reporter=plain", "--reporter=checkstyle,output=${buildDir}/ktlint.xml"
        // see https://github.com/pinterest/ktlint#usage for more
    }

    create("ktlintFormat", JavaExec::class) {
        group = "formatting"
        description = "Fix Kotlin code style deviations."
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"
        args("-F", "src/**/*.kt")
    }
}

configurations {
    all {
        exclude("com.google.guava", "listenablefuture")
    }
}

dependencies {
    ktlint(Libraries.ktlint)

    val betanet = "release~1.0.0-beta.1"
    implementation("com.github.radixdlt:radixdlt-java:$betanet")

    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.ankoCommons)
    implementation(Libraries.legacySupport)
    implementation(Libraries.appCompat)
    implementation(Libraries.material)
    implementation(Libraries.cardView)
    implementation(Libraries.lifeCycleExtensions)
    implementation(Libraries.lifeCycleRuntimeKtx)
    implementation(Libraries.constraintLayout)
    implementation(Libraries.ktxCore)
    implementation(Libraries.browser)
    implementation(Libraries.fireBaseCore)
    implementation(Libraries.roomRuntime)
    implementation(Libraries.roomRxJava)
    kapt(Libraries.roomCompiler)
    implementation(Libraries.roomKtx)
    implementation(Libraries.navigationFragmentKtx)
    implementation(Libraries.navigationUiKtx)
    implementation(Libraries.biometrics)
    implementation(Libraries.preference)
    implementation(Libraries.playServicesVision)
    implementation(Libraries.dagger)
    implementation(Libraries.daggerAndroid)
    implementation(Libraries.daggerAssistedInject)
    kapt(Libraries.daggerAndroidCompiler)
    kapt(Libraries.daggerCompiler)
    kapt(Libraries.daggerAssistedInjectProcessor)
    implementation(Libraries.icu4j)
    implementation(Libraries.circleImageView)
    implementation(Libraries.qrGen)
    implementation(Libraries.durationPicker)
    implementation(Libraries.glide)
    kapt(Libraries.glideCompiler)
    implementation(Libraries.rxAndroid)
    implementation(Libraries.rxKotlin) {
        exclude("io.reactivex.rxjava2", "rxjava")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }
    implementation(Libraries.timber)
    implementation(Libraries.threeTenABP)
    implementation(Libraries.processPhoenix)
    implementation(Libraries.vault)

    implementation(Libraries.searchView)
    implementation(Libraries.lottie)
    implementation(Libraries.liveEvent)

    // nova crypto micro libraries
    implementation(Libraries.novaCryptoBIP32)
    implementation(Libraries.novaCryptoBIP39)
    implementation(Libraries.novaCryptoBIP44)

    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.mockk)
    testImplementation(TestLibraries.junitJupiterApi)
    testRuntimeOnly(TestLibraries.junitJupiterEngine)

    testImplementation(TestLibraries.junitJupiterEngine)
    testImplementation(TestLibraries.junit4)
    testRuntimeOnly(TestLibraries.junitVintageEngine)

    androidTestImplementation(TestLibraries.junit4)
    androidTestImplementation(TestLibraries.junitJupiterApi)
    androidTestImplementation(TestLibraries.androidInstrumentationTest)

    androidTestRuntimeOnly(TestLibraries.junitJupiterEngine)
    androidTestRuntimeOnly(TestLibraries.junitPlatformRunner)
    androidTestRuntimeOnly(TestLibraries.androidInstrumentationTest)

    androidTestImplementation(TestLibraries.runner)
    androidTestImplementation(TestLibraries.rules)
    androidTestImplementation(TestLibraries.junit)
    androidTestImplementation(TestLibraries.core)
    androidTestImplementation(TestLibraries.barista) {
        exclude("com.android.support")
        exclude("org.jetbrains.kotlin") // Only if you already use Kotlin in your project
    }
    androidTestImplementation(TestLibraries.espressoIntents)
    androidTestImplementation(TestLibraries.coreTesting)
    androidTestUtil(TestLibraries.orchestrator)
}
