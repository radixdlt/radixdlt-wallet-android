package com.radixdlt.android.apps.wallet.ui.fragment.backupwallet.confirm

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.activity.main.MainViewModel
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import com.radixdlt.android.apps.wallet.util.showErrorSnackbarAboveNavigationView
import com.radixdlt.android.apps.wallet.util.showSuccessSnackbarAboveNavigationView
import com.radixdlt.android.databinding.FragmentConfirmBackupWalletBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfirmBackupWalletFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel

    private val args: ConfirmBackupWalletFragmentArgs by navArgs()

    private lateinit var ctx: Context

    private val backupWalletViewModel: ConfirmBackupWalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = initialiseDataBinding(inflater, container)
        backupWalletViewModel.showMnemonic(args)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseToolbar(R.string.confirm_backup_wallet_fragment_toolbar_title)
        initialiseViewModels()
        backupWalletViewModel.confirmBackupWalletAction.observe(
            viewLifecycleOwner,
            Observer(::action)
        )
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentConfirmBackupWalletBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_confirm_backup_wallet,
                container,
                false
            )
        binding.viewmodel = backupWalletViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    private fun initialiseViewModels() {
        activity?.apply {
            mainViewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]
        }
    }

    private fun action(action: ConfirmBackupWalletAction) = when (action) {
        ConfirmBackupWalletAction.ShowMnemonicError -> {
            showErrorSnackbarAboveNavigationView(R.string.confirm_backup_wallet_fragment_mnemonic_error)
        }
        ConfirmBackupWalletAction.Return -> {
            savePrefWalletBackedUp()
            returnToStart()
        }
    }

    private fun savePrefWalletBackedUp() {
        activity?.apply {
            defaultPrefs()[Pref.WALLET_BACKED_UP] = true
        }
    }

    private fun returnToStart() {
        lifecycleScope.launch {
            showSuccessSnackbarAboveNavigationView(R.string.confirm_backup_wallet_fragment_mnemonic_success)
            delay(1500)
            mainViewModel.showBackUpWalletNotification(false)
            findNavController().popBackStack(R.id.navigation_backup_wallet, true)
        }
    }
}
