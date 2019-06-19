package com.radixdlt.android.apps.wallet.util

import android.content.Context
import com.bottlerocketstudios.vault.SharedPreferenceVault
import com.bottlerocketstudios.vault.SharedPreferenceVaultFactory
import com.bottlerocketstudios.vault.SharedPreferenceVaultRegistry
import com.bottlerocketstudios.vault.keys.generator.Aes256RandomKeyFactory
import timber.log.Timber
import java.security.GeneralSecurityException
import java.security.SecureRandom

object Vault {
    private const val AUTOMATICALLY_KEYED_PREF_FILE_NAME = "com.radixdlt.android.automaticallyKeyedPref"
    private const val AUTOMATICALLY_KEYED_KEY_FILE_NAME = "com.radixdlt.android.automaticallyKeyedKey"
    private const val AUTOMATICALLY_KEYED_KEY_ALIAS = "com.radixdlt.android.automaticallyKeyedAlias"
    private const val AUTOMATICALLY_KEYED_KEY_INDEX = 3
    private const val AUTOMATICALLY_KEYED_PRESHARED_SECRET = "y3m[Um3^vTpLC7x%Xdq?4F8}"
    const val ENCRYPTION_KEY_NAME = "key"

    /**
     * Encapsulates index knowledge.
     */
    fun getVault(): SharedPreferenceVault {
        return SharedPreferenceVaultRegistry.getInstance().getVault(AUTOMATICALLY_KEYED_KEY_INDEX)
    }

    fun initialiseVault(context: Context): Boolean {
        try {
            initKeyedVault(context)
            return true
        } catch (e: GeneralSecurityException) {
            Timber.e(e)
        }

        return false
    }

    /**
     * Create a vault that will automatically key itself initially with a random key.
     */
    private fun initKeyedVault(context: Context) {
        val sharedPreferenceVault = SharedPreferenceVaultFactory
            .getAppKeyedCompatAes256Vault(
                context,
                AUTOMATICALLY_KEYED_PREF_FILE_NAME,
                AUTOMATICALLY_KEYED_KEY_FILE_NAME,
                AUTOMATICALLY_KEYED_KEY_ALIAS,
                AUTOMATICALLY_KEYED_KEY_INDEX,
                AUTOMATICALLY_KEYED_PRESHARED_SECRET
            )
        SharedPreferenceVaultRegistry
            .getInstance()
            .addVault(
                AUTOMATICALLY_KEYED_KEY_INDEX,
                AUTOMATICALLY_KEYED_PREF_FILE_NAME,
                AUTOMATICALLY_KEYED_KEY_ALIAS,
                sharedPreferenceVault
            )
    }

    /**
     * Clear Key when deleting everything
     */
    fun resetKey() {
        val secretKey = Aes256RandomKeyFactory.createKey()
        getVault().rekeyStorage(secretKey)
    }

    /**
     * Generate a secret key for use with encryption / decryption
     */
    fun generateKey(): ByteArray {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        return key
    }
}
