package com.radixdlt.android.apps.wallet.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.zxing.EncodeHintType
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.ui.fragment.wallet.TransactionsViewModel
import com.radixdlt.android.apps.wallet.util.EmptyTextWatcher
import com.radixdlt.android.apps.wallet.util.GENESIS_XRD
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.setAddressWithColors
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_receive_radix_invoice.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.net.URLEncoder
import javax.inject.Inject

class ReceiveRadixInvoiceActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var transactionsViewModel: TransactionsViewModel

    private val size = RadixWalletApplication.densityPixel!!

    private lateinit var myAddress: String

    private val tokenTypesList = arrayListOf<String>()

    private lateinit var generatedURI: String

    companion object {
        fun newIntent(ctx: Context) {
            ctx.startActivity<ReceiveRadixInvoiceActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_radix_invoice)

        showAddress()

        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.title = getString(R.string.receive_radix_activity_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setListeners()
        initialiseViewModels()
    }

    private fun generateURI(
        address: String = myAddress,
        token: String = GENESIS_XRD,
        amount: String = "O",
        attachment: String? = null
    ): String {
        val domain = "https://www.radixdlt.com"
        val path = "/dapp/payment/send"
        val queryTo = "?to=$address"
        val queryAmount = "&amount=$amount"
        val queryToken = "&token=$token"

        return if (attachment == null) {
            "$domain$path$queryTo$queryAmount$queryToken"
        } else {
            val queryAttachment = "&attachment=${URLEncoder.encode(attachment, "UTF-8")}"
            "$domain$path$queryTo$queryAmount$queryToken$queryAttachment"
        }
    }

    private fun generateQR(uri: String) {
        val qrCode = QRCode.from(uri)
            .withSize(size, size)
            .withHint(EncodeHintType.MARGIN, 1)
            .bitmap()

        Glide.with(this).load(qrCode).into(imageView)
    }

    private fun setListeners() {
        amountEditText.addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(amount: Editable?) {
                val token = tokenTypesList[tokenTypeSpinner.selectedItemPosition]
                val attachment = if (inputMessageTIET.text.isNullOrBlank()) null else inputMessageTIET.text.toString()
                if (amount.isNullOrEmpty()) {
                    generatedURI = generateURI(token = token)
                    generateQR(generatedURI)
                } else {
                    generatedURI = generateURI(amount = "$amount", token = token, attachment = attachment)
                    generateQR(generatedURI)
                }
            }
        })

        inputMessageTIET.addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(message: Editable?) {
                val amount = if (amountEditText.text.isNullOrBlank()) "0" else amountEditText.text.toString()
                val token = tokenTypesList[tokenTypeSpinner.selectedItemPosition]
                if (message.isNullOrEmpty()) {
                    generatedURI = generateURI(amount = amount, token = token, attachment = null)
                    generateQR(generatedURI)
                } else {
                    generatedURI = generateURI(amount = amount, token = token, attachment = "$message")
                    generateQR(generatedURI)
                }
            }
        })

        tokenTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Not used
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val token = tokenTypesList[position]
                val amount = if (amountEditText.text.isNullOrBlank()) "0" else amountEditText.text.toString()
                val attachment = if (inputMessageTIET.text.isNullOrBlank()) null else inputMessageTIET.text.toString()
                generatedURI = generateURI(amount = amount, token = token, attachment = attachment)
                generateQR(generatedURI)
            }
        }

        copyInvoiceButton.setOnClickListener {
            copyToClipboard(this, generatedURI)
            toast(generatedURI)
        }

        shareInvoiceButton.setOnClickListener {
            openedShareDialog = true

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, generatedURI)
            startActivity(
                Intent.createChooser(
                    sharingIntent,
                    getString(R.string.account_fragment_sharing_intent_chooser_title)
                )
            )
        }

        copyAddressButton.setOnClickListener {
            copyToClipboard(this, myAddress)
            toast("Copied address: $myAddress")
        }
    }

    private fun initialiseViewModels() {
        transactionsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(TransactionsViewModel::class.java)

        transactionsViewModel.tokenTypesLiveData.observe(this, Observer { tokenTypes ->
            tokenTypes?.apply {
                setTokenTypeSpinner(tokenTypes)
            }
        })
    }

    private fun setTokenTypeSpinner(tokenTypes: List<String>) {
        tokenTypesList.clear()

        if (!tokenTypes.contains(GENESIS_XRD)) {
            tokenTypesList.add(GENESIS_XRD)
        }

        tokenTypesList.addAll(tokenTypes)

        val tokenTypesListSpinner = tokenTypesList.map {
            removeTokenCreatorAddress(it)
        }

        val tokenTypesSpinner = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, tokenTypesListSpinner
        )

        tokenTypeSpinner.adapter = tokenTypesSpinner

        tokenTypeSpinner.setSelection(tokenTypesListSpinner.indexOf("XRD"))
    }

    private fun removeTokenCreatorAddress(tokenType: String): String {
        return if (tokenType.contains("/")) {
            tokenType.split("/")[2]
        } else {
            tokenType
        }
    }

    private fun showAddress() {
        myAddress = QueryPreferences.getPrefAddress(this)
        addressTextView.text = setAddressWithColors(this, myAddress)
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

    override fun onResume() {
        super.onResume()
        openedShareDialog = false
    }
}
