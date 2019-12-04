package com.radixdlt.android.apps.wallet.ui.di.module

import android.content.Context
import androidx.room.Room
import com.radixdlt.android.apps.wallet.RadixWalletDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun providesRadixWalletDatabaseDatabase(context: Context): RadixWalletDatabase =
            Room.inMemoryDatabaseBuilder(context, RadixWalletDatabase::class.java).build()

    @Provides
    @Singleton
    fun providesAssetDao(database: RadixWalletDatabase) = database.assetDao()

    @Provides
    @Singleton
    fun providesTransactionsDao(database: RadixWalletDatabase) = database.transactionsDao()
}
