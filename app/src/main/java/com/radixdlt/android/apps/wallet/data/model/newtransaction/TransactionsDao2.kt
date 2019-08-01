package com.radixdlt.android.apps.wallet.data.model.newtransaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import java.math.BigDecimal

@Dao
interface TransactionsDao2 {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransactions(transactionEntity: List<TransactionEntity2>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransaction(transactionEntity: TransactionEntity2)

    @Query("SELECT * FROM TransactionEntity2")
    fun getAllTransactions(): Maybe<MutableList<TransactionEntity2>>

    @Query("SELECT * FROM TransactionEntity2 WHERE rri = :tokenType")
    fun getAllTransactionsByTokenType(tokenType: String): Maybe<List<TransactionEntity2>>

    @Query("SELECT * FROM TransactionEntity2 WHERE rri = :tokenType")
    fun getAllTransactionsByTokenTypeFlowable(tokenType: String): Flowable<MutableList<TransactionEntity2>>

    @Query("SELECT * FROM TransactionEntity2 WHERE rri = :rri ORDER BY timestamp DESC LIMIT 1")
    fun getLastTransactionByTokenType(rri: String): Single<TransactionEntity2>

    @Query("SELECT * FROM TransactionEntity2 WHERE rri = :rri ORDER BY timestamp DESC LIMIT 1")
    fun getLatestTransactionByTokenType(rri: String): Flowable<TransactionEntity2>

    @Query("SELECT * FROM TransactionEntity2 WHERE address = :address AND rri = :rri ORDER BY timestamp DESC")
    fun getAllTransactionsByAddressAndToken(address: String, rri: String): Maybe<MutableList<TransactionEntity2>>

    @Query("SELECT DISTINCT rri FROM TransactionEntity2 ORDER BY rri")
    fun getAllAssets(): Flowable<MutableList<String>>

    @Query("DELETE FROM TransactionEntity2")
    fun deleteTable()

    @Query("UPDATE TransactionEntity2 SET tokenName = :tokenName, tokenDescription = :tokenDescription, tokenIconUrl = :tokenUrlIcon, tokenTotalSupply = :tokenTotalSupply, tokenSupplyType = :tokenSupplyType WHERE rri = :rri")
    fun updateEntities(tokenName: String, tokenDescription: String, tokenUrlIcon: String, rri: String, tokenTotalSupply: BigDecimal, tokenSupplyType: String)
}
