package com.radixdlt.android.apps.wallet

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.radixdlt.android.apps.wallet.data.model.asset.AssetDao
import com.radixdlt.android.apps.wallet.data.model.asset.AssetEntity
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionDao
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionEntity
import java.math.BigDecimal

@Database(
    entities = [
        AssetEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RadixWalletDatabase.Converters::class)
abstract class RadixWalletDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun transactionsDao(): TransactionDao

    class Converters {
        @TypeConverter
        fun stringFromBigDecimal(subUnits: BigDecimal?): String? = subUnits?.toPlainString()

        @TypeConverter
        fun bigDecimalFromString(subUnitsString: String?): BigDecimal? =
            subUnitsString?.let { BigDecimal(it) }
    }
}
