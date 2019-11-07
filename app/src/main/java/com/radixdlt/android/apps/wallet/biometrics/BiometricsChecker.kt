package com.radixdlt.android.apps.wallet.biometrics

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager

sealed class BiometricsChecker {

    abstract val isUsingBiometrics: Boolean

    @TargetApi(Build.VERSION_CODES.Q)
    private class QBiometricsChecker(
        private val biometricManager: BiometricManager
    ) : BiometricsChecker() {

        private val availableCode = BiometricManager.BIOMETRIC_SUCCESS

        override val isUsingBiometrics: Boolean
            get() = availableCode == biometricManager.canAuthenticate()

        companion object {

            fun getInstance(context: Context): QBiometricsChecker? =
                context.getSystemService(BiometricManager::class.java)?.let {
                    QBiometricsChecker(it)
                }
        }
    }

    @Suppress("DEPRECATION")
    private class LegacyBiometricsChecker(
        private val fingerprintManager: android.hardware.fingerprint.FingerprintManager
    ) : BiometricsChecker() {

        override val isUsingBiometrics: Boolean
            get() = fingerprintManager.isHardwareDetected &&
                fingerprintManager.hasEnrolledFingerprints()

        companion object {

            fun getInstance(context: Context): LegacyBiometricsChecker? =
                context.getSystemService(
                    android.hardware.fingerprint.FingerprintManager::class.java
                )?.let {
                    LegacyBiometricsChecker(it)
                }
        }
    }

    private class DefaultBiometricsChecker : BiometricsChecker() {

        override val isUsingBiometrics: Boolean = false
    }

    companion object {

        @SuppressLint("ObsoleteSdkInt")
        fun getInstance(context: Context): BiometricsChecker {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                    QBiometricsChecker.getInstance(context)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    LegacyBiometricsChecker.getInstance(context)
                else -> null
            } ?: DefaultBiometricsChecker()
        }
    }
}
