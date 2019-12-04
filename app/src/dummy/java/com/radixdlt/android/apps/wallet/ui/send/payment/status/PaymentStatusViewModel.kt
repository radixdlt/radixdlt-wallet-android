package com.radixdlt.android.apps.wallet.ui.send.payment.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.client.application.translate.tokens.InsufficientFundsException
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.math.BigDecimal

class PaymentStatusViewModel : ViewModel() {

    private val _paymentStatusAction = MutableLiveData<PaymentStatusAction>()
    val paymentStatusAction: LiveData<PaymentStatusAction> get() = _paymentStatusAction

    private val _paymentStatusState = MutableLiveData<PaymentStatusState>()
    val paymentStatusState: LiveData<PaymentStatusState> get() = _paymentStatusState

    private val _toAddress = MutableLiveData<String>()
    val toAddress: LiveData<String> get() = _toAddress

    private val _amount = MutableLiveData<String>()
    val amount: LiveData<String> get() = _amount

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> get() = _status

    private val _insufficientFunds = MutableLiveData<Boolean>()
    val insufficientFunds: LiveData<Boolean> get() = _insufficientFunds

    private val compositeDisposable = CompositeDisposable()

    init {
        _paymentStatusState.value = PaymentStatusState.LOADING
    }

    fun makePayment(args: PaymentStatusDialogArgs) {
        sendTokens(args.addressTo, args.amount.toBigDecimal(), RRI.fromString(args.rri))
    }

    private fun sendTokens(to: String, amount: BigDecimal, rri: RRI) {
        if (amount > 0.01.toBigDecimal()) {
            sendError(InsufficientFundsException(rri, amount, 0.01.toBigDecimal()))
        } else {
            _paymentStatusState.postValue(PaymentStatusState.SUCCESS)
            _toAddress.postValue(to)
            _amount.postValue("$amount ${rri.name}")
        }
    }

    private fun sendError(t: Throwable? = null) {
        Timber.e(t, "ShowError sending...")
        if (t is InsufficientFundsException) {
            _insufficientFunds.postValue(true)
            _paymentStatusState.postValue(PaymentStatusState.FAILED)
        } else {
            _paymentStatusState.postValue(PaymentStatusState.FAILED)
        }
    }

    fun onOpenExplorerClickListener() {
        _paymentStatusAction.value = PaymentStatusAction.OpenExplorerAction
    }

    fun onTryAgainClickListener() {
        _paymentStatusAction.value = PaymentStatusAction.TryPaymentAgainAction
    }

    fun onCloseClickListener() {
        _paymentStatusAction.value = PaymentStatusAction.ClosePaymentAction
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
