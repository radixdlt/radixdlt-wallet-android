package com.radixdlt.android.ui.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.radixdlt.android.identity.Identity
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.createProgressDialog
import com.radixdlt.android.util.hideKeyboard
import com.radixdlt.android.util.setDialogMessage
import com.radixdlt.android.util.setProgressDialogVisible
import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.translate.tokens.InsufficientFundsException
import com.radixdlt.client.core.network.AtomSubmissionUpdate
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

    private var uri: Uri? = null

    companion object {
        private const val RC_BARCODE_CAPTURE = 9001

        private const val EXTRA_TRANSACTION_ADDRESS = "com.radixdlt.android.address"
        private const val EXTRA_URI = "com.radixdlt.android.address"

        fun newIntent(ctx: Context) {
            ctx.startActivity<SendRadixActivity>()
        }

        fun newIntent(ctx: Context, address: String) {
            ctx.startActivity<SendRadixActivity>(EXTRA_TRANSACTION_ADDRESS to address)
        }

        fun newIntent(ctx: Context, uri: Uri) {
            ctx.startActivity<SendRadixActivity>(EXTRA_URI to uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_radix)

        val addressExtra = intent.getStringExtra(EXTRA_TRANSACTION_ADDRESS)
        uri = intent.getParcelableExtra(EXTRA_URI)

        uri?.let {
            inputAddressTIET.setText(it.getQueryParameter("to"))
            amountEditText.setText(it.getQueryParameter("amount"))
            val attachment = it.getQueryParameter("attachment")
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


        sendTokensViewModel.tokenTypesLiveData.observe(this, Observer { tokenTypes ->
            tokenTypes?.apply {
                setTokenTypeSpinner(tokenTypes)
            }
        })

        sendTokensViewModel.sendTokensLiveData.observe(this, Observer { status ->
            status?.apply {
                setProgressDialogVisible(progressDialog, false)
                if (status == AtomSubmissionUpdate.AtomSubmissionState.STORED.name) {
                    if (uri != null) {
                        intent.removeExtra(SendRadixActivity.EXTRA_URI)
                        finishAffinity()
                    } else {
                        finish()
                    }
                } else if(status == InsufficientFundsException::class.java.simpleName) {
                    toast(getString(R.string.toast_not_enough_tokens_error))
                } else if(status == IllegalArgumentException::class.java.simpleName) {
                    longToast(R.string.toast_too_many_decimal_places_error)
                } else {
                    toast(getString(R.string.toast_wrong_address_format_error))
                }
            }
        })
    }

    private fun setListeners() {
        sendButton.setOnClickListener {
            val amount = if (amountEditText.text.isNullOrEmpty()) {
                toast(getString(R.string.toast_enter_valid_amount_error))
                return@setOnClickListener
            } else {
                amountEditText.text.toString().toBigDecimal()
            }

            if (amount < 0.00001.toBigDecimal()) {
                longToast(getString(R.string.toast_amount_too_small_error))
                return@setOnClickListener
            }

            val payLoad = when {
                inputMessageTIET.text.isNullOrEmpty() -> null
                else -> inputMessageTIET.text.toString()
            }

            if (inputAddressTIET.text.toString() == QueryPreferences.getPrefAddress(this)) {
                toast(getString(R.string.toast_entered_own_address_error))
                return@setOnClickListener
            }

            prepareForNextStep(sendButton, getString(R.string.send_radix_activity_sendgin_progress_dialog))

            val selectedToken = tokenTypesList.get(tokenTypeSpinner.selectedItemPosition)

            sendTokensViewModel.sendToken(
                inputAddressTIET.text.toString().trim(),
                amount,
                selectedToken,
                payLoad
            )
        }

        qrScanButton.setOnClickListener {
            startActivityForResult(
                Intent(this, BarcodeCaptureActivity::class.java), RC_BARCODE_CAPTURE
            )
        }

        inputMessageTIET.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                sendRadixScrollView.smoothScrollTo(0, sendRadixScrollView.bottom)
            }
        }
    }

    private fun setTokenTypeSpinner(tokenTypes: List<String>) {
        when {
            tokenTypes.isEmpty() -> tokenTypesList.add(getString(R.string.send_activity_no_tokens_spinner))
            tokenTypes.size == 1 -> tokenTypesList.addAll(tokenTypes)
            else -> {
                tokenTypesList.add(getString(R.string.send_activity_token_type_spinner))
                tokenTypesList.addAll(tokenTypes)
            }
        }
        val tokenTypesSpinner = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, tokenTypesList
        )
        tokenTypeSpinner.adapter = tokenTypesSpinner
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
                        data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject)
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
