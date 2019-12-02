package com.radixdlt.android.apps.wallet.ui.fragment.settings.biometrics

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthentication
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthenticationResult
import com.radixdlt.android.apps.wallet.databinding.DialogAuthenticateBiometricsBinding
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.ui.fragment.settings.AuthenticateFunctionality
import com.radixdlt.android.apps.wallet.ui.fragment.settings.SettingsSharedViewModel
import com.radixdlt.android.apps.wallet.ui.fragment.settings.pin.AuthenticatePinDialogArgs
import timber.log.Timber

class AuthenticateBiometricsDialog : FullScreenDialog() {

    private val args: AuthenticatePinDialogArgs by navArgs()
    val functionality by lazy { args.functionality }

    private lateinit var ctx: Context
    private lateinit var biometricsAuthentication: BiometricsAuthentication

    private val paymentBiometricsViewModel: AuthenticateBiometricsViewModel by viewModels()
    private val settingsSharedViewModel: SettingsSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogAuthenticateBiometricsBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_authenticate_biometrics, container, false)
        binding.viewmodel = paymentBiometricsViewModel
        binding.lifecycleOwner = this
        binding.toolbarDialog.setNavigationOnClickListener { navigateBackToSettings() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseBiometricsAuthentication()
        observeLiveData()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Timber.tag("lifecycle").d("onCancel")
        navigateBackToSettings()
    }

    private fun initialiseBiometricsAuthentication() {
        lifecycleScope.launchWhenResumed {
            activity?.apply {
                biometricsAuthentication = BiometricsAuthentication(this, ::handleResult)
                biometricsAuthentication.authenticate()
            }
        }
    }

    private fun observeLiveData() {
        paymentBiometricsViewModel.paymentBiometricsAction.observe(
            viewLifecycleOwner,
            Observer(::action)
        )
    }

    private fun handleResult(result: BiometricsAuthenticationResult) {
        paymentBiometricsViewModel.setBiometricsAuthenticationResult(result)
        if (result is BiometricsAuthenticationResult.Success) {
            // navigateBackToSettings()
            findNavController().popBackStack()
            when (functionality) {
                AuthenticateFunctionality.BACKUP -> settingsSharedViewModel.backup()
                AuthenticateFunctionality.BIOMETRICS -> settingsSharedViewModel.useBiometrics()
                AuthenticateFunctionality.PAYMENT -> TODO()
            }
        }
    }

    private fun action(paymentPinAction: AuthenticateBiometricsAction?) {
        when (paymentPinAction) {
            AuthenticateBiometricsAction.USE_PIN -> {
                navigateBackToSettings()
                settingsSharedViewModel.usePin(functionality)
            }
            AuthenticateBiometricsAction.CANCEL -> {
                navigateBackToSettings()
            }
        }
    }

    private fun navigateBackToSettings() {
        // val action = AuthenticateBiometricsDialogDirections
        //     .navigationAuthenticateBiometricsDialogToNavigationSettings()
        // findNavController().navigate(action)
        settingsSharedViewModel.cancel()
        findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}
