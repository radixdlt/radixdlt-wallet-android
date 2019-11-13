package com.radixdlt.android.apps.wallet.di.component

import android.app.Application
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.di.module.AndroidBuilderModule
import com.radixdlt.android.apps.wallet.di.module.RadixWalletModule
import com.radixdlt.android.apps.wallet.di.module.ViewModelModule
import com.radixdlt.android.apps.wallet.ui.di.module.DatabaseModule
import com.radixdlt.android.apps.wallet.ui.fragment.transactions.AssetTransactionsViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AndroidBuilderModule::class,
    RadixWalletModule::class,
    ViewModelModule::class,
    DatabaseModule::class,
    AssistedInjectModule::class
])
interface RadixWalletComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): RadixWalletComponent
    }

    fun inject(application: RadixWalletApplication)

    // Exposes the UserDetailModel.Factory object so we can reference the create()
    // method that takes a User ID String as an argument
    val userDetailViewModelFactory: AssetTransactionsViewModel.Factory
}

// Module annotated with "@AssistedModule" that references generated code (AssistedInject_AssistedInjectModule::class)
@AssistedModule
@Module(includes = [AssistedInject_AssistedInjectModule::class])
interface AssistedInjectModule
