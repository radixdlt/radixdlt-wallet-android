// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://dl.bintray.com/novacrypto/BIP/")
    }
    dependencies {
        classpath(BuildPlugins.androidGradlePlugin)
        classpath(kotlin(BuildPlugins.gradlePlugin, kotlinVersion))
        classpath(BuildPlugins.safeArgsPlugin)
        classpath("com.google.gms:google-services:4.3.3")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.5.2.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("com.github.ben-manes.versions") version ("0.27.0")
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url ="https://jitpack.io")
    }
}

tasks.register("clean").configure {
    delete("build")
}
