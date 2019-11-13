package com.radixdlt.android.apps.wallet.ui.dialog.authentication.pin.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsChecker
import com.radixdlt.android.apps.wallet.databinding.DialogSetupPinAuthenticationBinding
import com.radixdlt.android.apps.wallet.ui.dialog.authentication.AuthenticationDialog
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set

class SetupPinAuthenticationDialog : AuthenticationDialog() {

    private val viewModelSetupAuthentication: SetupPinAuthenticationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    override fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogSetupPinAuthenticationBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.dialog_setup_pin_authentication,
                container,
                false
            )
        binding.viewmodel = viewModelSetupAuthentication
        binding.lifecycleOwner = this
        binding.toolbarDialog.setNavigationOnClickListener { dismiss() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseViewModels()
    }

    override fun initialiseViewModels() {
        super.initialiseViewModels()
        viewModelSetupAuthentication.setupPinAuthenticationAction.observe(
            viewLifecycleOwner,
            Observer(::action)
        )
    }

    private fun action(setupPinAuthenticationAction: SetupPinAuthenticationAction?) {
        when (setupPinAuthenticationAction) {
            SetupPinAuthenticationAction.NAVIGATE -> {
                mainViewModel.showBackUpWalletNotification(false)
                savePrefWalletBackedUp()
                if (BiometricsChecker.getInstance(ctx).isUsingBiometrics) {
                    showSetupBiometricsDialog()
                } else {
                    navigate()
                }
            }
        }
    }

    private fun savePrefWalletBackedUp() {
        activity?.apply {
            defaultPrefs()[Pref.WALLET_BACKED_UP] = true
            defaultPrefs()[Pref.PIN_SET] = true
        }
    }

    private fun showSetupBiometricsDialog() {
        dismiss()
        findNavController().navigate(R.id.navigation_setup_biometrics)
    }
}
