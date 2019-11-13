package com.radixdlt.android.apps.wallet.ui.dialog.authentication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainViewModel
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set
import dagger.android.support.AndroidSupportInjection
import java.io.File
import javax.inject.Inject

abstract class AuthenticationDialog : FullScreenDialog() {

    lateinit var ctx: Context

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var mainViewModel: MainViewModel

    abstract fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View

    open fun initialiseViewModels() {
        activity?.apply {
            mainViewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    fun navigate() {
        when (activity) {
            is StartActivity -> {
                savePrefs()
                openWallet()
            }
            is MainActivity -> {
                returnToStart()
                dismiss()
            }
        }
    }

    private fun savePrefs() {
        activity?.apply {
            defaultPrefs()[Pref.ADDRESS] = Identity.api?.address?.toString()
            defaultPrefs()[Pref.PASSWORD] = false
            defaultPrefs()[Pref.MNEMONIC_SEED] = true
            defaultPrefs()[Pref.WALLET_BACKED_UP] = true
            File(filesDir, "keystore.key").createNewFile() // creating dummy file for now
        }
    }

    private fun openWallet() {
        MainActivity.newIntent(ctx)
        activity?.finish()
    }

    private fun returnToStart() {
        mainViewModel.popAuthenticationSetupBackStack()
    }
}
