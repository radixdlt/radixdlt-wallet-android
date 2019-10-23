package com.radixdlt.android.apps.wallet.ui.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.identity.AndroidRadixIdentity
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.util.EmptyTextWatcher
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.VAULT_SECRET
import com.radixdlt.android.apps.wallet.util.Vault
import com.radixdlt.android.apps.wallet.util.createProgressDialog
import com.radixdlt.android.apps.wallet.util.setProgressDialogVisible
import com.radixdlt.client.application.identity.PrivateKeyEncrypter
import com.radixdlt.client.application.identity.RadixIdentities
import com.radixdlt.client.core.crypto.ECKeyPair
import com.radixdlt.client.core.crypto.RadixECKeyPairs
import io.github.novacrypto.bip39.MnemonicValidator
import io.github.novacrypto.bip39.SeedCalculator
import io.github.novacrypto.bip39.wordlists.English
import kotlinx.android.synthetic.main.activity_enter_mnemonic.*
import kotlinx.android.synthetic.main.activity_enter_password.*
import okio.ByteString
import java.io.File
import java.io.FileReader

class EnterMnemonicActivity : AppCompatActivity() {

    private val sharedPreferenceVault = Vault.getVault()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_mnemonic)

        progressDialog = createProgressDialog(this)

        inputMnemonicOrSeedTIET.addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(input: Editable?) {
                if (input.isNullOrEmpty()) {
                    mnemonicOrSeedTextView.text = ""
                    return
                }

                if (validMnemonic(input)) {
                    mnemonicOrSeedTextView.text = "CREATING FROM MNEMONIC"
                } else {
                    mnemonicOrSeedTextView.text = "CREATING FROM SEED"
                }
            }
        })

        createWalletFromMnemonicButton.setOnClickListener {
            val mnemonicOrSeed = inputMnemonicOrSeedTIET.text

            if (mnemonicOrSeed.isNullOrEmpty()) return@setOnClickListener

            val seed: ByteArray

            seed = if (validMnemonic(mnemonicOrSeed)) {
                SeedCalculator().calculateSeed(mnemonicOrSeed.toString(), "")
            } else {
                mnemonicOrSeed.toString().toByteArray()
            }

            val privateKey = RadixECKeyPairs
                    .newInstance()
                    .generateKeyPairFromSeed(seed)
                    .privateKey

            val privateKeyHex: String = ByteString.of(*privateKey).hex() // Note the spread operator
            saveKey(privateKeyHex)

            Identity.myIdentity =
                AndroidRadixIdentity(ECKeyPair(privateKey))

            val address = Identity.api!!.address.toString()

            QueryPreferences.setPrefAddress(this, address)
            QueryPreferences.setPrefPasswordEnabled(this, false) // set to false
            QueryPreferences.setPrefCreatedByMnemonicOrSeed(this, true)
            File(filesDir, "keystore.key").createNewFile()
            openWallet()
        }
    }

    private fun retrieveRadixIdentity(myKeyFile: File) {
        Identity.myIdentity = RadixIdentities.loadOrCreateEncryptedFile(
                myKeyFile.path, inputPasswordTIET.text.toString()
        )

        val address = Identity.api!!.address.toString()

        QueryPreferences.setPrefAddress(this, address)

        loadKey() ?: run {
            val privateKey = PrivateKeyEncrypter.decryptPrivateKey(
                    inputPasswordTIET.text.toString(), FileReader(myKeyFile)
            )

            val privateKeyHex: String = ByteString.of(*privateKey).hex() // Note the spread operator

            saveKey(privateKeyHex)
        }
    }

    private fun openWallet() {
        setProgressDialogVisible(progressDialog, false)
        MainActivity.newIntent(this)
        finishAffinity()
    }

    private fun validMnemonic(input: Editable?): Boolean {
        return try {
            MnemonicValidator.ofWordList(English.INSTANCE).validate(input)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun saveKey(key: String) {
        sharedPreferenceVault.edit().putString(VAULT_SECRET, key).apply()
    }

    private fun loadKey(): String? {
        return sharedPreferenceVault.getString(VAULT_SECRET, null)
    }
}
