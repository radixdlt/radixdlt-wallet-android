package com.radixdlt.android.apps.wallet.ui.fragment.importwallet

import android.content.ClipboardManager
import android.content.Context
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
import com.radixdlt.android.apps.wallet.util.PREF_SECRET
import com.radixdlt.android.apps.wallet.util.QueryPreferences
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
import java.io.File

class ImportWalletViewModel : ViewModel() {

    // Only used in the event that mnemonic has an invalid checksum and
    // warning dialog is shown
    private lateinit var context: Context

    private val _importWalletAction = MutableLiveData<ImportWalletAction>()
    val importWalletAction: LiveData<ImportWalletAction> get() = _importWalletAction

    private val _pastedMnemonic = MutableLiveData<Array<String>>()
    val pastedMnemonic: LiveData<Array<String>> get() = _pastedMnemonic

    private var mnemonic: String = ""

    init {
        _pastedMnemonic.value = emptyArray()
    }

    fun pasteButtonClick(context: Context) {
        viewModelScope.launch {
            delay(100)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.let {
                val mnemonicArray = it.text.trim().split(" ").toTypedArray()
                if (mnemonicArray.size != 12) {
                    showMnemonicError()
                } else {
                    _pastedMnemonic.value = mnemonicArray
                }
            }
        }
    }

    fun confirmButtonClick(button: View, view: ConstraintLayout) {
        if (!button.isEnabled) return
        viewModelScope.launch {
            button.isEnabled = false
            delay(125)
            buildMnemonicFromFields(view)
            validateMnemonic(view.context, mnemonic)
            button.isEnabled = true
        }
    }

    private fun buildMnemonicFromFields(view: ConstraintLayout) {
        mnemonic = ""
        view.children.forEach {
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
        importWallet(context, mnemonic)
        openWallet()
    }

    private fun importWallet(context: Context, mnemonic: String) {
        val seed: ByteArray = SeedCalculator().calculateSeed(mnemonic, "")

        val privateKey = RadixECKeyPairs
            .newInstance()
            .generateKeyPairFromSeed(seed)
            .privateKey

        val privateKeyHex: String = ByteString.of(*privateKey).hex() // Note the spread operator
        Vault.getVault().edit().putString(PREF_SECRET, privateKeyHex).apply()

        Identity.myIdentity = AndroidRadixIdentity(ECKeyPair(privateKey))

        val address = Identity.api!!.address.toString()

        QueryPreferences.setPrefAddress(context, address)
        QueryPreferences.setPrefPasswordEnabled(context, false) // set to false
        QueryPreferences.setPrefCreatedByMnemonicOrSeed(context, true)
        File(context.filesDir, "keystore.key").createNewFile() // creating dummy file for now
    }

    private fun validateMnemonic(context: Context, input: String) {
        return try {
            MnemonicValidator.ofWordList(English.INSTANCE).validate(input)
            importWallet(context, mnemonic)
            openWallet()
        } catch (e: InvalidChecksumException) {
            this.context = context
            showWarningDialog()
        } catch (e: Exception) {
            Timber.e("Mnemonic Exception: $e")
            showMnemonicError()
        }
    }

    private fun openWallet() {
        _importWalletAction.value = ImportWalletAction.OpenWallet
    }

    private fun showWarningDialog() {
        _importWalletAction.value = ImportWalletAction.ShowDialog
    }

    private fun showMnemonicError() {
        _importWalletAction.value = ImportWalletAction.ShowMnemonicError
    }
}
