package com.radixdlt.android.apps.wallet.ui.fragment.settings.pin

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.DialogAuthenticatePinBinding
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.ui.fragment.settings.AuthenticateFunctionality
import com.radixdlt.android.apps.wallet.ui.fragment.settings.SettingsSharedViewModel
import timber.log.Timber

class AuthenticatePinDialog : FullScreenDialog() {

    private val args: AuthenticatePinDialogArgs by navArgs()
    val functionality by lazy { args.functionality }

    private lateinit var ctx: Context

    private val authenticatePinViewModel: AuthenticatePinViewModel by viewModels()
    private val settingsSharedViewModel: SettingsSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogAuthenticatePinBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_authenticate_pin, container, false)
        binding.viewmodel = authenticatePinViewModel
        binding.lifecycleOwner = this
        binding.toolbarDialog.setNavigationOnClickListener { navigateBackToSettings() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseViewModels()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Timber.tag("lifecycle").d("onCancel")
        navigateBackToSettings()
    }

    private fun initialiseViewModels() {
        authenticatePinViewModel.paymentPinAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(paymentPinAction: AuthenticatePinAction?) {
        when (paymentPinAction) {
            AuthenticatePinAction.SUCCESS -> {
                findNavController().popBackStack()
                when (functionality) {
                    AuthenticateFunctionality.BACKUP -> settingsSharedViewModel.backup()
                    AuthenticateFunctionality.BIOMETRICS -> settingsSharedViewModel.useBiometrics()
                    AuthenticateFunctionality.PAYMENT -> TODO()
                }
            }
        }
    }

    private fun navigateBackToSettings() {
        // val action = AuthenticatePinDialogDirections
        //     .navigationAuthenticatePinDialogToNavigationSettings()
        // findNavController().navigate(action)
        settingsSharedViewModel.cancel()
        findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}
