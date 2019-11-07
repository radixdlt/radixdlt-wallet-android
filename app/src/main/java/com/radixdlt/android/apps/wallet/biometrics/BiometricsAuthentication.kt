package com.radixdlt.android.apps.wallet.biometrics

import android.os.Handler
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.radixdlt.android.apps.wallet.R

class BiometricsAuthentication(
    fragmentActivity: FragmentActivity,
    private val callback: (BiometricsAuthenticationResult) -> Unit
) {

    private val handler = Handler()

    private val authCallback = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            callback(BiometricsAuthenticationResult.Error(errorCode))
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            callback(BiometricsAuthenticationResult.Failure)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            callback(BiometricsAuthenticationResult.Success)
        }
    }

    private val biometricPrompt = BiometricPrompt(
        fragmentActivity,
        { runnable -> handler.post(runnable) },
        authCallback
    )

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(fragmentActivity.getString(R.string.biometric_prompt_title))
        .setNegativeButtonText(fragmentActivity.getString(R.string.biometric_prompt_negative_text))
        .build()

    fun authenticate() {
        biometricPrompt.authenticate(promptInfo)
    }
}
