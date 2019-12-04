package com.radixdlt.android.apps.wallet.data.model.transaction

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransactions(transactionEntity: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransaction(transactionEntity: TransactionEntity)

    @Query("SELECT * FROM TransactionEntity WHERE rri = :rri ORDER BY timestamp DESC")
    fun getAllTransactionsFromAsset(rri: String): Flowable<MutableList<TransactionEntity>>

    @Query("DELETE FROM TransactionEntity")
    fun deleteTable()

    @Update
    fun update(vararg transactionEntity: TransactionEntity)

    @Delete
    fun delete(vararg transactionEntity: TransactionEntity)
}
