package com.radixdlt.android.apps.wallet.ui.backupwallet.backup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.FragmentBackupWalletBinding
import com.radixdlt.android.apps.wallet.ui.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.main.MainViewModel
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import com.radixdlt.android.apps.wallet.util.showSnackbarAboveNavigationView
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class BackupWalletFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var ctx: Context

    private val backupWalletViewModel: BackupWalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        activity?.apply {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        if ((activity is MainActivity)) {
            (activity as MainActivity).setNavAndBottomNavigationVisible()
        }
        initialiseToolbar(R.string.backup_wallet_fragment_toolbar_title)
        initialiseViewModels()
        backupWalletViewModel.backupWalletAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentBackupWalletBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_backup_wallet, container, false)
        binding.viewmodel = backupWalletViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    private fun initialiseViewModels() {
        activity?.let {
            ViewModelProviders.of(it, viewModelFactory)[MainViewModel::class.java].apply {
                setBottomNavigationCheckedItem(R.id.menu_bottom_settings)
            }
        }
    }

    private fun action(action: BackupWalletAction?) {
        when (action) {
            is BackupWalletAction.ConfirmBackup -> navigateToConfirmBackup(action.mnemonic)
            BackupWalletAction.CopyMnemonic -> {
                showSnackbarAboveNavigationView(R.string.backup_wallet_fragment_copied_mnemonic)
            }
        }
    }

    private fun navigateToConfirmBackup(mnemonic: String) {
        val action = BackupWalletFragmentDirections
            .navigationBackupWalletToNavigationConfirmBackupWallet(mnemonic)
        findNavController().navigate(action)
    }
}
