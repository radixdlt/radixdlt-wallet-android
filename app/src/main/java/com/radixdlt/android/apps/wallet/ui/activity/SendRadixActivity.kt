package com.radixdlt.android.apps.wallet.ui.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.createProgressDialog
import com.radixdlt.android.apps.wallet.util.hideKeyboard
import com.radixdlt.android.apps.wallet.util.setDialogMessage
import com.radixdlt.android.apps.wallet.util.setProgressDialogVisible
import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.translate.tokens.InsufficientFundsException
import com.radixdlt.client.core.network.actions.SubmitAtomResultAction
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_send_radix.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import timber.log.Timber
import javax.inject.Inject

class SendRadixActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var sendTokensViewModel: SendTokensViewModel

    private lateinit var progressDialog: ProgressDialog

    private lateinit var api: RadixApplicationAPI

    private val tokenTypesList = arrayListOf<String>()

    private val minimumSendAmount = 0.00001.toBigDecimal()

    private lateinit var myAddress: String

    private var uri: Uri? = null
    private var tokenTypeExtra: String? = null
    private lateinit var token: String

    companion object {
        private const val RC_BARCODE_CAPTURE = 9001

        private const val EXTRA_TRANSACTION_ADDRESS = "com.radixdlt.android.address"
        private const val EXTRA_TRANSACTION_TOKEN_TYPE = "com.radixdlt.android.token_type"
        private const val EXTRA_URI = "com.radixdlt.android.address"

        fun newIntent(ctx: Context) {
            ctx.startActivity<SendRadixActivity>()
        }

        fun newIntent(ctx: Context, address: String, tokenType: String) {
            ctx.startActivity<SendRadixActivity>(
                EXTRA_TRANSACTION_ADDRESS to address,
                EXTRA_TRANSACTION_TOKEN_TYPE to tokenType
            )
        }

        fun newIntent(ctx: Context, uri: Uri) {
            ctx.startActivity<SendRadixActivity>(EXTRA_URI to uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_radix)

        myAddress = QueryPreferences.getPrefAddress(this)

        val addressExtra = intent.getStringExtra(EXTRA_TRANSACTION_ADDRESS)
        tokenTypeExtra = intent.getStringExtra(EXTRA_TRANSACTION_TOKEN_TYPE)
        uri = intent.getParcelableExtra(EXTRA_URI)

        uri?.let {
            inputAddressTIET.setText(it.getQueryParameter("to"))
            amountEditText.setText(it.getQueryParameter("amount"))
            val attachment = it.getQueryParameter("attachment")
            token = it.getQueryParameter("token") ?: "XRD"
            if (attachment != null && attachment.isNotBlank()) {
                inputMessageTIET.setText(attachment)
            }
        }

        addressExtra?.let {
            inputAddressTIET.setText(it)
            amountEditText.requestFocus()
        }

        if (uri == null && addressExtra == null) {
            inputAddressTIET.requestFocus()
        }

        progressDialog = createProgressDialog(this)

        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.title = getString(R.string.send_radix_activity_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Identity.api?.let {
            api = it
        } ?: run {
            startActivity<NewWalletActivity>()
            finishAffinity()
            return
        }

        setListeners()
        initialiseViewModels()
    }

    private fun initialiseViewModels() {
        sendTokensViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SendTokensViewModel::class.java)

        sendTokensViewModel.tokenTypesLiveData.sendingTokens = true

        sendTokensViewModel.tokenTypesLiveData.observe(this, Observer { tokenTypes ->
            tokenTypes?.apply {
                setTokenTypeSpinner(tokenTypes)
            }
        })

        sendTokensViewModel.sendTokensLiveData.observe(this, Observer { status ->
            status?.apply {
                setProgressDialogVisible(progressDialog, false)
                if (status == SubmitAtomResultAction.SubmitAtomResultActionType.STORED.name) {
                    if (uri != null) {
                        intent.removeExtra(EXTRA_URI)
                        finishAffinity()
                    } else {
                        finish()
                    }
                } else if (status == SubmitAtomResultAction.SubmitAtomResultActionType.COLLISION.name ||
                    status == SubmitAtomResultAction.SubmitAtomResultActionType.VALIDATION_ERROR.name) {
                    toast(getString(R.string.toast_collision_error))
                } else if (status == InsufficientFundsException::class.java.simpleName) {
                    toast(getString(R.string.toast_not_enough_tokens_error))
                } else if (status == IllegalArgumentException::class.java.simpleName) {
                    longToast(R.string.toast_too_many_decimal_places_error)
                } else {
                    toast(getString(R.string.toast_wrong_address_format_error))
                }
            }
        })
    }

    private fun setListeners() {
        sendButton.setOnClickListener {
            val amountText = amountEditText.text?.toString()
            val payLoad = if (inputMessageTIET.text.isNullOrBlank()) null else inputMessageTIET.text.toString()

            val amount = if (amountText.isNullOrEmpty()) {
                toast(getString(R.string.toast_enter_valid_amount_error))
                return@setOnClickListener
            } else {
                amountText.toBigDecimal()
            }

            if (amount < minimumSendAmount) {
                longToast(getString(R.string.toast_amount_too_small_error))
                return@setOnClickListener
            }

            if (inputAddressTIET.text.toString() == myAddress) {
                toast(getString(R.string.toast_entered_own_address_error))
                return@setOnClickListener
            }

            val selectedToken = tokenTypesList[tokenTypeSpinner.selectedItemPosition]

            if (selectedToken == getString(R.string.send_activity_token_type_spinner)) {
                toast("Please select type of token to send")
                return@setOnClickListener
            }

            prepareForNextStep(it, getString(R.string.send_radix_activity_sending_progress_dialog))

            Handler().postDelayed({
                sendTokensViewModel.sendToken(
                    inputAddressTIET.text.toString().trim(),
                    amount,
                    selectedToken,
                    payLoad
                )
            }, 50)
        }

        qrScanButton.setOnClickListener {
            startActivityForResult(
                Intent(this, BarcodeCaptureActivity::class.java),
                RC_BARCODE_CAPTURE
            )
        }

        inputMessageTIET.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                sendRadixScrollView.smoothScrollTo(0, sendRadixScrollView.bottom)
            }
        }
    }

    private fun setTokenTypeSpinner(tokenTypes: List<String>) {
        tokenTypesList.clear()
        when {
            tokenTypes.isEmpty() -> tokenTypesList.add(getString(R.string.send_activity_no_tokens_spinner))
            tokenTypes.size == 1 -> tokenTypesList.add(tokenTypes.first())
            else -> {
                tokenTypesList.add(getString(R.string.send_activity_token_type_spinner))
                tokenTypesList.addAll(tokenTypes)
            }
        }

        val tokenTypesListSpinner = tokenTypesList.map {
            removeTokenCreatorAddress(it)
        }

        val tokenTypesSpinner = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, tokenTypesListSpinner
        )

        tokenTypeSpinner.adapter = tokenTypesSpinner

        // set spinner selection if token passed from transaction details
        tokenTypeExtra?.let {
            setTokenInSpinner(tokenTypes, it)
        }

        // set spinner selection if populating from a URI
        uri?.let {
            setTokenInSpinner(tokenTypes, token)
        }
    }

    private fun removeTokenCreatorAddress(tokenType: String): String {
        return if (tokenType.contains("/@")) {
            tokenType.split("/@")[1]
        } else {
            tokenType
        }
    }

    private fun setTokenInSpinner(tokenTypes: List<String>, token: String) {
        val tokenTypeIndex = tokenTypes.indexOf(token)
        if (tokenTypeIndex == -1) {
            toast(getString(R.string.toast_token_not_owned))
            return
        }

        if (tokenTypes.size > 1) {
            tokenTypeSpinner.setSelection(tokenTypes.indexOf(token) + 1)
        } else {
            tokenTypeSpinner.setSelection(tokenTypes.indexOf(token))
        }
    }

    private fun prepareForNextStep(it: View, message: String) {
        setDialogMessage(progressDialog, message)
        setProgressDialogVisible(progressDialog, true)
        hideKeyboard(it)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode: Barcode =
                        data.getParcelableExtra(BarcodeCaptureActivity.BARCODE_OBJECT)
                    inputAddressTIET.setText(barcode.displayValue.trim())
                    Timber.d("Barcode read: ${barcode.displayValue.trim()}")
                } else {
                    toast(getString(R.string.barcode_failure))
                    Timber.d("No barcode captured, intent data is null")
                }
            } else {
                val failureString = getString(R.string.barcode_error) +
                    CommonStatusCodes.getStatusCodeString(resultCode)
                toast(getString(R.string.toast_error_reading_qr_code))
                Timber.e(failureString)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
