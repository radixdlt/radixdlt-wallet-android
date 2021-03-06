const val kotlinVersion = "1.3.61"

object BuildPlugins {
    object Versions {
        const val gradlePluginVersion = "3.5.0"
        const val safeArgs = "2.2.0-rc02"
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
    const val compile = 29
    const val target = compile
}

object Libraries {
    private object Versions {
        const val ktlint = "0.35.0"
        const val ankoCommons = "0.10.8"
        const val appCompat = "1.1.0"
        const val jepack = "1.0.0"
        const val constraintLayout = "2.0.0-beta3"
        const val browser = "1.2.0-beta01"
        const val firebaseCore = "17.2.1"
        const val room = "2.2.2"
        const val coreKtx = "1.2.0-rc01"
        const val lifeCycleExtentions = "2.2.0-rc02"
        const val navigationVersion = "2.2.0-rc02"
        const val biometrics = "1.0.0"
        const val preference = "1.1.0"
        const val dagger = "2.24"
        const val daggerAssistedInject = "0.5.2"
        const val glide = "4.10.0"
        const val material = "1.1.0-alpha10"
        const val playServicesVision = "19.0.0"
        const val icu = "65.1"
        const val circleImageView = "3.0.1"
        const val qrGen = "2.6.0"
        const val rxAndroid = "2.1.1"
        const val rxKotlin = "2.4.0"
        const val timeDurationPicker = "1.1.3"
        const val threeTenABP = "1.2.1"
        const val timber = "4.7.1"
        const val processPhoenix = "2.0.0"
        const val novaCrypto = "2019.01.27"
        const val searchView = "28.0.0"
        const val lottie = "3.3.0"
        const val liveEvent = "1.2.0"
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
    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomRxJava =  "androidx.room:room-rxjava2:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"
    const val biometrics = "androidx.biometric:biometric:${Versions.biometrics}"
    const val preference = "androidx.preference:preference-ktx:${Versions.preference}"

    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.navigationVersion}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.navigationVersion}"

    const val playServicesVision = "com.google.android.gms:play-services-vision:${Versions.playServicesVision}"

    const val icu4j = "com.ibm.icu:icu4j:${Versions.icu}"

    const val circleImageView = "de.hdodenhof:circleimageview:${Versions.circleImageView}"

    const val qrGen = "com.github.kenglxn.QRGen:android:${Versions.qrGen}"

    const val durationPicker = "mobi.upod:time-duration-picker:${Versions.timeDurationPicker}"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    const val lottie = "com.airbnb.android:lottie:${Versions.lottie}"

    const val liveEvent = "com.github.hadilq.liveevent:liveevent:${Versions.liveEvent}"

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
    const val daggerAssistedInject = "com.squareup.inject:assisted-inject-annotations-dagger2:${Versions.daggerAssistedInject}"
    const val daggerAssistedInjectProcessor = "com.squareup.inject:assisted-inject-processor-dagger2:${Versions.daggerAssistedInject}"

    const val searchView = "com.lapism:searchview:${Versions.searchView}"

    const val novaCryptoBIP32 = "io.github.novacrypto:BIP32:${Versions.novaCrypto}"
    const val novaCryptoBIP39 = "io.github.novacrypto:BIP39:${Versions.novaCrypto}"
    const val novaCryptoBIP44 = "io.github.novacrypto:BIP44:${Versions.novaCrypto}"
}

object TestLibraries {
    private object Versions {
        const val junit4 = "4.13-rc-2"
        const val mockitoKotlin = "1.5.0"
        const val mockk = "1.9.3"
        const val jupiter = "5.6.0-M1"
        const val androidIntrumentationTest = "0.2.2"
        const val junitPlatformRunner = "1.6.0-M1"
        const val runner = "1.3.0-alpha02"
        const val rules = "1.3.0-alpha02"
        const val barista = "3.2.0"
        const val espressoIntents = "3.3.0-alpha02"
        const val junitKtx = "1.1.2-alpha02"
        const val coreKtx = "1.2.1-alpha02"
        const val coreTesting = "2.1.0"
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
    const val junit = "androidx.test.ext:junit-ktx:${Versions.junitKtx}"
    const val core = "androidx.test:core-ktx:${Versions.coreKtx}"

    const val barista = "com.schibsted.spain:barista:${Versions.barista}"
    const val espressoIntents = "androidx.test.espresso:espresso-intents:${Versions.espressoIntents}"
    const val coreTesting = "androidx.arch.core:core-testing:${Versions.coreTesting}"
    const val orchestrator = "androidx.test:orchestrator:${Versions.orchestrator}"
}
