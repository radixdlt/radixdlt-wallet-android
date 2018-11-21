package com.radixdlt.android

import androidx.room.Database
import androidx.room.RoomDatabase
import com.radixdlt.android.data.model.message.MessageEntity
import com.radixdlt.android.data.model.message.MessagesDao
import com.radixdlt.android.data.model.transaction.TransactionEntity
import com.radixdlt.android.data.model.transaction.TransactionsDao

@Database(entities = [TransactionEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class RadixWalletDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionsDao

    abstract fun messagesDao(): MessagesDao
}
