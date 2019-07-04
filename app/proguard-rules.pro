# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name toAddress the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this toAddress preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this toAddress
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keep class javax.** { *; }
-keep class org.** { *; }
-keep class com.radixdlt.client.** { *; }

# Need toAddress keep due toAddress reflection
-keepclassmembers class android.support.design.internal.BottomNavigationMenuView {
    boolean mShiftingMode;
}

# Butterknife
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# Timber
-dontwarn org.jetbrains.annotations.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Google libs
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

## Support library v4 22.2.0
-dontwarn android.support.v4.app.**
-dontwarn android.support.v4.view.**
-dontwarn android.support.v4.widget.**

-dontwarn android.support.v7.**

# OkHttp3
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.*
-dontwarn okhttp3.internal.platform.*

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

## Need to remove bottom line to automatically upload mapping file
# -printmapping mapping.txt

# Bouncycastle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Jackson
-keepattributes Signature,*Annotation*,EnclosingMethod # (Because jackson uses annotation)
-keep class com.fasterxml.jackson.** { *; } # (Keep everything under the jackson package)
-dontwarn com.fasterxml.jackson.databind.** # (Do not throw warning from here)
-dontwarn com.fasterxml.jackson.** # (Do not throw any kind of warning from here)
-keep class org.json.JSONObject.** {** put(java.lang.String,java.util.Map);}

# Guava
-dontwarn java.lang.ClassValue
-keep class java.lang.ClassValue { *; }
-dontwarn com.google.common.util.concurrent.FuturesGetChecked**
-dontwarn com.google.common.reflect.Parameter**
-dontwarn com.google.common.reflect.Invokable**


