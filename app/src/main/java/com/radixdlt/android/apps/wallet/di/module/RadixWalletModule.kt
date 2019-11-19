package com.radixdlt.android.apps.wallet.di.module

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.radixdlt.android.apps.wallet.connectivity.ConnectivityLiveData
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RadixWalletModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun providesConnectivityManager(application: Application): ConnectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    @Singleton
    fun providesConnectivityLiveData(app: Application) = ConnectivityLiveData(app)
}
