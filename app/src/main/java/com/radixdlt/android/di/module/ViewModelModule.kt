package com.radixdlt.android.di.module

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.radixdlt.android.data.model.message.ContactsRepository
import com.radixdlt.android.data.model.message.ConversationRepository
import com.radixdlt.android.data.model.message.SendMessageLiveData
import com.radixdlt.android.data.model.transaction.BalanceLiveData
import com.radixdlt.android.data.model.transaction.TransactionDetails
import com.radixdlt.android.data.model.transaction.TransactionDetailsLiveData
import com.radixdlt.android.di.ViewModelFactory
import com.radixdlt.android.ui.activity.ConversationViewModel
import com.radixdlt.android.ui.activity.TransactionDetailsViewModel
import com.radixdlt.android.ui.fragment.ContactsViewModel
import com.radixdlt.android.ui.fragment.TransactionsViewModel
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
    abstract fun bindBalanceLiveData(balanceLiveData: BalanceLiveData): LiveData<String>

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
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
