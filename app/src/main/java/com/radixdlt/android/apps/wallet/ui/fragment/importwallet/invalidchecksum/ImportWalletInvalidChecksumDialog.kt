package com.radixdlt.android.apps.wallet.ui.fragment.importwallet.invalidchecksum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.ui.fragment.importwallet.ImportWalletAction
import com.radixdlt.android.apps.wallet.ui.fragment.importwallet.shared.ImportWalletSharedViewModel
import com.radixdlt.android.databinding.DialogImportWalletInvalidChecksumBinding

class ImportWalletInvalidChecksumDialog : FullScreenDialog() {

    private val viewModel: ImportWalletInvalidChecksumViewModel by viewModels()
    private val sharedViewModel: ImportWalletSharedViewModel by activityViewModels()

//    private lateinit var viewModel: ImportWalletSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogImportWalletInvalidChecksumBinding = DataBindingUtil
            .inflate(
                inflater,
                R.layout.dialog_import_wallet_invalid_checksum,
                container,
                false
        )
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.importWalletAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(action: ImportWalletAction) {
        when (action) {
            ImportWalletAction.OpenWallet -> continueOpening()
            ImportWalletAction.CloseDialog -> dismiss()
        }
    }

    private fun continueOpening() {
        sharedViewModel.continueClickListener()
        dismiss()
    }
}
