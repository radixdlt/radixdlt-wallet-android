package com.radixdlt.android.apps.wallet.data.model.transaction

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import com.radixdlt.android.apps.wallet.data.model.asset.AssetEntity
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
@Entity(
    primaryKeys = ["timestamp"],
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["rri"],
            childColumns = ["rri"],
            onDelete = CASCADE
        )
    ]
)
data class TransactionEntity(
    val accountAddress: String,
    val accountName: String,
    @ColumnInfo(index = true) val address: String,
    val amount: BigDecimal,
    val message: String?,
    val sent: Boolean,
    val timestamp: Long,
    @ColumnInfo(index = true) val rri: String
) : Parcelable
