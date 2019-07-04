// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
    }
    dependencies {
        classpath(BuildPlugins.androidGradlePlugin)
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath("com.google.gms:google-services:4.2.0")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.3.2.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("com.github.ben-manes.versions") version ("0.20.0")
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
