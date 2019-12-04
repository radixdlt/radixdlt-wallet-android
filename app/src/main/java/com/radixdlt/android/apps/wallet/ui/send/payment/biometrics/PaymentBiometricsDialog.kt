package com.radixdlt.android.apps.wallet.ui.send.payment.biometrics

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthentication
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthenticationResult
import com.radixdlt.android.apps.wallet.databinding.DialogPaymentBiometricsBinding
import com.radixdlt.android.apps.wallet.ui.send.PaymentViewModel
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog

class PaymentBiometricsDialog : FullScreenDialog() {

    private lateinit var ctx: Context
    private lateinit var biometricsAuthentication: BiometricsAuthentication

    private val paymentBiometricsViewModel: PaymentBiometricsViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogPaymentBiometricsBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_payment_biometrics, container, false)
        binding.viewmodel = paymentBiometricsViewModel
        binding.lifecycleOwner = this
        binding.toolbarDialog.setNavigationOnClickListener { dismiss() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseBiometricsAuthentication()
        observeLiveData()
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
            dismiss()
            paymentViewModel.pay()
        }
    }

    private fun action(paymentPinAction: PaymentBiometricsAction?) {
        when (paymentPinAction) {
            PaymentBiometricsAction.USE_PIN -> {
                dismiss()
                paymentViewModel.usePin()
            }
            PaymentBiometricsAction.CANCEL -> {
                dismiss()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}
