package com.radixdlt.android.apps.wallet.biometrics

sealed class BiometricsAuthenticationResult {
    object Success : BiometricsAuthenticationResult()
    object Failure : BiometricsAuthenticationResult()
    class Error(val code: Int) : BiometricsAuthenticationResult()
}
