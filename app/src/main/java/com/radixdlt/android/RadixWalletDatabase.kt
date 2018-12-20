package com.radixdlt.android

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.radixdlt.android.data.model.message.MessageEntity
import com.radixdlt.android.data.model.message.MessagesDao
import com.radixdlt.android.data.model.transaction.TransactionEntity
import com.radixdlt.android.data.model.transaction.TransactionsDao
import java.math.BigDecimal

@Database(
    entities = [TransactionEntity::class, MessageEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(RadixWalletDatabase.Converters::class)
abstract class RadixWalletDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionsDao
    abstract fun messagesDao(): MessagesDao

    class Converters {
        @TypeConverter
        fun stringFromBigDecimal(subUnits: BigDecimal): String =
            subUnits.toPlainString()

        @TypeConverter
        fun bigDecimalFromString(subUnitsString: String): BigDecimal =
            BigDecimal(subUnitsString)
    }
}
