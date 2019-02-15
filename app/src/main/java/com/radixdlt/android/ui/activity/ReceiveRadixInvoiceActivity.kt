package com.radixdlt.android.ui.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.zxing.EncodeHintType
import com.radixdlt.android.R
import com.radixdlt.android.RadixWalletApplication
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.copyToClipboard
import com.radixdlt.android.util.setAddressWithColors
import kotlinx.android.synthetic.main.activity_receive_radix_invoice.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class ReceiveRadixInvoiceActivity : BaseActivity() {

    private val size = RadixWalletApplication.densityPixel!!

    private lateinit var myAddress: String

    private val tokenTypesList = arrayListOf<String>()

    private var uri: Uri? = null
    private var tokenTypeExtra: String? = null
    private lateinit var token: String

    companion object {
        fun newIntent(ctx: Context) {
            ctx.startActivity<ReceiveRadixInvoiceActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_radix_invoice)

        showAddress()

        val qrCode = QRCode.from(myAddress)
            .withSize(size, size)
            .withHint(EncodeHintType.MARGIN, 1)
            .bitmap()

        Glide.with(this).load(qrCode).into(imageView)

        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.title = getString(R.string.receive_radix_activity_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setListeners()
//        initialiseViewModels()
    }

    private fun setListeners() {
        copyInvoiceButton.setOnClickListener {
            copyToClipboard(this, "")

            toast("")
        }

        shareInvoiceButton.setOnClickListener {

        }
    }

    private fun initialiseViewModels() {
    }

    private fun setTokenTypeSpinner(tokenTypes: List<String>) {
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
}
