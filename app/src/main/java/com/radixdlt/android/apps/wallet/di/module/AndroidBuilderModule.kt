package com.radixdlt.android.apps.wallet.di.module

import com.radixdlt.android.apps.wallet.ui.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.dialog.authentication.biometrics.setup.SetupBiometricsAuthenticationDialog
import com.radixdlt.android.apps.wallet.ui.dialog.authentication.pin.setup.SetupPinAuthenticationDialog
import com.radixdlt.android.apps.wallet.ui.main.assets.AssetsFragment
import com.radixdlt.android.apps.wallet.ui.backupwallet.backup.BackupWalletFragment
import com.radixdlt.android.apps.wallet.ui.backupwallet.confirm.ConfirmBackupWalletFragment
import com.radixdlt.android.apps.wallet.ui.send.payment.assetselection.PaymentAssetSelectionFragment
import com.radixdlt.android.apps.wallet.ui.send.payment.input.PaymentInputFragment
import com.radixdlt.android.apps.wallet.ui.main.settings.SettingsFragment
import com.radixdlt.android.apps.wallet.ui.main.transactions.AssetTransactionsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AndroidBuilderModule {

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindAssetsFragment(): AssetsFragment

    @ContributesAndroidInjector
    abstract fun bindAssetTransactionsFragment(): AssetTransactionsFragment

    @ContributesAndroidInjector
    abstract fun bindPaymentInputFragment(): PaymentInputFragment

    @ContributesAndroidInjector
    abstract fun bindPaymentAssetSelectionFragment(): PaymentAssetSelectionFragment

    @ContributesAndroidInjector
    abstract fun bindSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun bindBackupWalletFragment(): BackupWalletFragment

    @ContributesAndroidInjector
    abstract fun bindConfirmBackupWalletFragment(): ConfirmBackupWalletFragment

    @ContributesAndroidInjector
    abstract fun bindSetupPinAuthenticationDialog(): SetupPinAuthenticationDialog

    @ContributesAndroidInjector
    abstract fun bindSetupBiometricsAuthenticationDialog(): SetupBiometricsAuthenticationDialog
}
