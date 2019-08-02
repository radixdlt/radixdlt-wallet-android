const val kotlinVersion = "1.3.41"

object BuildPlugins {
    object Versions {
        const val gradlePluginVersion = "3.5.0-rc02"
        const val safeArgs = "2.1.0-beta02"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.gradlePluginVersion}"
    const val gradlePlugin = "gradle-plugin"
    const val safeArgsPlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.safeArgs}"

    const val androidApplication = "com.android.application"
    const val android = "android"
    const val androidExtensions = "android.extensions"
    const val kapt = "kapt"
    const val safeArgs = "androidx.navigation.safeargs.kotlin"
    const val androidJunit5 = "de.mannodermaus.android-junit5"
}

object AndroidSdk {
    const val min = 24
    const val compile = 28
    const val target = compile
}

object Libraries {
    private object Versions {
        const val ktlint = "0.34.2"
        const val ankoCommons = "0.10.8"
        const val appCompat = "1.1.0-rc01"
        const val jepack = "1.0.0"
        const val constraintLayout = "2.0.0-beta2"
        const val browser = "1.0.0"
        const val firebaseCore = "17.0.1"
        const val room = "1.1.1"
        const val coreKtx = "1.2.0-alpha02"
        const val lifeCycleExtentions = "2.2.0-alpha02"
        const val navigationVersion = "2.1.0-beta02"
        const val dagger = "2.24"
        const val glide = "4.9.0"
        const val material = "1.1.0-alpha09"
        const val playServicesVision = "18.0.0"
        const val icu = "64.2"
        const val circleImageView = "3.0.0"
        const val qrGen = "2.6.0"
        const val rxAndroid = "2.1.1"
        const val rxKotlin = "2.4.0"
        const val timeDurationPicker = "1.1.3"
        const val threeTenABP = "1.2.1"
        const val timber = "4.7.1"
        const val processPhoenix = "2.0.0"
        const val novaCryptoBIP39 = "2019.01.27"
    }

    const val ktlint = "com.pinterest:ktlint:${Versions.ktlint}"

    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    const val ankoCommons =  "org.jetbrains.anko:anko-commons:${Versions.ankoCommons}"
    const val legacySupport = "androidx.legacy:legacy-support-v4:${Versions.jepack}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val ktxCore = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val cardView = "androidx.cardview:cardview:${Versions.jepack}"
    const val lifeCycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifeCycleExtentions}"
    const val lifeCycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifeCycleExtentions}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val browser = "androidx.browser:browser:${Versions.browser}"
    const val fireBaseCore = "com.google.firebase:firebase-core:${Versions.firebaseCore}"
    const val roomRuntime = "android.arch.persistence.room:runtime:${Versions.room}"
    const val roomRxJava =  "android.arch.persistence.room:rxjava2:${Versions.room}"
    const val roomCompiler = "android.arch.persistence.room:compiler:${Versions.room}"

    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.navigationVersion}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.navigationVersion}"

    const val playServicesVision = "com.google.android.gms:play-services-vision:${Versions.playServicesVision}"

    const val icu4j = "com.ibm.icu:icu4j:${Versions.icu}"

    const val circleImageView = "de.hdodenhof:circleimageview:${Versions.circleImageView}"

    const val qrGen = "com.github.kenglxn.QRGen:android:${Versions.qrGen}"

    const val durationPicker = "mobi.upod:time-duration-picker:${Versions.timeDurationPicker}"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    // Rx
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"
    const val rxKotlin =  "io.reactivex.rxjava2:rxkotlin:${Versions.rxKotlin}"

    // JakeWharton
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    const val threeTenABP = "com.jakewharton.threetenabp:threetenabp:${Versions.threeTenABP}"
    const val processPhoenix = "com.jakewharton:process-phoenix:${Versions.processPhoenix}"

    const val vault = "com.bottlerocketstudios:vault:1.4.2"

    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerAndroid = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val daggerAndroidCompiler = "com.google.dagger:dagger-android-processor:${Versions.dagger}"

    const val novaCryptoBIP39 = "io.github.novacrypto:BIP39:${Versions.novaCryptoBIP39}"
}

object TestLibraries {
    private object Versions {
        const val junit4 = "4.13-beta-3"
        const val mockitoKotlin = "1.5.0"
        const val mockk = "1.9.3"
        const val jupiter = "5.5.1"
        const val androidIntrumentationTest = "0.2.2"
        const val junitPlatformRunner = "1.5.1"
        const val runner = "1.3.0-alpha02"
        const val rules = "1.3.0-alpha02"
        const val barista = "3.2.0"
        const val espressoIntents = "3.3.0-alpha02"
        const val junit = "1.1.2-alpha02"
        const val coreTesting = "1.1.1"
        const val orchestrator = "1.3.0-alpha02"
    }

    const val mockitoKotlin = "com.nhaarman:mockito-kotlin:${Versions.mockitoKotlin}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"

    const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.jupiter}"
    const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.jupiter}"
    const val junitJupiterParams = "org.junit.jupiter:junit-jupiter-params:${Versions.jupiter}"

    const val junit4 = "junit:junit:${Versions.junit4}"
    const val junitVintageEngine = "org.junit.vintage:junit-vintage-engine:${Versions.jupiter}"

    const val androidInstrumentationTest = "de.mannodermaus.junit5:android-instrumentation-test:${Versions.androidIntrumentationTest}"

    const val junitPlatformRunner = "org.junit.platform:junit-platform-runner:${Versions.junitPlatformRunner}"
    const val androidInstrumentationTestRunner = "de.mannodermaus.junit5:android-instrumentation-test-runner:${Versions.androidIntrumentationTest}"

    const val runner = "androidx.test:runner:${Versions.runner}"
    const val rules = "androidx.test:rules:${Versions.rules}"
    const val junit = "androidx.test.ext:junit:${Versions.junit}"

    const val barista = "com.schibsted.spain:barista:${Versions.barista}"
    const val espressoIntents = "androidx.test.espresso:espresso-intents:${Versions.espressoIntents}"
    const val coreTesting = "android.arch.core:core-testing:${Versions.coreTesting}"
    const val orchestrator = "androidx.test:orchestrator:${Versions.orchestrator}"
}
