package com.radixdlt.android.apps.wallet.di.component

import android.app.Application
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.di.module.AndroidBuilderModule
import com.radixdlt.android.apps.wallet.di.module.RadixWalletModule
import com.radixdlt.android.apps.wallet.di.module.ViewModelModule
import com.radixdlt.android.apps.wallet.ui.di.module.DatabaseModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AndroidBuilderModule::class,
    RadixWalletModule::class,
    ViewModelModule::class,
    DatabaseModule::class
])
interface RadixWalletComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): RadixWalletComponent
    }

    fun inject(application: RadixWalletApplication)
}
