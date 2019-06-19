import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension

plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinAndroidExtensions)
    id(BuildPlugins.kotlinKapt)
    id(BuildPlugins.androidJunit5)
}

android {
    compileSdkVersion(AndroidSdk.compile)
    defaultConfig {
        applicationId = "com.radixdlt.android.apps.wallet"
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)
        versionCode = 6
        versionName = "0.15.00"

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
            isMinifyEnabled = false
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
        create("normal") {
            setDimension("sdk")
        }
        create("oreo") {
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
            (buildType.name == "release" && flavors[0].name == "oreo") ||
            (buildType.name == "release" && flavors[0].name == "betanet") ||
            (buildType.name == "dev" && flavors[0].name == "oreo") ||
            (buildType.name == "debug" && flavors[0].name == "betanet") ||
            (buildType.name == "dev" && flavors[0].name == "normal") -> setIgnore(true)
        }
    }
}

val ktlint by configurations.creating

tasks {

    check {
        dependsOn(ktlint)
    }

    withType(Test::class) {
        useJUnitPlatform()
    }

    create("ktlint", JavaExec::class) {
        group = "verification"
        description = "Check Kotlin code style."
        classpath = ktlint
        main = "com.github.shyiko.ktlint.Main"
        args("src/**/*.kt")
        // to generate report in checkstyle format prepend following args:
        // "--reporter=plain", "--reporter=checkstyle,output=${buildDir}/ktlint.xml"
        // see https://github.com/shyiko/ktlint#usage for more
    }

    create("ktlintFormat", JavaExec::class) {
        group = "formatting"
        description = "Fix Kotlin code style deviations."
        classpath = ktlint
        main = "com.github.shyiko.ktlint.Main"
        args("-F", "src/**/*.kt")
    }
}

androidExtensions {
    configure(delegateClosureOf<AndroidExtensionsExtension> {
        isExperimental = true
    })
}

configurations {
    all {
        exclude("com.google.guava", "listenablefuture")
    }
}

dependencies {
    ktlint(Libraries.ktlint)

    val tagVersion = "v23-beta"
    implementation("com.radixdlt:radixdlt-java:$tagVersion")

    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.ankoCommons)
    implementation(Libraries.legacySupport)
    implementation(Libraries.appCompat)
    implementation(Libraries.material)
    implementation(Libraries.cardView)
    implementation(Libraries.fragment)
    implementation(Libraries.lifeCycleExtensions)
    implementation(Libraries.constraintLayout)
    implementation(Libraries.ktxCore)
    implementation(Libraries.browser)
    implementation(Libraries.fireBaseCore)
    implementation(Libraries.roomRuntime)
    implementation(Libraries.roomRxJava)
    kapt(Libraries.roomCompiler)
    implementation(Libraries.navigationFragmentKtx)
    implementation(Libraries.navigationUiKtx)
    implementation(Libraries.playServicesVision)
    implementation(Libraries.dagger)
    implementation(Libraries.daggerAndroid)
    kapt(Libraries.daggerAndroidCompiler)
    kapt(Libraries.daggerCompiler)
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
    implementation(Libraries.novaCryptoBIP39)

    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.mockk)
    testImplementation(TestLibraries.junitJupiterApi)
    testRuntimeOnly(TestLibraries.junitJupiterEngine)

    testImplementation(TestLibraries.junitJupiterEngine)
    testImplementation(TestLibraries.junit4)
    testRuntimeOnly(TestLibraries.junitVintageEngine)

    androidTestImplementation(TestLibraries.junitJupiterApi)
    androidTestImplementation(TestLibraries.androidInstrumentationTest)

    androidTestRuntimeOnly(TestLibraries.junitJupiterEngine)
    androidTestRuntimeOnly(TestLibraries.junitPlatformRunner)
    androidTestRuntimeOnly(TestLibraries.androidInstrumentationTest)

    androidTestImplementation(TestLibraries.runner)
    androidTestImplementation(TestLibraries.rules)
    androidTestImplementation(TestLibraries.junit)
    androidTestImplementation(TestLibraries.barista) {
        exclude("com.android.support")
        exclude("org.jetbrains.kotlin") // Only if you already use Kotlin in your project
    }
    androidTestImplementation(TestLibraries.espressoIntents)
    androidTestImplementation(TestLibraries.coreTesting)
    androidTestUtil(TestLibraries.orchestrator)
}
