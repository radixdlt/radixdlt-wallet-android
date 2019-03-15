package com.radixdlt.android.di.module

import com.radixdlt.android.ui.activity.ConversationActivity
import com.radixdlt.android.ui.activity.EnterPasswordActivity
import com.radixdlt.android.ui.activity.ReceiveRadixInvoiceActivity
import com.radixdlt.android.ui.activity.SendRadixActivity
import com.radixdlt.android.ui.activity.TransactionDetailsActivity
import com.radixdlt.android.ui.fragment.ContactsFragment
import com.radixdlt.android.ui.fragment.MoreOptionsFragment
import com.radixdlt.android.ui.fragment.WalletFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AndroidBuilderModule {

    @ContributesAndroidInjector
    abstract fun bindWalletFragment(): WalletFragment

    @ContributesAndroidInjector
    abstract fun bindContactsFragment(): ContactsFragment

    @ContributesAndroidInjector
    abstract fun bindMoreOptionsFragment(): MoreOptionsFragment

    @ContributesAndroidInjector
    abstract fun bindConversationActivity(): ConversationActivity

    @ContributesAndroidInjector
    abstract fun bindTransactionDetailsActivity(): TransactionDetailsActivity

    @ContributesAndroidInjector
    abstract fun bindEnterPasswordActivity(): EnterPasswordActivity

    @ContributesAndroidInjector
    abstract fun bindSendRadixActivity(): SendRadixActivity

    @ContributesAndroidInjector
    abstract fun bindReceiveRadixInvoiceActivity(): ReceiveRadixInvoiceActivity
}
