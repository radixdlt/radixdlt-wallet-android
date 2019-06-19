package com.radixdlt.android.apps.wallet.di.module

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RadixWalletModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application
}
