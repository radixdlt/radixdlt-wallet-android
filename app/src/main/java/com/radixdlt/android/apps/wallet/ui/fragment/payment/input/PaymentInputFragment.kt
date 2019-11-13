package com.radixdlt.android.apps.wallet.ui.fragment.payment.input

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.BarcodeCaptureActivity
import com.radixdlt.android.apps.wallet.ui.activity.NewWalletActivity
import com.radixdlt.android.apps.wallet.ui.activity.PaymentViewModel
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetPayment
import com.radixdlt.android.apps.wallet.util.EmptyTextWatcher
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.isRadixAddress
import com.radixdlt.android.apps.wallet.util.showKeyboard
import com.radixdlt.android.apps.wallet.util.toast
import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.core.atoms.particles.RRI
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_payment_input.*
import org.jetbrains.anko.px2dip
import org.jetbrains.anko.startActivity
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.max

class PaymentInputFragment : Fragment() {

    private val args: PaymentInputFragmentArgs by navArgs()
    private val addressArg: String? by lazy { args.address }
    private val uri: Uri? by lazy { args.uri }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val dimen: Int by lazy { resources.getDimension(R.dimen.toolbar_elevation).toInt() }
    private val myAddress: String by lazy { QueryPreferences.getPrefAddress(ctx) }

    private val paymentViewModel: PaymentViewModel by activityViewModels()

    private lateinit var paymentInputViewModel: PaymentInputViewModel
    private lateinit var ctx: Context
    private lateinit var token: String
    private lateinit var api: RadixApplicationAPI
    private lateinit var maxValue: String
    private lateinit var tokenGranularity: BigDecimal

    private var noteEmpty = true
    private var greenSpan = false

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
        ctx = view.context
        checkForIdentity()

        initialiseToolbar(view)

