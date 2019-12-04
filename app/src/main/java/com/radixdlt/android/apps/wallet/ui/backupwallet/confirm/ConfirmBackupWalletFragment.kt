package com.radixdlt.android.apps.wallet.ui.backupwallet.confirm

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.FragmentConfirmBackupWalletBinding
import com.radixdlt.android.apps.wallet.ui.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.main.MainViewModel
import com.radixdlt.android.apps.wallet.ui.main.settings.SettingsSharedViewModel
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import com.radixdlt.android.apps.wallet.util.showErrorSnackbarAboveNavigationView
import com.radixdlt.android.apps.wallet.util.showSuccessSnackbarAboveNavigationView
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ConfirmBackupWalletFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val args: ConfirmBackupWalletFragmentArgs by navArgs()

    private lateinit var ctx: Context

    private val confirmbackupWalletViewModel by viewModels<ConfirmBackupWalletViewModel>()
    private val settingsSharedViewModel by activityViewModels<SettingsSharedViewModel>()

    private val mainViewModel by activityViewModels<MainViewModel> {
        viewModelFactory
    }

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
        confirmbackupWalletViewModel.showMnemonic(args)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        if ((activity is MainActivity)) {
            (activity as MainActivity).setNavAndBottomNavigationVisible()
        }
        initialiseToolbar(R.string.confirm_backup_wallet_fragment_toolbar_title)
        initialiseViewModels()
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentConfirmBackupWalletBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_confirm_backup_wallet,
                container,
                false
            )
        binding.viewmodel = confirmbackupWalletViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    private fun initialiseViewModels() {
        settingsSharedViewModel.popAuthenticationSetupBackStack.observe(viewLifecycleOwner,
            Observer { popAuthenticationBackStack() }
        )
        confirmbackupWalletViewModel.confirmBackupWalletAction.observe(
            viewLifecycleOwner, Observer(::action)
        )
    }

    private fun action(action: ConfirmBackupWalletAction) = when (action) {
        ConfirmBackupWalletAction.ShowMnemonicError -> {
            showErrorSnackbarAboveNavigationView(R.string.confirm_backup_wallet_fragment_mnemonic_error)
        }
        ConfirmBackupWalletAction.Navigate -> {
            if (ctx.defaultPrefs()[Pref.PIN_SET, false]) {
                navigateToStart()
            } else {
                navigateToSetupPin()
            }
        }
    }

    private fun popAuthenticationBackStack() {
        lifecycleScope.launchWhenResumed {
            showSuccessSnackbarAboveNavigationView(
                R.string.settings_fragment_change_backup_and_security_success_snackbar
            )
            findNavController().popBackStack(R.id.navigation_backup_wallet, true)
        }
    }

    private fun navigateToSetupPin() {
        findNavController().navigate(R.id.navigation_setup_pin)
    }

    private fun navigateToStart() {
        lifecycleScope.launchWhenResumed {
            showSuccessSnackbarAboveNavigationView(R.string.confirm_backup_wallet_fragment_mnemonic_success)
            mainViewModel.showBackUpWalletNotification(false)
            findNavController().popBackStack(R.id.navigation_backup_wallet, true)
        }
    }
}
