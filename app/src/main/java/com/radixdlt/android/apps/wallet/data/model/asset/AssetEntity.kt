package com.radixdlt.android.apps.wallet.data.model.asset

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
@Entity(
    primaryKeys = ["rri"]
)
data class AssetEntity(
    val accountAddress: String,
    @ColumnInfo(index = true) val rri: String,
    val tokenName: String?,
    val tokenDescription: String?,
    val tokenIconUrl: String?,
    val tokenTotalSupply: BigDecimal?,
    val tokenGranularity: BigDecimal?,
    val tokenSupplyType: String?,
    val amount: BigDecimal
) : Parcelable
