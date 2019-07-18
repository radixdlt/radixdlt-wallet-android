package com.radixdlt.android.apps.wallet

import android.app.Activity
import android.app.Application
import android.util.Base64
import androidx.fragment.app.Fragment
import com.jakewharton.threetenabp.AndroidThreeTen
import com.radixdlt.android.apps.wallet.di.component.DaggerRadixWalletComponent
import com.radixdlt.android.apps.wallet.di.component.RadixWalletComponent
import com.radixdlt.android.apps.wallet.identity.BootStrapConfigAndroidImpl
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.Vault
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import org.jetbrains.anko.dip
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

import javax.inject.Inject

class RadixWalletApplication : Application(), HasActivityInjector, HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    private val radixWalletComponent: RadixWalletComponent by lazy {
        DaggerRadixWalletComponent.builder()
            .application(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        radixWalletComponent.inject(this)

        // Choose what Radix network to bootstrap
        val bootstrapConfig = BootStrapConfigAndroidImpl.radixBetanetNode(this)
        Identity.init(bootstrapConfig)

        Timber.plant(Timber.DebugTree())

        // initialize vault
        Vault.initialiseVault(this)
        generateEncryptionKey()

        AndroidThreeTen.init(this)

        densityPixel = dip(250)
    }

    /**
     * Generate encryption key and store it in vault
     */
    private fun generateEncryptionKey() {
        if (Vault.getVault().getString(Vault.ENCRYPTION_KEY_NAME, null) == null) {
            Vault.getVault()
                .edit()
                .putString(
                    Vault.ENCRYPTION_KEY_NAME,
                    Base64.encodeToString(Vault.generateKey(), Base64.DEFAULT)
                )
                .apply()
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    fun startActivityTransitionTimer() {
        activityTransitionTimer = Timer()
        activityTransitionTimerTask = object : TimerTask() {
            override fun run() {
                wasInBackground = true
            }
        }

        activityTransitionTimer?.schedule(
            activityTransitionTimerTask,
            QueryPreferences.getPrefAutoLockTimeOut(this)
        )
    }

    fun stopActivityTransitionTimer() {
        activityTransitionTimerTask?.cancel()
        activityTransitionTimer?.cancel()

        wasInBackground = false
    }

    companion object {
        var densityPixel: Int? = null
        private var activityTransitionTimer: Timer? = null
        private var activityTransitionTimerTask: TimerTask? = null
        var wasInBackground: Boolean = false
    }
}
