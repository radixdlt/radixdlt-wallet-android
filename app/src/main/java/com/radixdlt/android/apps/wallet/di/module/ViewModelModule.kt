package com.radixdlt.android.apps.wallet.di.module

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.radixdlt.android.apps.wallet.data.model.message.ContactsRepository
import com.radixdlt.android.apps.wallet.data.model.message.ConversationRepository
import com.radixdlt.android.apps.wallet.data.model.message.SendMessageLiveData
import com.radixdlt.android.apps.wallet.data.model.transaction.BalanceLiveData
import com.radixdlt.android.apps.wallet.data.model.transaction.SendTokensLiveData
import com.radixdlt.android.apps.wallet.data.model.transaction.TokenTypesLiveData
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionDetails
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionDetailsLiveData
import com.radixdlt.android.apps.wallet.di.ViewModelFactory
import com.radixdlt.android.apps.wallet.ui.activity.ConversationViewModel
import com.radixdlt.android.apps.wallet.ui.activity.SendTokensViewModel
import com.radixdlt.android.apps.wallet.ui.activity.TransactionDetailsViewModel
import com.radixdlt.android.apps.wallet.ui.fragment.contacts.ContactsViewModel
import com.radixdlt.android.apps.wallet.ui.fragment.wallet.TransactionsViewModel
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetsLiveData
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetsViewModel
import com.radixdlt.android.apps.wallet.ui.fragment.transactions.AssetTransactionsViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Named
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
    @ViewModelKey(TransactionsViewModel::class)
    abstract fun bindTransactionViewModel(viewModel: TransactionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AssetsViewModel::class)
    abstract fun bindAssetsViewModel(viewModel: AssetsViewModel): ViewModel

    @Binds
    @Named("assets")
    abstract fun bindAssetsLiveData(assetsLiveData: AssetsLiveData): AssetsLiveData

    @Binds
    @IntoMap
    @ViewModelKey(AssetTransactionsViewModel::class)
    abstract fun bindAssetTransactionsViewModel(viewModel: AssetTransactionsViewModel): ViewModel

    @Binds
    @Named("balance")
    abstract fun bindBalanceLiveData(balanceLiveData: BalanceLiveData): BalanceLiveData

    @Binds
    @Named("contacts")
    abstract fun bindContactsRepository(
        contactsRepository: ContactsRepository
    ): ContactsRepository

    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    abstract fun bindMessageViewModel(viewModel: ContactsViewModel): ViewModel

    @Binds
    @Named("sendMessage")
    abstract fun bindSendMessageLiveData(
        sendMessageLiveData: SendMessageLiveData
    ): SendMessageLiveData

    @Binds
    @Named("conversation")
    abstract fun bindConversationRepository(
        conversationRepository: ConversationRepository
    ): ConversationRepository

    @Binds
    @IntoMap
    @ViewModelKey(ConversationViewModel::class)
    abstract fun bindConversationViewModel(viewModel: ConversationViewModel): ViewModel

    @Binds
    abstract fun bindTransactionDetailsRepository(
        transactionDetailsLiveData: TransactionDetailsLiveData
    ): LiveData<TransactionDetails>

    @Binds
    @IntoMap
    @ViewModelKey(TransactionDetailsViewModel::class)
    abstract fun bindTransactionDetailsViewModel(viewModel: TransactionDetailsViewModel): ViewModel

    @Binds
    @Named("tokenTypes")
    abstract fun bindTokenTypesLiveData(
        tokenTypesLiveData: TokenTypesLiveData
    ): TokenTypesLiveData

    @Binds
    @Named("sendToken")
    abstract fun bindSendTokensLiveData(
        sendTokensLiveData: SendTokensLiveData
    ): SendTokensLiveData

    @Binds
    @IntoMap
    @ViewModelKey(SendTokensViewModel::class)
    abstract fun bindSendTokensViewModel(viewModel: SendTokensViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
