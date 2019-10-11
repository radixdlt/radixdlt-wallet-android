package com.radixdlt.android.apps.wallet.ui.fragment.createwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set
import com.radixdlt.android.databinding.FragmentCreateWalletBinding
import java.io.File

class CreateWalletFragment : Fragment() {

    private val createWalletViewModel: CreateWalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.apply {
            window?.clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createWalletViewModel.createWalletAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentCreateWalletBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_wallet, container, false)
        binding.viewmodel = createWalletViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    private fun action(action: CreateWalletAction?) {
        when (action) {
            is CreateWalletAction.OpenWallet -> {
                savePrefs(action.address)
                openWallet()
            }
            CreateWalletAction.ImportWallet -> importWallet()
        }
    }

    private fun savePrefs(address: String) {
        activity?.apply {
            defaultPrefs()[Pref.ADDRESS] = address
            defaultPrefs()[Pref.PASSWORD] = false
            defaultPrefs()[Pref.MNEMONIC_SEED] = true
            defaultPrefs()[Pref.WALLET_BACKED_UP] = false
            File(filesDir, "keystore.key").createNewFile() // creating dummy file for now
        }
    }

    private fun openWallet() {
        activity?.apply {
            MainActivity.newIntent(this)
            finish()
        }
    }

    private fun importWallet() {
        val action = CreateWalletFragmentDirections
            .actionNavigationCreateWalletToNavigationImportWallet()
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        activity?.apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
