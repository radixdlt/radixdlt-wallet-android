package com.radixdlt.android.apps.wallet.data.model.newtransaction

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
@Entity(
    primaryKeys = ["timestamp"]
)
data class TransactionEntity2(
    val account: String,
    @ColumnInfo(index = true) val address: String,
    val amount: BigDecimal,
    val message: String?,
    val sent: Boolean,
    val timestamp: Long,
    val rri: String,
    val tokenName: String?,
    val tokenDescription: String?,
    val tokenIconUrl: String?,
    val tokenTotalSupply: BigDecimal?,
    val tokenSupplyType: String?
) : Parcelable
