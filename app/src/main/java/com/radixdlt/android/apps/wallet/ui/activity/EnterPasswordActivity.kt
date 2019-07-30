package com.radixdlt.android.apps.wallet.ui.activity

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.message.MessagesDao
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionsDao
import com.radixdlt.android.apps.wallet.helper.TextFormatHelper
import com.radixdlt.android.apps.wallet.identity.AndroidRadixIdentity
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.dialog.DeleteWalletDialog
import com.radixdlt.android.apps.wallet.util.PREF_SECRET
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.Vault
import com.radixdlt.android.apps.wallet.util.createProgressDialog
import com.radixdlt.android.apps.wallet.util.deleteAllData
import com.radixdlt.android.apps.wallet.util.hideKeyboard
import com.radixdlt.android.apps.wallet.util.setDialogMessage
import com.radixdlt.android.apps.wallet.util.setProgressDialogVisible
import com.radixdlt.android.apps.wallet.util.showKeyboard
import com.radixdlt.client.application.identity.PrivateKeyEncrypter
import com.radixdlt.client.application.identity.RadixIdentities
import com.radixdlt.client.core.crypto.ECKeyPair
import dagger.android.AndroidInjection
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_enter_password.*
import okio.ByteString
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File
import java.io.FileReader
import javax.inject.Inject

class EnterPasswordActivity : AppCompatActivity(), DeleteWalletDialog.DeleteWalletDialogListener {

    @Inject
    lateinit var transactionsDao: TransactionsDao

    @Inject
    lateinit var transactionsDao2: TransactionsDao2

    @Inject
    lateinit var messagesDao: MessagesDao

    private var newUser: Boolean = false
    private var uri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    private val sharedPreferenceVault = Vault.getVault()