        initialiseUri()
        initialiseAddress()
        initialiseViewModels()
        noteFieldVisibility()
        setListeners()
    }

    private fun noteFieldVisibility() {
        noteInputVisibility(paymentViewModel.noteInputShown)
    }

    private fun setColorForLastCharactersInAddress(s: Editable): SpannableStringBuilder {
        val start = s.length - 7
        val firstPart = s.toString().substring(0, s.length - 7)
        val firstPartSpannableChar = SpannableString(firstPart)
        val last6Chars = s.toString().substring(start)
        val lastSpannableChar = SpannableString(last6Chars)

        // Span to set char color
        val fcs = ForegroundColorSpan(ContextCompat.getColor(activity!!, R.color.radixGreen2))
        // Set the text color for the last 7 characters
        lastSpannableChar.setSpan(fcs, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        return SpannableStringBuilder(firstPartSpannableChar).append(lastSpannableChar)
    }

    private fun setDefaultColorForAddress(before: String): SpannableString {
        val textSpannableChar = SpannableString(before)

        // Span to set char color
        val fcs = ForegroundColorSpan(ContextCompat.getColor(activity!!, R.color.materialGrey900))
        textSpannableChar.setSpan(fcs, 0, before.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        return textSpannableChar
    }

    private fun noteInputVisibility(visible: Boolean) {
        if (visible) {
            paymentInputMessageTIL.visibility = View.VISIBLE
            paymentInputAddNote.text = getString(R.string.payment_input_fragment_note_delete)
        } else {
            paymentInputMessageTIET.text?.clear()
            paymentInputMessageTIL.visibility = View.GONE
            paymentInputAddNote.text = getString(R.string.payment_input_fragment_note_optional)
        }
        paymentViewModel.noteInputShown = visible
    }

    private fun checkForIdentity() {
        Identity.api?.let {
            api = it
        } ?: run {
            activity?.startActivity<NewWalletActivity>()
            activity?.finishAffinity()
            return
        }
    }

    private fun initialiseAddress() {
        addressArg?.let {
            paymentInputAddressTIET.setText(it)
            paymentInputAmountTIET.requestFocus()
        }
    }

    private fun initialiseUri() {
        uri?.let {
            paymentInputAddressTIET.setText(it.getQueryParameter("to"))
            paymentInputAmountTIET.setText(it.getQueryParameter("amount"))
            val attachment = it.getQueryParameter("attachment")
            token = it.getQueryParameter("token") ?: "XRD"
            if (attachment != null && attachment.isNotBlank()) {
                paymentInputMessageTIET.setText(attachment)
            }
        }
    }

    private fun initialiseToolbar(view: View) {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.elevation = view.context.px2dip(dimen)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.payment_input_fragment_title)
    }

    private fun initialiseViewModels() {
        paymentInputViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(PaymentInputViewModel::class.java)

        paymentInputViewModel.asset.observe(viewLifecycleOwner, Observer(::assetPayment))

        if (paymentViewModel.selectedAsset.isNotEmpty()) {
            paymentInputViewModel.getTransactionsFromAsset(paymentViewModel.selectedAsset)
        }
    }

    private fun assetPayment(asset: AssetPayment?) {
        asset?.apply {
            paymentInputAssetIso.text = iso
            setIcon(asset)

            maxValue = total.toBigDecimal().stripTrailingZeros().toPlainString()
            tokenGranularity = granularity ?: 18.toBigDecimal()

            val total = total.toBigDecimal()
                .setScale(
                    getScale(tokenGranularity), RoundingMode.HALF_UP
                )
                .stripTrailingZeros()
                .toPlainString()

            paymentInputMaxValue.text =
                getString(R.string.payment_input_fragment_add_max_value, total, iso)

            val rri = RRI.of(RadixAddress.from(address), iso).toString()
            paymentViewModel.selectedAsset = rri

            val amount = paymentInputAmountTIET.text?.toString()
            validateAmount(amount)
        }
    }

    private fun validateAmount(amount: String?) {
        if (!amount.isNullOrEmpty() && amount.toBigDecimal() > maxValue.toBigDecimal()) {
            paymentInputAmountTIL.error = getString(R.string.payment_input_fragment_not_enough_tokens_error)
            paymentInputAmountTIL.errorIconDrawable = null
        } else if (!amount.isNullOrEmpty() && getScale(amount.toBigDecimal()) > getScale(tokenGranularity)) {
            paymentInputAmountTIL.error = getString(
                R.string.payment_input_fragment_granularity_error,
                getScale(tokenGranularity).toString()
            )
            paymentInputAmountTIL.errorIconDrawable = null
        } else {
            paymentInputAmountTIL.error = null
            paymentInputAmountTIL.isErrorEnabled = false
        }
    }

    private fun setIcon(asset: AssetPayment) {
        val urlIcon = if (asset.urlIcon.isNullOrBlank()) null else asset.urlIcon
        Glide.with(ctx)
            .load(urlIcon)
            .fallback(R.drawable.no_token_icon)
            .into(paymentInputAssetIcon)
    }

    private fun setListeners() {
        paymentInputSendButton.setOnClickListener {
            val addressFrom = Identity.api?.address.toString()
            val addressTo = paymentInputAddressTIET.text.toString().trim()
            val amountText = paymentInputAmountTIET.text.toString().trim()
            val note = paymentInputMessageTIET.text.let {
                if (it.isNullOrBlank()) null else it.toString()
            }

            if (validateAddressAndAmount(addressTo, amountText)) return@setOnClickListener

            if (!paymentInputAddressTIL.error.isNullOrEmpty() ||
                !paymentInputAmountTIL.error.isNullOrEmpty()) {
                return@setOnClickListener
            }

            noteEmpty = paymentInputMessageTIET.text.isNullOrEmpty()

            val action = PaymentInputFragmentDirections
                .actionNavigationPaymentInputToNavigationPaymentSummary(
                    addressFrom,
                    addressTo,
                    null,
                    amountText,
                    paymentViewModel.selectedAsset,
                    note
                )
            findNavController().navigate(action)
        }

        paymentInputQrScanButton.setOnClickListener {
            startActivityForResult(
                Intent(activity, BarcodeCaptureActivity::class.java),
                RC_BARCODE_CAPTURE
            )
        }

        paymentInputPasteButton.setOnClickListener {
            val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.let {
                val radixAddressToPaste = it.text.toString()
                paymentInputAddressTIET.setText(radixAddressToPaste)
                if (isRadixAddress(radixAddressToPaste)) {
                    paymentInputAddressTIET.clearFocus()

                    if (radixAddressToPaste == myAddress) {
                        paymentInputAddressTIL.error = getString(R.string.toast_entered_own_address_error)
                        paymentInputAddressTIL.errorIconDrawable = null
                        return@setOnClickListener
                    }

                    if (paymentInputAmountTIET.text.isNullOrEmpty()) {
                        paymentInputAmountTIET.requestFocus()
                        showKeyboard(paymentInputAmountTIET)
                    }
                } else {
                    paymentInputAddressTIL.error = getString(R.string.payment_input_fragment_enter_valid_address_error)
                }
            }
        }

        paymentInputMessageTIET.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                paymentInputScrollView.smoothScrollTo(0, paymentInputScrollView.bottom)
            }
        }

        paymentInputLinearLayout.setOnClickListener {
            val action = PaymentInputFragmentDirections
                .actionNavigationPaymentInputToNavigationPaymentAssetSelection()

            findNavController().navigate(action)
        }

        paymentInputMaxValue.setOnClickListener {
            paymentInputAmountTIET.setText(maxValue)
            paymentInputAmountTIL.isErrorEnabled = false
            paymentInputAmountTIET.clearFocus()
        }

        paymentInputAddNote.setOnClickListener {
            noteInputVisibility(paymentInputMessageTIL.visibility == View.GONE)
        }

        paymentInputAmountTIET.addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val amount = s?.toString()
                validateAmount(amount)
            }
        })

        paymentInputAddressTIET.addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    paymentInputAddressTIL.error = null
                    paymentInputAddressTIL.isErrorEnabled = false
                } else if (!isRadixAddress(s.toString())) {

                    paymentInputAddressTIL.error =
                        getString(R.string.payment_input_fragment_enter_valid_address_error)
                    paymentInputAddressTIL.errorIconDrawable = null

                    if (greenSpan) {
                        greenSpan = false

                        paymentInputAddressTIET.removeTextChangedListener(this)

                        val spannableString = setDefaultColorForAddress(s.toString())

                        paymentInputAddressTIET.setText(spannableString)
                        paymentInputAddressTIET.text?.let {
                            paymentInputAddressTIET.setSelection(it.length)
                        }
                        paymentInputAddressTIET.addTextChangedListener(this)
                    }
                } else {
                    greenSpan = true
                    paymentInputAddressTIET.removeTextChangedListener(this)

                    paymentInputAddressTIL.error = null
                    paymentInputAddressTIL.isErrorEnabled = false

                    val spannableStringBuilder = setColorForLastCharactersInAddress(s)

                    paymentInputAddressTIET.text = spannableStringBuilder

                    paymentInputAddressTIET.addTextChangedListener(this)

                    paymentInputAddressTIET.clearFocus()

                    if (paymentInputAmountTIET.text.isNullOrEmpty()) {
                        paymentInputAmountTIET.requestFocus()
                    }
                }
            }
        })
    }

    private fun validateAddressAndAmount(addressTo: String, amountText: String): Boolean {
        if (addressTo.isEmpty() && amountText.isEmpty()) {
            paymentInputAddressTIL.error = getString(R.string.payment_input_fragment_address_error)
            paymentInputAddressTIL.errorIconDrawable = null
            paymentInputAmountTIL.error = getString(R.string.payment_input_fragment_amount_error)
            paymentInputAmountTIL.errorIconDrawable = null
            return true
        } else if (addressTo.isEmpty()) {
            paymentInputAddressTIL.error = getString(R.string.payment_input_fragment_address_error)
            paymentInputAddressTIL.errorIconDrawable = null
            return true
        } else if (amountText.isEmpty()) {
            paymentInputAmountTIL.error = getString(R.string.payment_input_fragment_amount_error)
            paymentInputAmountTIL.errorIconDrawable = null
            return true
        } else if (paymentInputAddressTIET.text.toString() == myAddress) {
            paymentInputAddressTIL.error = getString(R.string.toast_entered_own_address_error)
            paymentInputAddressTIL.errorIconDrawable = null
            return true
        }
        return false
    }

    private fun getScale(bigDecimal: BigDecimal): Int {
        return max(0, bigDecimal.stripTrailingZeros().scale())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode: Barcode? =
                        data.getParcelableExtra(BarcodeCaptureActivity.BARCODE_OBJECT)

                    barcode?.let {
                        paymentInputAddressTIET.setText(it.displayValue.trim())
                    }
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
