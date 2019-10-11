package com.radixdlt.android.apps.wallet.ui.fragment.restorewallet.restore

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.fragment.restorewallet.shared.RestoreWalletSharedAction
import com.radixdlt.android.apps.wallet.ui.fragment.restorewallet.shared.RestoreWalletSharedViewModel
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import com.radixdlt.android.apps.wallet.util.showErrorSnackbar
import com.radixdlt.android.databinding.FragmentRestoreWalletBinding
import kotlinx.android.synthetic.main.fragment_restore_wallet.*
import java.io.File

class RestoreWalletFragment : Fragment() {

    private lateinit var ctx: Context

    private val restoreWalletViewModel: RestoreWalletViewModel by viewModels()
    private val sharedViewModel: RestoreWalletSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseToolbar(toolbar, R.string.restore_wallet_fragment_toolbar_title)
        restoreWalletViewModel.restoreWalletAction.observe(viewLifecycleOwner, Observer(::action))
        sharedViewModel.restoreWalletSharedAction.observe(viewLifecycleOwner, Observer(::sharedAction))
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentRestoreWalletBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_restore_wallet, container, false)
        binding.viewmodel = restoreWalletViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    private fun sharedAction(action: RestoreWalletSharedAction) {
        when (action) {
            RestoreWalletSharedAction.OpenWallet -> restoreWalletViewModel.importWallet()
        }
    }

    private fun action(action: RestoreWalletAction) {
        when (action) {
            RestoreWalletAction.PasteMnemonic -> pasteMnemonic()
            RestoreWalletAction.ShowMnemonicError -> {
                showErrorSnackbar(R.string.restore_wallet_fragment_mnemonic_error)
            }
            RestoreWalletAction.ShowDialog -> showDialog()
            is RestoreWalletAction.OpenWallet -> {
                savePrefs(action.address)
                openWallet()
            }
        }
    }

    private fun pasteMnemonic() {
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip?.getItemAt(0)?.let {
            val mnemonicArray = it.text.trim().split(" ").toTypedArray()
            if (mnemonicArray.size != 12) {
                showErrorSnackbar(R.string.restore_wallet_fragment_mnemonic_error)
            } else {
                restoreWalletViewModel.setPastedMnemonic(mnemonicArray)
            }
        }
    }

    private fun showDialog() {
        val action = RestoreWalletFragmentDirections
            .navigationImportWalletToNavigationImportWalletInvalidChecksum()
        findNavController().navigate(action)
    }

    private fun openWallet() {
        MainActivity.newIntent(ctx)
        activity?.finish()
    }

    private fun savePrefs(address: String) {
        activity?.apply {
            defaultPrefs()[Pref.ADDRESS] = address
            defaultPrefs()[Pref.PASSWORD] = false
            defaultPrefs()[Pref.MNEMONIC_SEED] = true
            defaultPrefs()[Pref.WALLET_BACKED_UP] = true
            File(filesDir, "keystore.key").createNewFile() // creating dummy file for now
        }
    }
}
