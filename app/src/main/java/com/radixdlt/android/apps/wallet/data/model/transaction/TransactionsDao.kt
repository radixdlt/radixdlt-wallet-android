package com.radixdlt.android.apps.wallet.data.model.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface TransactionsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransactions(transactionEntity: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransaction(transactionEntity: TransactionEntity)

    @Query("SELECT * FROM TransactionEntity")
    fun getAllTransactions(): Maybe<MutableList<TransactionEntity>>

    @Query("SELECT * FROM TransactionEntity WHERE tokenClassISO = :tokenType")
    fun getAllTransactionsByTokenType(tokenType: String): Maybe<List<TransactionEntity>>

    @Query("SELECT * FROM TransactionEntity ORDER BY dateUnix DESC LIMIT 1")
    fun getLatestTransaction(): Flowable<TransactionEntity>

    @Query("SELECT * FROM TransactionEntity WHERE tokenClassISO = :tokenType ORDER BY dateUnix DESC LIMIT 1")
    fun getLatestTransactionByTokenType(tokenType: String): Flowable<TransactionEntity>

    @Query("SELECT * FROM TransactionEntity WHERE address = :address AND tokenClassISO = :token ORDER BY dateUnix DESC")
    fun getAllTransactionsByAddressAndToken(address: String, token: String): Maybe<MutableList<TransactionEntity>>

    @Query("SELECT DISTINCT tokenClassISO FROM TransactionEntity ORDER BY tokenClassISO")
    fun getAllTokenTypes(): Flowable<MutableList<String>>

    @Query("DELETE FROM TransactionEntity")
    fun deleteTable()
}
