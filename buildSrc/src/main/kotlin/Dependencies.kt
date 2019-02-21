const val kotlinVersion = "1.3.21"

object BuildPlugins {
    object Versions {
        const val androidBuildToolsVersion = "3.3.1"
    }

    const val androidGradlePlugin =
        "com.android.tools.build:gradle:${Versions.androidBuildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val kotlinKapt = "kotlin-kapt"
}

object AndroidSdk {
    const val min = 21
    const val compile = 28
    const val target = compile
}

object Libraries {
    private object Versions {
        const val anko = "0.10.1"
        const val appCompat = "1.0.2"
        const val jepack = "1.0.0"
        const val constraintLayout = "2.0.0-alpha3"
        const val ktx = "1.1.0-alpha04"
        const val navVersion = "1.0.0-beta02"
        const val dagger = "2.21"
    }

    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    const val ankoCommons =  "org.jetbrains.anko:anko-commons:${Versions.anko}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val ktxCore = "androidx.core:core-ktx:${Versions.ktx}"
    const val cardView = "androidx.cardview:cardview:${Versions.jepack}"
    const val fragment = "androidx.fragment:fragment-ktx:${Versions.jepack}"
    const val lifeCycleExtensions = "androidx.lifecycle:lifecycle-extensions:2.0.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val browser = "androidx.browser:browser:1.0.0"
    const val fireBase = "com.google.firebase:firebase-core:16.0.7"
    const val roomRuntime = "android.arch.persistence.room:runtime:1.1.1"
    const val roomRxJava =  "android.arch.persistence.room:rxjava2:1.1.1"
    const val roomCompiler = "android.arch.persistence.room:compiler:1.1.1"

    const val navigationFragmentKtx = "android.arch.navigation:navigation-fragment-ktx:${Versions.navVersion}"
    const val navigationUiKtx = "android.arch.navigation:navigation-fragment-ktx:${Versions.navVersion}"

    const val playServicesVision = "com.google.android.gms:play-services-vision:17.0.2"

    const val icu4j = "com.ibm.icu:icu4j:53.1"

    const val circleImageView = "de.hdodenhof:circleimageview:2.2.0"

    const val qrGen = "com.github.kenglxn.QRGen:android:2.5.0"

    const val durationPicker = "mobi.upod:time-duration-picker:1.1.3"

    const val glide = "com.github.bumptech.glide:glide:4.7.1"
    const val glideCompiler = "com.github.bumptech.glide:compiler:4.7.1"

    // Rx
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.0"
    const val rxKotlin =  "io.reactivex.rxjava2:rxkotlin:2.2.0"

    // JakeWharton
    const val timber = "com.jakewharton.timber:timber:4.7.0"
    const val threeTenABP = "com.jakewharton.threetenabp:threetenabp:1.0.5"
    const val processPhoenix = "com.jakewharton:process-phoenix:2.0.0"

    const val vault = "com.bottlerocketstudios:vault:1.4.2"

    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerAndroid = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val daggerAndroidCompiler =
        "com.google.dagger:dagger-android-processor:${Versions.dagger}"
}

object TestLibraries {
    private object Versions {
        const val junit4 = "4.13-beta-2"
    }

    const val mockitoKotlin = "com.nhaarman:mockito-kotlin:1.5.0"
    const val mockk = "io.mockk:mockk:1.8.13.kotlin13"

    const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:5.4.0"
    const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:5.4.0"
    const val junitJupiterParams = "org.junit.jupiter:junit-jupiter-params:5.4.0"

    const val junit4 = "junit:junit:${Versions.junit4}"
    const val junitVintageEngine = "org.junit.vintage:junit-vintage-engine:5.4.0"

    const val androidInstrumentationTest = "de.mannodermaus.junit5:android-instrumentation-test:0.2.2"

    const val junitPlatformRunner = "org.junit.platform:junit-platform-runner:1.4.0"
    const val androidInstrumentationTestRunner = "de.mannodermaus.junit5:android-instrumentation-test-runner:0.2.2"

    const val runner = "androidx.test:runner:1.1.1"
    const val rules = "androidx.test:rules:1.1.1"

    const val barista = "com.schibsted.spain:barista:2.7.0"
    const val espressoIntents = "androidx.test.espresso:espresso-intents:3.1.1"
    const val orchestrator = "androidx.test:orchestrator:1.1.1"
}
