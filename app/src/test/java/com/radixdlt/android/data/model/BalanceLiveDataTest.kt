package com.radixdlt.android.data.model

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.radixdlt.android.data.model.transaction.BalanceLiveData
import com.radixdlt.android.data.model.transaction.TransactionEntity
import com.radixdlt.android.data.model.transaction.TransactionsDao
import com.radixdlt.android.extension.ImmediateSchedulersExtension
import com.radixdlt.android.extension.InstantTaskExecutorExtension
import com.radixdlt.android.util.TOTAL
import com.radixdlt.client.atommodel.tokens.TokenClassReference
import io.reactivex.Flowable
import io.reactivex.Maybe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions

@Extensions(
    ExtendWith(InstantTaskExecutorExtension::class),
    ExtendWith(ImmediateSchedulersExtension::class)
)
class BalanceLiveDataTest {
    private val lifecycleOwner: LifecycleOwner = mock()

    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val observer: Observer<String> = mock()
    private val transactionsDao: TransactionsDao = mock()

    private val balanceLiveData: BalanceLiveData = BalanceLiveData(transactionsDao)
    private val xrdToken = "XRD"
    private val w00tToken = "W00T"

    private val transactionEntity = TransactionEntity(
        "Test", 100L,
        "+100", "message", false, 1000000,
        xrdToken, TokenClassReference.getSubunits()
    )

    private val transactionEntity2 = TransactionEntity(
        "Test2", 11L,
        "11", "message", true, 1000001,
        xrdToken, TokenClassReference.getSubunits()
    )

    private val transactionEntity3 = TransactionEntity(
        "Test3", 12L,
        "+12", "message", false, 1000002,
        xrdToken, TokenClassReference.getSubunits()
    )

    private val transactionEntity4 = TransactionEntity(
        "Test4", 102L,
        "+102", "message", false, 1000003,
        w00tToken, TokenClassReference.getSubunits()
    )

    @BeforeEach
    fun setup() {
        whenever(lifecycleOwner.lifecycle).thenReturn(lifecycleRegistry)
        balanceLiveData.observe(lifecycleOwner, observer)
    }

