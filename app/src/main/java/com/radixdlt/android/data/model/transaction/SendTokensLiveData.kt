package com.radixdlt.android.data.model.transaction

import androidx.lifecycle.LiveData
import com.radixdlt.android.identity.Identity
import com.radixdlt.client.application.translate.tokens.InsufficientFundsException
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.atommodel.tokens.TokenClassReference
import com.radixdlt.client.core.network.AtomSubmissionUpdate
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

class SendTokensLiveData @Inject constructor(
    // TODO: Use in future where token will be inserted immediately and have a pending state until
    // confirmed by the network.
    private val transactionsDao: TransactionsDao
) : LiveData<String>() {

    private val compositeDisposable = CompositeDisposable()

    fun sendToken(to: String, amount: BigDecimal, token: String, payLoad: String?) {
        Identity.api!!.getBalance(Identity.api!!.getMyAddress())
            .subscribe { it ->
                Timber.tag("Empty").d("$it ${it.isEmpty()}")

                var balance: BigDecimal = BigDecimal.ZERO

                val tokenClassReference = it.map { map ->
                    balance = map.value
                    map.key
                }.find {
                    // FIXME: For now only the symbol is checked, for nativeToken we should check the address too
                    it.symbol == token
                } ?: return@subscribe

                if (it.isNotEmpty() && balance > BigDecimal.ZERO) {
                    sendTokens(to, amount, tokenClassReference, payLoad)
                }
                // else fail???
            }.addTo(compositeDisposable)
    }

    private fun sendTokens(
        to: String,
        amount: BigDecimal,
        tokenClassReference: TokenClassReference,
        payLoad: String?
    ) {
        try {
            val r: Observable<AtomSubmissionUpdate> = if (payLoad != null) {
                Identity.api!!.sendTokens(
                    RadixAddress.from(to),
                    amount,
                    tokenClassReference, // Identity.api!!.nativeTokenRef
                    payLoad
                )
                    .toObservable()
                    .doOnError(::checkErrorAndShowToast)
            } else {
                Identity.api!!.sendTokens(
                    RadixAddress.from(to),
                    amount,
                    tokenClassReference
                ) // Identity.api!!.nativeTokenRef
                    .toObservable()
                    .doOnError(::checkErrorAndShowToast)
            }

            r.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.getState().isComplete) {
                        postValue(it.getState().name)
                    }
                    Timber.d("Network status is... $it")
                }, {
                    Timber.e(it, "There is an error!!!")
                    if (it is InsufficientFundsException) {
                        postValue(it.javaClass.simpleName)
                    }
                }).addTo(compositeDisposable)
        } catch (e: Exception) {
            Timber.e(e, "Error sending...")
            postValue(e.javaClass.simpleName)
        }
    }

    private fun checkErrorAndShowToast(it: Throwable) {
        if (it is RuntimeException) {
            if (it.cause is InsufficientFundsException) {
                postValue(it.javaClass.simpleName)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        compositeDisposable.clear()
    }
}
