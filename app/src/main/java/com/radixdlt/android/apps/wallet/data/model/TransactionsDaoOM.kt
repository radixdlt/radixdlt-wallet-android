package com.radixdlt.android.apps.wallet.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable

@Dao
interface TransactionsDaoOM {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransactions(transactionEntity: List<TransactionsEntityOM>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransaction(transactionEntity: TransactionsEntityOM)

    @Query("SELECT * FROM TransactionsEntityOM WHERE rri = :rri ORDER BY timestamp DESC")
    fun getAllTransactionsFromAsset(rri: String): Flowable<MutableList<TransactionsEntityOM>>

//    @Query("SELECT * FROM TransactionsEntityOM WHERE rri = :tokenType ORDER BY timestamp DESC")
//    fun getAllTransactionsByTokenType(tokenType: String): Maybe<List<TransactionsEntityOM>>
//
//    @Query("SELECT * FROM TransactionsEntityOM WHERE rri = :tokenType")
//    fun getAllTransactionsByTokenTypeFlowable(tokenType: String): Flowable<MutableList<TransactionsEntityOM>>
//
//    @Query("SELECT * FROM TransactionsEntityOM WHERE rri = :rri ORDER BY timestamp DESC LIMIT 1")
//    fun getLastTransactionByTokenType(rri: String): Single<TransactionsEntityOM>
//
//    @Query("SELECT * FROM TransactionsEntityOM WHERE rri = :rri ORDER BY timestamp DESC LIMIT 1")
//    fun getLatestTransactionByTokenType(rri: String): Flowable<TransactionsEntityOM>
//
//    @Query("SELECT DISTINCT rri FROM TransactionsEntityOM ORDER BY rri")
//    fun getAllAssets(): Flowable<MutableList<String>>
//
//    @Query("SELECT COUNT(accountAddress) FROM TransactionsEntityOM")
//    suspend fun transactionsCount(): Int
//
//    @Query("UPDATE TransactionsEntityOM SET tokenName = :tokenName, tokenDescription = :tokenDescription, tokenIconUrl = :tokenUrlIcon, tokenTotalSupply = :tokenTotalSupply, tokenGranularity = :tokenGranularity, tokenSupplyType = :tokenSupplyType WHERE rri = :rri")
//    fun updateEntities(tokenName: String, tokenDescription: String, tokenUrlIcon: String, rri: String, tokenTotalSupply: BigDecimal, tokenGranularity: BigDecimal, tokenSupplyType: String)

    @Query("DELETE FROM TransactionsEntityOM")
    fun deleteTable()

    @Update
    fun update(vararg transactionsEntityOM: TransactionsEntityOM)

    @Delete
    fun delete(vararg transactionsEntityOM: TransactionsEntityOM)
}