    companion object {
        private const val EXTRA_URI = "com.radixdlt.android.uri"

        fun newIntent(ctx: Context) {
            ctx.startActivity<EnterPasswordActivity>()
        }

        fun newIntent(ctx: Context, uri: Uri) {
            ctx.startActivity<EnterPasswordActivity>(EXTRA_URI to uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_password)

        setConnectingUniverseName()
        setConnectingToSpecificNode()

        uri = intent.getParcelableExtra(EXTRA_URI)

        progressDialog = createProgressDialog(this)

        val myKeyFile = File(filesDir, "keystore.key")
        if (myKeyFile.exists() && QueryPreferences.getPrefPasswordEnabled(this)) {
            subWelcomeTextView.text = getString(R.string.enter_password_activity_sub_welcome_msg)
            createWalletButton.text = getString(R.string.enter_password_activity_unlock_button)
            inputRepeatPasswordTIL.visibility = View.GONE
        } else if (myKeyFile.exists() && !QueryPreferences.getPrefPasswordEnabled(this)) {

            val secret = loadKey() ?: run {
                deleteWallet()
                return
            }

            val privateKeyDecoded = ByteString.decodeHex(secret).toByteArray()

            Identity.myIdentity =
                AndroidRadixIdentity(ECKeyPair(privateKeyDecoded))
            openWallet()
        } else {
            newUser = true
        }

        createWalletButton.setOnClickListener {
            var exception: Exception? = null
            if (newUser) {
                if (passwordLengthChecker()) return@setOnClickListener

                if (!TextUtils.equals(inputPasswordTIET.text, inputRepeatPasswordTIET.text)) {
                    toast(getString(R.string.toast_passwords_dont_match_error))
                    return@setOnClickListener
                }

                prepareForNextStep(
                    it,
                    getString(R.string.enter_password_activity_creating_wallet_progress_dialog)
                )

                doAsync {
                    try {
                        retrieveRadixIdentity(myKeyFile)
                    } catch (e: Exception) {
                        exception = e
                        Timber.e(e, "Error retrieving identity")
                    }
                    uiThread {
                        if (exception != null) {
                            setProgressDialogVisible(progressDialog, false)
                            toast(getString(R.string.toast_creating_wallet_error))
                        } else {
                            openWallet()
                        }
                    }
                }
            } else {
                if (passwordLengthChecker()) return@setOnClickListener

                prepareForNextStep(
                    it,
                    getString(R.string.enter_password_activity_unlocking_wallet_progress_dialog)
                )

                doAsync {
                    try {
                        retrieveRadixIdentity(myKeyFile)
                    } catch (e: Exception) {
                        exception = e
                    }

                    uiThread {
                        if (exception != null) {
                            Timber.e(exception, "We have an error!")
                            setProgressDialogVisible(progressDialog, false)
                            toast(getString(R.string.toast_decrypting_wallet_error))
                            showKeyboard(inputPasswordTIET)
                            return@uiThread
                        } else {
                            openWallet()
                        }
                    }
                }
            }
        }

        exportWalletTextView.setOnClickListener {
            DeleteWalletDialog.newInstance()
                .show(supportFragmentManager, "DELETE_WALLET_DIALOG")
        }

        inputPasswordTIET.setOnFocusChangeListener { _, hasFocus ->
            if (inputRepeatPasswordTIET.visibility == View.GONE && hasFocus) {
                startScrollView.smoothScrollTo(0, startScrollView.bottom)
            }
        }

        inputRepeatPasswordTIET.setOnFocusChangeListener { v, hasFocus ->
            if (v.visibility == View.VISIBLE && hasFocus) {
                startScrollView.smoothScrollTo(0, startScrollView.bottom)
            }
        }
    }

    private fun setConnectingUniverseName() {
        val universe = TextFormatHelper.normal(
            TextFormatHelper.color(
                ContextCompat.getColor(this, R.color.white),
                getString(R.string.enter_password_activity_xml_universe)
            ), TextFormatHelper.color(
                ContextCompat.getColor(this, R.color.colorAccentSecondary),
                QueryPreferences.getPrefNetwork(this)!!
            )
        )

        universeTextView.text = universe
    }

    private fun setConnectingToSpecificNode() {
        if (!QueryPreferences.getPrefIsRandomNodeSelection(this)) {
            specificNodeTextView.visibility = View.VISIBLE
            val node = TextFormatHelper.normal(
                TextFormatHelper.color(
                    ContextCompat.getColor(this, R.color.white),
                    getString(R.string.enter_password_activity_xml_specific_node)
                ), TextFormatHelper.color(
                    ContextCompat.getColor(this, R.color.colorAccentSecondary),
                    QueryPreferences.getPrefNodeIP(this)
                )
            )

            specificNodeTextView.text = node
        } else {
            specificNodeTextView.visibility = View.GONE
        }
    }

    private fun passwordLengthChecker(): Boolean {
        if (inputPasswordTIET.text!!.length < 6) {
            toast(getString(R.string.toast_password_length_error))
            return true
        }
        return false
    }

    private fun retrieveRadixIdentity(myKeyFile: File) {
        Identity.myIdentity = RadixIdentities.loadOrCreateEncryptedFile(
            myKeyFile.path, inputPasswordTIET.text.toString()
        )

        val address = Identity.api!!.address.toString()

        QueryPreferences.setPrefAddress(this@EnterPasswordActivity, address)

        loadKey() ?: run {
            val privateKey = PrivateKeyEncrypter.decryptPrivateKey(
                inputPasswordTIET.text.toString(), FileReader(myKeyFile)
            )

            val privateKeyHex: String = ByteString.of(*privateKey).hex() // Note the spread operator

            saveKey(privateKeyHex)
        }
    }

    private fun saveKey(key: String) {
        sharedPreferenceVault.edit().putString(PREF_SECRET, key).apply()
    }

    private fun loadKey(): String? {
        return sharedPreferenceVault.getString(PREF_SECRET, null)
    }

    private fun prepareForNextStep(it: View, message: String) {
        setDialogMessage(progressDialog, message)
        setProgressDialogVisible(progressDialog, true)
        hideKeyboard(it)
    }

    private fun openWallet() {
        setProgressDialogVisible(progressDialog, false)
        when {
            BaseActivity.lockActive && uri == null -> {
                BaseActivity.lockActive = false
                finish()
            }
            uri != null -> {
                BaseActivity.lockActive = false
                MainActivity.newIntent(this, uri!!)
                finishAffinity()
            }
            else -> {
                MainActivity.newIntent(this)
                finishAffinity()
            }
        }
    }

    override fun onDialogPositiveClick(dialog: AppCompatDialogFragment) {
        deleteWallet()
    }

    private fun deleteWallet() {
        deleteAllData(this)
        deleteTables()

        startActivity<NewWalletActivity>()
        finishAffinity()
    }

    private fun deleteTables() {
        Completable.fromAction {
            transactionsDao.deleteTable()
            transactionsDao2.deleteTable()
            messagesDao.deleteTable()
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    override fun onBackPressed() {
        if (BaseActivity.lockActive) {
            BaseActivity.lockActive = false
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }
}
