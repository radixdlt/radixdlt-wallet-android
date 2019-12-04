package com.radixdlt.android.apps.wallet.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.DialogDeleteWalletBinding
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog

class SettingsDeleteWalletDialog : FullScreenDialog() {

    private val settingsDeleteWalletViewModel by viewModels<SettingsDeleteWalletViewModel>()
    private val settingsSharedViewModel by activityViewModels<SettingsSharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViewModels()
    }

    fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogDeleteWalletBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_delete_wallet, container, false)
        binding.viewmodel = settingsDeleteWalletViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    fun initialiseViewModels() {
        settingsDeleteWalletViewModel.deleteWallet.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(delete: Boolean) {
        if (delete) {
            settingsSharedViewModel.setDeleteWallet(true)
        }
        dismiss()
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}
