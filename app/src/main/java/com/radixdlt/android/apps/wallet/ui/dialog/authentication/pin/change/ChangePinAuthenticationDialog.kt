package com.radixdlt.android.apps.wallet.ui.dialog.authentication.pin.change

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.DialogChangePinAuthenticationBinding
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.ui.fragment.settings.SettingsSharedViewModel

class ChangePinAuthenticationDialog : FullScreenDialog() {

    private val changePinAuthenticationViewModel: ChangePinAuthenticationViewModel by viewModels()
    private val settingsSharedViewModel: SettingsSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogChangePinAuthenticationBinding =
            DataBindingUtil.inflate(
                inflater, R.layout.dialog_change_pin_authentication, container, false
            )
        binding.viewmodel = changePinAuthenticationViewModel
        binding.lifecycleOwner = this
        binding.toolbarDialog.setNavigationOnClickListener { dismiss() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        activity?.apply {
            changePinAuthenticationViewModel.changePinAuthenticationAction.observe(this, Observer {
                settingsSharedViewModel.showChangedPinSnackbar()
                dismiss()
            })
        }
    }

    override fun onPause() {
        dismiss()
        super.onPause()
    }
}
