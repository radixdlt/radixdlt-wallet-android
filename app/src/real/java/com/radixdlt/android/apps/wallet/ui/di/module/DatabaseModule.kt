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
            Room.databaseBuilder(
                    context, RadixWalletDatabase::class.java, "RadixWalletDatabase"
            ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun providesTransactionDao(database: RadixWalletDatabase) = database.transactionDao()

    @Provides
    @Singleton
    fun providesTransactionDao2(database: RadixWalletDatabase) = database.transactionDao2()

    @Provides
    @Singleton
    fun providesMessagesDao(database: RadixWalletDatabase) = database.messagesDao()
}
