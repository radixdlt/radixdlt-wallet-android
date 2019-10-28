package com.radixdlt.android.apps.wallet.di.module

import com.radixdlt.android.apps.wallet.ui.activity.ConversationActivity
import com.radixdlt.android.apps.wallet.ui.activity.EnterPasswordActivity
import com.radixdlt.android.apps.wallet.ui.activity.ReceiveRadixInvoiceActivity
import com.radixdlt.android.apps.wallet.ui.activity.SendRadixActivity
import com.radixdlt.android.apps.wallet.ui.activity.TransactionDetailsActivity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.dialog.pin.setup.SetupPinDialog
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetsFragment
import com.radixdlt.android.apps.wallet.ui.fragment.backupwallet.backup.BackupWalletFragment
import com.radixdlt.android.apps.wallet.ui.fragment.backupwallet.confirm.ConfirmBackupWalletFragment
import com.radixdlt.android.apps.wallet.ui.fragment.contacts.ContactsFragment
import com.radixdlt.android.apps.wallet.ui.fragment.moreoptions.MoreOptionsFragment
import com.radixdlt.android.apps.wallet.ui.fragment.payment.assetselection.PaymentAssetSelectionFragment
import com.radixdlt.android.apps.wallet.ui.fragment.payment.input.PaymentInputFragment
import com.radixdlt.android.apps.wallet.ui.fragment.transactions.AssetTransactionsFragment
import com.radixdlt.android.apps.wallet.ui.fragment.wallet.WalletFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AndroidBuilderModule {

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindWalletFragment(): WalletFragment

    @ContributesAndroidInjector
    abstract fun bindAssetsFragment(): AssetsFragment

    @ContributesAndroidInjector
    abstract fun bindAssetTransactionsFragment(): AssetTransactionsFragment

    @ContributesAndroidInjector
    abstract fun bindPaymentInputFragment(): PaymentInputFragment

    @ContributesAndroidInjector
    abstract fun bindPaymentAssetSelectionFragment(): PaymentAssetSelectionFragment

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

    @ContributesAndroidInjector
    abstract fun bindBackupWalletFragment(): BackupWalletFragment

    @ContributesAndroidInjector
    abstract fun bindConfirmBackupWalletFragment(): ConfirmBackupWalletFragment

    @ContributesAndroidInjector
    abstract fun bindPinDialog(): SetupPinDialog
}
