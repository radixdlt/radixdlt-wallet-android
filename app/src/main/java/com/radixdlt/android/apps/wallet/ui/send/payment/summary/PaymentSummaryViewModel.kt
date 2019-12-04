package com.radixdlt.android.apps.wallet.ui.send.payment.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.client.core.atoms.particles.RRI

class PaymentSummaryViewModel : ViewModel() {

    private val _paymentSummaryAction = MutableLiveData<PaymentSummaryAction>()
    val paymentSummaryAction: LiveData<PaymentSummaryAction> get() = _paymentSummaryAction

    private val _accountFrom = MutableLiveData<String>()
    val accountFrom: LiveData<String> get() = _accountFrom

    private val _addressFrom = MutableLiveData<String>()
    val addressFrom: LiveData<String> get() = _addressFrom

    private val _nameTo = MutableLiveData<String>()
    val nameTo: LiveData<String> get() = _nameTo

    private val _addressTo = MutableLiveData<String>()
    val addressTo: LiveData<String> get() = _addressTo

    private val _amount = MutableLiveData<String>()
    val amount: LiveData<String> get() = _amount

    private val _note = MutableLiveData<String>()
    val note: LiveData<String> get() = _note

    private val _rri = MutableLiveData<String>()
    val rri: LiveData<String> get() = _rri

    private val _tokenSymbol = MutableLiveData<String>()
    val tokenSymbol: LiveData<String> get() = _tokenSymbol

    private val _noteVisible = MutableLiveData<Boolean>()
    val noteVisible: LiveData<Boolean> get() = _noteVisible

    fun showTransactionSummary(args: PaymentSummaryFragmentArgs) {
        _accountFrom.value = "" // Future when supporting multi account
        _addressFrom.value = args.addressFrom
        _nameTo.value = "" // Future when supporting contacts
        _addressTo.value = args.addressTo
        _amount.value = args.amount
        _rri.value = args.rri
        val rri = RRI.fromString(args.rri)
        _tokenSymbol.value = rri.name

        // Show or hide note
        if (args.note.isNullOrEmpty()) {
            _noteVisible.value = false
        } else {
            _note.value = args.note
            _noteVisible.value = true
        }
    }

    fun onConfirmAndSendClickListener() {
        _paymentSummaryAction.value = PaymentSummaryAction.Authenticate
    }

    fun onCopyAddressClickListener(address: String) {
        _paymentSummaryAction.value = PaymentSummaryAction.CopyToClipboard(address)
    }
}
