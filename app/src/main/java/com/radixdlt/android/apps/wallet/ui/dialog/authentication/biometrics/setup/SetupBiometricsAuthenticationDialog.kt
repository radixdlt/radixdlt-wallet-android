package com.radixdlt.android.apps.wallet.ui.dialog.authentication.biometrics.setup

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.DialogSetupBiometricsAuthenticationBinding
import com.radixdlt.android.apps.wallet.ui.dialog.authentication.AuthenticationDialog
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set

class SetupBiometricsAuthenticationDialog : AuthenticationDialog() {

    private val viewModelSetupAuthentication: SetupBiometricsAuthenticationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseViewModels()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        navigate()
    }

    override fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogSetupBiometricsAuthenticationBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.dialog_setup_biometrics_authentication,
                container,
                false
            )
        binding.viewmodel = viewModelSetupAuthentication
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun initialiseViewModels() {
        viewModelSetupAuthentication.setupBiometricsAuthenticationAction.observe(
            viewLifecycleOwner,
            Observer(::action)
        )
    }

    private fun action(setupPinAuthenticationAction: SetupBiometricsAuthenticationAction?) {
        when (setupPinAuthenticationAction) {
            SetupBiometricsAuthenticationAction.USE_BIOMETRICS -> {
                savePrefUseBiometrics()
                navigate()
            }
            SetupBiometricsAuthenticationAction.CANCEL -> navigate()
        }
    }

    private fun savePrefUseBiometrics() {
        activity?.apply {
            defaultPrefs()[Pref.USE_BIOMETRICS] = true
        }
    }
}
