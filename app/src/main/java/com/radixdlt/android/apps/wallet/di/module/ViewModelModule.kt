package com.radixdlt.android.apps.wallet.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.radixdlt.android.apps.wallet.di.ViewModelFactory
import com.radixdlt.android.apps.wallet.ui.main.MainViewModel
import com.radixdlt.android.apps.wallet.ui.main.assets.AssetsViewModel
import com.radixdlt.android.apps.wallet.ui.send.payment.assetselection.PaymentAssetSelectionViewModel
import com.radixdlt.android.apps.wallet.ui.send.payment.input.PaymentInputViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AssetsViewModel::class)
    abstract fun bindAssetsViewModel(viewModel: AssetsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaymentInputViewModel::class)
    abstract fun bindPaymentInputViewModel(viewModel: PaymentInputViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaymentAssetSelectionViewModel::class)
    abstract fun bindPaymentAssetSelectionViewModel(viewModel: PaymentAssetSelectionViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
