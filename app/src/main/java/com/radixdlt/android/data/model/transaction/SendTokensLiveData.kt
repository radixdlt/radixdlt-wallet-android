package com.radixdlt.android.data.model.transaction

import androidx.lifecycle.LiveData
import com.radixdlt.android.identity.Identity
import com.radixdlt.client.application.translate.tokens.InsufficientFundsException
import com.radixdlt.client.application.translate.tokens.TokenUnitConversions
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.core.atoms.particles.RRI
import com.radixdlt.client.core.network.actions.SubmitAtomAction
import com.radixdlt.client.core.network.actions.SubmitAtomResultAction
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class SendTokensLiveData @Inject constructor(
    // TODO: Use in future where token will be inserted immediately and have a pending state until
    // confirmed by the network.
    private val transactionsDao: TransactionsDao
) : LiveData<String>() {

    private val compositeDisposable = CompositeDisposable()
    private var balanceChecked = false

    fun sendToken(to: String, amount: BigDecimal, token: String, payLoad: String?) {
        balanceChecked = false
        Identity.api!!.getBalance(Identity.api!!.getMyAddress())
            .subscribe {
                // FIXME: currently getBalance() has a bit of an erratic behaviour. Check again with future updates!
                if (balanceChecked) {
                    return@subscribe
                }
                balanceChecked = true
                val tokenClassReference = it.map { map ->
                    map
                }.find {
                    it.key.toString() == token
                } ?: run {
                    postValue("ERROR")
                    return@subscribe
                }

                if (it.isNotEmpty() && tokenClassReference.value > BigDecimal.ZERO) {
                    Timber.tag("SEND_TOKENS").d("$to, $amount, ${tokenClassReference.key}, $payLoad")
                    sendTokens(to, amount, tokenClassReference.key, payLoad)
                } else {
                    postValue("ERROR")
                }
            }.addTo(compositeDisposable) // TODO: Maybe dispose after initial balance check!
    }

    private fun sendTokens(
        to: String,
        amount: BigDecimal,
        rri: RRI,
        payLoad: String?
    ) {
        try {
            val r: Observable<SubmitAtomAction> = if (payLoad != null) {
                Identity.api!!.transferTokens(
                    RadixAddress.from(to),
                    amount,
                    rri, // Identity.api!!.nativeTokenRef
                    payLoad
                )
                    .toObservable()
                    .doOnError(::checkErrorAndShowToast)
            } else {
                Identity.api!!.transferTokens(
                    RadixAddress.from(to),
                    amount,
                    rri
                ) // Identity.api!!.nativeTokenRef
                    .toObservable()
                    .doOnError(::checkErrorAndShowToast)
            }

            r.subscribeOn(Schedulers.io())
                .subscribe({
                    if (it is SubmitAtomResultAction) {
//                        if (it.type == SubmitAtomResultAction.SubmitAtomResultActionType.STORED) {
//                             TODO: Will likely need the atom hid to do this which I now can!!
//                            val transactionEntity = createTransactionEntity(
//                                amount, to, payLoad, it, tokenClassReference
//                            )
//                            transactionsDao.insertTransaction(transactionEntity)
//                            postValue(it.type.name)
//                            return@subscribe
//                        }
                        postValue(it.type.name)
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

    private fun createTransactionEntity(
        amount: BigDecimal,
        to: String,
        payLoad: String?,
        it: SubmitAtomAction,
        rri: RRI
    ): TransactionEntity {
        val amountFormatted = amount.setScale(
            5, RoundingMode.HALF_UP
        ).toPlainString()

        return TransactionEntity(
            to,
            amount.toLong(),
            amountFormatted,
            payLoad,
            true,
            it.atom.timestamp,
            rri.name,
            TokenUnitConversions.getSubunits()
        )
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
