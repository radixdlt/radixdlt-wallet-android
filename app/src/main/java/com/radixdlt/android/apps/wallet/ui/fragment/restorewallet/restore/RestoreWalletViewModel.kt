package com.radixdlt.android.apps.wallet.ui.fragment.restorewallet.restore

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.textfield.TextInputLayout
import com.radixdlt.android.apps.wallet.identity.AndroidRadixIdentity
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.util.PREF_MNEMONIC
import com.radixdlt.android.apps.wallet.util.PREF_SECRET
import com.radixdlt.android.apps.wallet.util.Vault
import com.radixdlt.client.core.crypto.ECKeyPair
import com.radixdlt.client.core.crypto.RadixECKeyPairs
import io.github.novacrypto.bip39.MnemonicValidator
import io.github.novacrypto.bip39.SeedCalculator
import io.github.novacrypto.bip39.Validation.InvalidChecksumException
import io.github.novacrypto.bip39.wordlists.English
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.ByteString
import timber.log.Timber

class RestoreWalletViewModel : ViewModel() {

    private val _restoreWalletAction = MutableLiveData<RestoreWalletAction>()
    val restoreWalletAction: LiveData<RestoreWalletAction> get() = _restoreWalletAction

    private val _pastedMnemonic = MutableLiveData<Array<String>>()
    val pastedMnemonic: LiveData<Array<String>> get() = _pastedMnemonic

    private var mnemonic: String = ""

    init {
        _pastedMnemonic.value = emptyArray()
    }

    fun setPastedMnemonic(mnemonicArray: Array<String>) {
        _pastedMnemonic.value = mnemonicArray
    }

    fun pasteButtonClick() {
        viewModelScope.launch {
            delay(100)
            _restoreWalletAction.value = RestoreWalletAction.PasteMnemonic
        }
    }

    fun confirmButtonClick(button: View, view: ConstraintLayout) {
        if (!button.isEnabled) return
        viewModelScope.launch {
            button.isEnabled = false
            delay(125)
            buildMnemonicFromFields(view)
            validateMnemonic(mnemonic)
            button.isEnabled = true
        }
    }

    private fun buildMnemonicFromFields(layout: ConstraintLayout) {
        mnemonic = ""
        layout.children.forEach {
            if (it is TextInputLayout && !it.editText?.text.isNullOrEmpty()) {
                it.editText?.apply {
                    mnemonic += "$text"
                    if (tag != "12") {
                        mnemonic += " "
                    }
                }
            }
        }
        Timber.d(mnemonic)
    }

    fun importWallet() {
        createWallet(mnemonic)
        openWallet()
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

    private fun validateMnemonic(input: String) {
        return try {
            MnemonicValidator.ofWordList(English.INSTANCE).validate(input)
            createWallet(mnemonic)
            openWallet()
        } catch (e: InvalidChecksumException) {
            showWarningDialog()
        } catch (e: Exception) {
            Timber.e("Mnemonic Exception: $e")
            showMnemonicError()
        }
    }

    private fun openWallet() {
        _restoreWalletAction.value = RestoreWalletAction.OpenWallet(Identity.api!!.address.toString())
    }

    private fun showWarningDialog() {
        _restoreWalletAction.value = RestoreWalletAction.ShowDialog
    }

    private fun showMnemonicError() {
        _restoreWalletAction.value = RestoreWalletAction.ShowMnemonicError
    }
}
