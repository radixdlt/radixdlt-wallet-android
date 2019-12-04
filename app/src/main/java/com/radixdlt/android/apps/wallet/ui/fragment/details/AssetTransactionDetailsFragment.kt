package com.radixdlt.android.apps.wallet.ui.fragment.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.FragmentAssetTransactionDetailsBinding
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.showSnackbarAboveNavigationView

class AssetTransactionDetailsFragment : Fragment() {

    val args: AssetTransactionDetailsFragmentArgs by navArgs()
    private val assetTransactionDetailsViewModel by viewModels<AssetTransactionDetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentAssetTransactionDetailsBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_asset_transaction_details, container, false)
        binding.viewmodel = assetTransactionDetailsViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViewModel()
    }

    private fun initialiseViewModel() {
        assetTransactionDetailsViewModel.showTransactionDetails(args)
        assetTransactionDetailsViewModel.assetTransactionDetailsAction
            .observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(action: AssetTransactionDetailsAction) {
        when (action) {
            is AssetTransactionDetailsAction.CopyToClipboard -> copyAndShowSnackbar(action.message)
        }
    }

    private fun copyAndShowSnackbar(message: String) {
        view?.let {
            copyToClipboard(it.context, message)
            showSnackbarAboveNavigationView(message)
        }
    }
}
