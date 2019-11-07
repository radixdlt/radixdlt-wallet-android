package com.radixdlt.android.apps.wallet.ui.dialog.authentication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.StartActivity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set
import java.io.File

abstract class AuthenticationDialog : FullScreenDialog() {

    lateinit var ctx: Context

    abstract fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View

    abstract fun initialiseViewModels()

    fun navigate() {
        when (activity) {
            is StartActivity -> {
                savePrefs()
                openWallet()
            }
            is MainActivity -> {
                dismiss()
                returnToStart()
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
        findNavController().popBackStack(R.id.navigation_backup_wallet, true)
    }
}
