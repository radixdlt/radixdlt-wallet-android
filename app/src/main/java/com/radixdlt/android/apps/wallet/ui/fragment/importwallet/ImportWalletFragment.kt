package com.radixdlt.android.apps.wallet.ui.fragment.importwallet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.fragment.importwallet.shared.ImportWalletSharedAction
import com.radixdlt.android.apps.wallet.ui.fragment.importwallet.shared.ImportWalletSharedViewModel
import com.radixdlt.android.apps.wallet.util.config
import com.radixdlt.android.databinding.FragmentImportWalletBinding
import kotlinx.android.synthetic.main.fragment_import_wallet.*

class ImportWalletFragment : Fragment() {

    private lateinit var ctx: Context

    private val importWalletViewModel: ImportWalletViewModel by viewModels()
    private val sharedViewModel: ImportWalletSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseToolbar()
        importWalletViewModel.importWalletAction.observe(viewLifecycleOwner, Observer(::action))
        sharedViewModel.importWalletAction.observe(viewLifecycleOwner, Observer(::sharedAction))
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentImportWalletBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_import_wallet, container, false)
        binding.viewmodel = importWalletViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    private fun sharedAction(action: ImportWalletSharedAction) {
        when (action) {
            ImportWalletSharedAction.OpenWallet -> importWalletViewModel.importWallet()
        }
    }

    private fun action(action: ImportWalletAction) {
        when (action) {
            ImportWalletAction.ShowMnemonicError -> showSnackbar()
            ImportWalletAction.ShowDialog -> showDialog()
            ImportWalletAction.OpenWallet -> openWallet()
        }
    }

    private fun showDialog() {
        val action = ImportWalletFragmentDirections
            .navigationImportWalletToNavigationImportWalletInvalidChecksum()
        findNavController().navigate(action)
    }

    private fun openWallet() {
        MainActivity.newIntent(ctx)
        activity?.finish()
    }

    private fun showSnackbar() {
        view?.let {
            val sb = Snackbar.make(
                it.rootView,
                getString(R.string.import_wallet_fragment_mnemonic_error),
                Snackbar.LENGTH_SHORT
            )
            sb.config(it.context)
            sb.show()
        }
    }

    private fun initialiseToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar as Toolbar)
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.import_wallet_fragment_toolbar_title)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
    }
}
