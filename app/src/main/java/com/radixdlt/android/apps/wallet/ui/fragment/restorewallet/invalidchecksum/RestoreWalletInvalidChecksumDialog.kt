package com.radixdlt.android.apps.wallet.ui.fragment.restorewallet.invalidchecksum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.ui.fragment.restorewallet.restore.RestoreWalletAction
import com.radixdlt.android.apps.wallet.ui.fragment.restorewallet.shared.RestoreWalletSharedViewModel
import com.radixdlt.android.apps.wallet.databinding.DialogRestoreWalletInvalidChecksumBinding

class RestoreWalletInvalidChecksumDialog : FullScreenDialog() {

    private val viewModel: RestoreWalletInvalidChecksumViewModel by viewModels()
    private val sharedViewModel: RestoreWalletSharedViewModel by activityViewModels()

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
        val binding: DialogRestoreWalletInvalidChecksumBinding = DataBindingUtil
            .inflate(inflater, R.layout.dialog_restore_wallet_invalid_checksum, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.restoreWalletAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(action: RestoreWalletAction) {
        when (action) {
            is RestoreWalletAction.ShowSetupPinDialog -> continueOpening()
            RestoreWalletAction.CloseDialog -> dismiss()
        }
    }

    private fun continueOpening() {
        sharedViewModel.continueClickListener()
        dismiss()
    }
}
