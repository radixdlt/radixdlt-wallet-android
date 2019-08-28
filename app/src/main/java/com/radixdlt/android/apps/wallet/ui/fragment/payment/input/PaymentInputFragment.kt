package com.radixdlt.android.apps.wallet.ui.fragment.payment.input

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.BarcodeCaptureActivity
import com.radixdlt.android.apps.wallet.ui.activity.NewWalletActivity
import com.radixdlt.android.apps.wallet.ui.activity.SendTokensViewModel
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.toast
import com.radixdlt.client.application.RadixApplicationAPI
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_send_radix.*
import org.jetbrains.anko.px2dip
import org.jetbrains.anko.startActivity
import timber.log.Timber
import javax.inject.Inject

class PaymentInputFragment : Fragment() {

    private val args: PaymentInputFragmentArgs by navArgs()
    private val addressArg: String? by lazy { args.address }
    private val tokenArg: String? by lazy { args.token }
    private val uri: Uri? by lazy { args.uri }

    val dimen: Int by lazy { resources.getDimension(R.dimen.toolbar_elevation).toInt() }

    private lateinit var token: String

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var sendTokensViewModel: SendTokensViewModel

    private lateinit var api: RadixApplicationAPI

    private val tokenTypesList = arrayListOf<String>()

    private val minimumSendAmount = 0.00001.toBigDecimal()

    private lateinit var myAddress: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_payment_input, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myAddress = QueryPreferences.getPrefAddress(view.context)

        (activity as AppCompatActivity).supportActionBar?.elevation = view.context.px2dip(dimen)

        uri?.let {
            inputAddressTIET.setText(it.getQueryParameter("to"))
            amountEditText.setText(it.getQueryParameter("amount"))
            val attachment = it.getQueryParameter("attachment")
            token = it.getQueryParameter("token") ?: "XRD"
            if (attachment != null && attachment.isNotBlank()) {
                inputMessageTIET.setText(attachment)
            }
        }

        addressArg?.let {
            inputAddressTIET.setText(it)
            amountEditText.requestFocus()
        }

        if (uri == null && addressArg == null) {
            inputAddressTIET.requestFocus()
        }

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        (activity as AppCompatActivity).supportActionBar?.title = "Payment"

        Identity.api?.let {
            api = it
        } ?: run {
            activity?.startActivity<NewWalletActivity>()
            activity?.finishAffinity()
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
    }

    private fun setListeners() {
        sendButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_payment_summary)
        }

        qrScanButton.setOnClickListener {
            activity?.apply {
                startActivityForResult(
                    Intent(this, BarcodeCaptureActivity::class.java),
                    RC_BARCODE_CAPTURE
                )
            }
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
            activity!!, android.R.layout.simple_spinner_dropdown_item, tokenTypesListSpinner
        )

        tokenTypeSpinner.adapter = tokenTypesSpinner

        // set spinner selection if token passed from transaction details
        tokenArg?.let {
            setTokenInSpinner(tokenTypes, it)
        }

        // set spinner selection if populating from a URI
        uri?.let {
            setTokenInSpinner(tokenTypes, token)
        }
    }

    private fun removeTokenCreatorAddress(tokenType: String): String {
        return if (tokenType.contains("/")) {
            tokenType.split("/")[2]
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity!!.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val RC_BARCODE_CAPTURE = 9001
    }
}
