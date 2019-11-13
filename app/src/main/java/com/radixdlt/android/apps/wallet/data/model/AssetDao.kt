package com.radixdlt.android.apps.wallet.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal

@Dao
interface AssetDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAssets(assets: List<AssetEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAsset(asset: AssetEntity)

    @Query("SELECT * FROM AssetEntity ORDER BY tokenName")
    fun getAllAssets(): Flowable<MutableList<AssetEntity>>

    @Query("SELECT * FROM AssetEntity WHERE rri = :rri")
    fun getAsset(rri: String): Single<AssetEntity>

    @Query("SELECT * FROM AssetEntity WHERE rri = :rri")
    fun getAssetFlowable(rri: String): Flowable<AssetEntity>

//    @Query("SELECT COUNT(accountAddress) FROM TransactionEntity2")
//    suspend fun transactionsCount(): Int

    @Query("DELETE FROM AssetEntity")
    fun deleteTable()

    @Query("UPDATE AssetEntity SET tokenName = :tokenName, tokenDescription = :tokenDescription, tokenIconUrl = :tokenUrlIcon, tokenTotalSupply = :tokenTotalSupply, tokenGranularity = :tokenGranularity, tokenSupplyType = :tokenSupplyType WHERE rri = :rri")
    fun updateEntities(rri: String, tokenName: String, tokenDescription: String, tokenUrlIcon: String, tokenTotalSupply: BigDecimal, tokenGranularity: BigDecimal, tokenSupplyType: String)

    @Query("UPDATE AssetEntity SET amount = amount + :amount WHERE rri = :rri")
    fun updateEntityAmount(rri: String, amount: BigDecimal)
}
