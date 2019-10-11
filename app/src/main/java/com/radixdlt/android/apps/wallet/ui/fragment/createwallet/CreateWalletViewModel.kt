package com.radixdlt.android.apps.wallet.ui.fragment.createwallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.identity.AndroidRadixIdentity
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.util.PREF_MNEMONIC
import com.radixdlt.android.apps.wallet.util.PREF_SECRET
import com.radixdlt.android.apps.wallet.util.Vault
import com.radixdlt.client.core.crypto.ECKeyPair
import com.radixdlt.client.core.crypto.RadixECKeyPairs
import io.github.novacrypto.bip39.MnemonicGenerator
import io.github.novacrypto.bip39.SeedCalculator
import io.github.novacrypto.bip39.Words
import io.github.novacrypto.bip39.wordlists.English
import okio.ByteString
import java.security.SecureRandom

class CreateWalletViewModel : ViewModel() {

    private val _createWalletAction = MutableLiveData<CreateWalletAction?>()
    val createWalletAction: LiveData<CreateWalletAction?> get() = _createWalletAction

    fun openWallet() {
        val mnemonic = generateMnemonic()
        createWallet(mnemonic)
        _createWalletAction.value = CreateWalletAction.OpenWallet(Identity.api!!.address.toString())
        resetActionValue()
    }

    fun importWallet() {
        _createWalletAction.value = CreateWalletAction.ImportWallet
        resetActionValue()
    }

    private fun resetActionValue() {
        _createWalletAction.value = null
    }

    private fun generateMnemonic(): String {
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWELVE.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE)
            .createMnemonic(entropy) { sb.append(it) }

        return sb.toString()
    }

    private fun createWallet(mnemonic: String) {
        val seed: ByteArray = SeedCalculator().calculateSeed(mnemonic, "")

        val privateKey = RadixECKeyPairs
            .newInstance()
            .generateKeyPairFromSeed(seed)
            .privateKey

        val privateKeyHex: String = ByteString.of(*privateKey).hex() // Note the spread operator
        Vault.getVault().edit().putString(PREF_SECRET, privateKeyHex).apply()
        Vault.getVault().edit().putString(PREF_MNEMONIC, mnemonic).apply()

        Identity.myIdentity = AndroidRadixIdentity(ECKeyPair(privateKey))
    }
}
