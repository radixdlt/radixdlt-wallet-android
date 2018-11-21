package com.radixdlt.android.di.component

import android.app.Application
import com.radixdlt.android.RadixWalletApplication
import com.radixdlt.android.di.module.AndroidBuilderModule
import com.radixdlt.android.di.module.DatabaseModule
import com.radixdlt.android.di.module.RadixWalletModule
import com.radixdlt.android.di.module.ViewModelModule
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
