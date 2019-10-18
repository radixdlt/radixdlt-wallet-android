package com.radixdlt.android.apps.wallet.ui.fragment.payment.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.client.application.translate.tokens.InsufficientFundsException
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.core.atoms.AtomStatus
import com.radixdlt.client.core.atoms.particles.RRI
import com.radixdlt.client.core.network.actions.SubmitAtomStatusAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
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
        sendTokens(args.addressTo, args.amount.toBigDecimal(), RRI.fromString(args.rri), args.note)
    }

    private fun sendTokens(to: String, amount: BigDecimal, rri: RRI, payLoad: String?) {
        try {
            Identity.api!!.sendTokens(rri, RadixAddress.from(to), amount, payLoad)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it is SubmitAtomStatusAction) {
                        if (it.statusNotification.atomStatus == AtomStatus.STORED) {
                            _paymentStatusState.value = PaymentStatusState.SUCCESS
                            _toAddress.value = to
                            _amount.value = "$amount ${rri.name}"
                        } else {
                            sendError()
                        }
                    }
                }, ::sendError)
                .addTo(compositeDisposable)
        } catch (e: Exception) {
            sendError(e)
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
