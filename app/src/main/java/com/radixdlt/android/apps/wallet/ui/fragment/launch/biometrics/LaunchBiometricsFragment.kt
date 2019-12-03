package com.radixdlt.android.apps.wallet.ui.fragment.launch.biometrics

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthentication
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthenticationResult
import com.radixdlt.android.apps.wallet.databinding.FragmentLaunchBiometricsBinding
import com.radixdlt.android.apps.wallet.ui.activity.PaymentActivity
import com.radixdlt.android.apps.wallet.ui.activity.PaymentViewModel
import com.radixdlt.android.apps.wallet.ui.activity.ReceivePaymentViewModel
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainViewModel

class LaunchBiometricsFragment : Fragment() {

    private lateinit var ctx: Context
    private lateinit var biometricsAuthentication: BiometricsAuthentication

    private val launchBiometricsViewModel: LaunchBiometricsViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val receivePaymentViewModel: ReceivePaymentViewModel by activityViewModels()
    private val paymentViewModel: PaymentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentLaunchBiometricsBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_launch_biometrics, container, false)
        binding.viewmodel = launchBiometricsViewModel
        binding.lifecycleOwner = this
        // binding.toolbarDialog.setNavigationOnClickListener { dismiss() }

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
        launchBiometricsViewModel.paymentBiometricsAction.observe(
            viewLifecycleOwner,
            Observer(::action)
        )
    }

    private fun handleResult(result: BiometricsAuthenticationResult) {
        launchBiometricsViewModel.setBiometricsAuthenticationResult(result)
        if (result is BiometricsAuthenticationResult.Success) {
            // dismiss()
            if (activity is MainActivity) {
                mainViewModel.unlock()
            } else if (activity is PaymentActivity) {
                paymentViewModel.unlock()
            } else {
                receivePaymentViewModel.unlock()
            }
        }
    }

    private fun action(paymentPinAction: LaunchBiometricsAction?) {
        when (paymentPinAction) {
            LaunchBiometricsAction.USE_PIN -> {
                if (activity is MainActivity) {
                    mainViewModel.usePin()
                } else if (activity is PaymentActivity) {
                    paymentViewModel.usePinAuthentication()
                } else {
                    receivePaymentViewModel.usePin()
                }
            }
            LaunchBiometricsAction.LOGOUT -> {
                if (activity is MainActivity) {
                    mainViewModel.logout()
                } else if (activity is PaymentActivity) {
                    paymentViewModel.logout()
                } else {
                    receivePaymentViewModel.logout()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::biometricsAuthentication.isInitialized) {
            Handler().post {
                biometricsAuthentication.authenticate()
            }
        }
    }
}