    @Nested
    @DisplayName("Given a BalanceLiveData which is not active")
    inner class Inactive {
        private val transactionEntities = mutableListOf<TransactionEntity>()

        @BeforeEach
        fun setup() {
            transactionEntities.add(transactionEntity)
            whenever(transactionsDao.getAllTransactions()).thenReturn(
                Maybe.just(transactionEntities)
            )
            whenever(transactionsDao.getLatestTransaction()).thenReturn(
                Flowable.just(transactionEntity)
            )
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        @Nested
        @DisplayName("When the balance is updated")
        inner class NeverActive {

            @BeforeEach
            fun setup() {
                balanceLiveData.retrieveWalletBalance(TOTAL)
            }

            @Test
            @DisplayName("Then we do not receive a postValue callback")
            fun noCallback() {
                verify(observer, never()).onChanged(any())
            }
        }
    }

    @Nested
    @DisplayName("Given a BalanceLiveData which is active")
    inner class Active {

        @BeforeEach
        fun setup() {
        }

        @Nested
        @DisplayName("When there have not been any transactions")
        inner class EmptyTransactions {
            private val transactionEntities = mutableListOf<TransactionEntity>()

            @BeforeEach
            fun setup() {
                whenever(transactionsDao.getAllTransactions()).thenReturn(
                    Maybe.just(transactionEntities)
                )
                whenever(transactionsDao.getLatestTransaction()).thenReturn(
                    Flowable.empty()
                )

                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            @Test
            @DisplayName("Then we do not receive a postValue callback")
            fun observerCallback() {
                verify(observer, never()).onChanged(any())
            }
        }

        @Nested
        @DisplayName("When the balance is updated")
        inner class Balance {
            private val transactionEntities = mutableListOf<TransactionEntity>()

            @BeforeEach
            fun setup() {
                transactionEntities.add(transactionEntity)
                whenever(transactionsDao.getAllTransactions()).thenReturn(
                    Maybe.just(transactionEntities)
                )
                whenever(transactionsDao.getLatestTransaction()).thenReturn(
                    Flowable.just(transactionEntity)
                )

                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            @Test
            @DisplayName("Then view receives a single postValue callback")
            fun observerCallback() {
                verify(observer, times(1)).onChanged(any())
                verify(observer, times(1)).onChanged("100")
            }
        }

        @Nested
        @DisplayName("When last retrieved transaction is different and sent")
        inner class NewTransactionSent {
            private val transactionEntities = mutableListOf<TransactionEntity>()

            @BeforeEach
            fun setup() {
                transactionEntities.add(transactionEntity)
                whenever(transactionsDao.getAllTransactions()).thenReturn(
                    Maybe.just(transactionEntities)
                )
                whenever(transactionsDao.getLatestTransaction()).thenReturn(
                    Flowable.just(transactionEntity2)
                )

                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            @Test
            @DisplayName("Then view receives two postValue callback")
            fun observerCallback() {
                // Number of times postValue is called
                verify(observer, times(2)).onChanged(any())

                // Values that postValue should return when called
                verify(observer, times(1)).onChanged("100")
                verify(observer, times(1)).onChanged("89")
            }
        }

        @Nested
        @DisplayName("When last retrieved transaction is different and received")
        inner class NewTransactionReceived {
            private val transactionEntities = mutableListOf<TransactionEntity>()

            @BeforeEach
            fun setup() {
                transactionEntities.add(transactionEntity)
                whenever(transactionsDao.getAllTransactions()).thenReturn(
                    Maybe.just(transactionEntities)
                )
                whenever(transactionsDao.getLatestTransaction()).thenReturn(
                    Flowable.just(transactionEntity3)
                )

                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            @Test
            @DisplayName("Then view receives two postValue callback")
            fun observerCallback() {
                // Number of times postValue is called
                verify(observer, times(2)).onChanged(any())

                // Values that postValue should return when called
                verify(observer, times(1)).onChanged("100")
                verify(observer, times(1)).onChanged("112")
            }
        }

        @Nested
        @DisplayName("When a different token is selected")
        inner class TokenSelected {
            private val transactionEntities = mutableListOf<TransactionEntity>()
            private val transactionEntitiesTokens = mutableListOf<TransactionEntity>()

            @BeforeEach
            fun setup() {
                transactionEntities.add(transactionEntity)
                transactionEntities.add(transactionEntity4)
                whenever(transactionsDao.getAllTransactions()).thenReturn(
                    Maybe.just(transactionEntities)
                )
                whenever(transactionsDao.getLatestTransaction()).thenReturn(
                    Flowable.just(transactionEntity4)
                )

                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

                transactionEntitiesTokens.add(transactionEntity4)
                whenever(transactionsDao.getAllTransactionsByTokenType(w00tToken)).thenReturn(
                    Maybe.just(transactionEntitiesTokens)
                )
                whenever(transactionsDao.getLatestTransactionByTokenType(w00tToken)).thenReturn(
                    Flowable.just(transactionEntity4)
                )
                balanceLiveData.retrieveWalletBalance(w00tToken)
            }

            @Test
            @DisplayName("Then the view receives two postValue callbacks")
            fun observerCallback() {
                // Number of times postValue is called
                verify(observer, times(2)).onChanged(any())

                // Values that postValue should return when called
                verify(observer, times(1)).onChanged("202")
                verify(observer, times(1)).onChanged("102")
            }
        }
    }

    @Nested
    @DisplayName("Given a BalanceLiveData which was active")
    inner class WasActive {
        private val transactionEntities = mutableListOf<TransactionEntity>()

        @BeforeEach
        fun setup() {
            whenever(transactionsDao.getAllTransactions()).thenReturn(Maybe.empty())
            whenever(transactionsDao.getLatestTransaction()).thenReturn(Flowable.empty())

            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }

        @Nested
        @DisplayName("When the balance is updated")
        inner class BalanceUpdate {

            @BeforeEach
            fun setup() {
                transactionEntities.add(transactionEntity)
                transactionEntities.add(transactionEntity4)
                whenever(transactionsDao.getAllTransactions()).thenReturn(
                    Maybe.just(transactionEntities)
                )
                whenever(transactionsDao.getLatestTransaction()).thenReturn(
                    Flowable.just(transactionEntity4)
                )
                balanceLiveData.retrieveWalletBalance(TOTAL)
            }

            @Test
            @DisplayName("Then we do not receive a postValue callback")
            fun noCallback() {
                verify(observer, never()).onChanged(any())
            }
        }
    }
}
