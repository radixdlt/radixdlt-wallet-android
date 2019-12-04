package com.radixdlt.android.apps.wallet.ui.launch.pin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.FragmentLaunchPinBinding
import com.radixdlt.android.apps.wallet.ui.send.PaymentActivity
import com.radixdlt.android.apps.wallet.ui.send.PaymentViewModel
import com.radixdlt.android.apps.wallet.ui.receive.ReceiveViewModel
import com.radixdlt.android.apps.wallet.ui.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.main.MainViewModel

class LaunchPinFragment : Fragment() {

    private lateinit var ctx: Context

    private val launchPinViewModel: LaunchPinViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val receiveViewModel: ReceiveViewModel by activityViewModels()
    private val paymentViewModel: PaymentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentLaunchPinBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_launch_pin, container, false)
        binding.viewmodel = launchPinViewModel
        binding.lifecycleOwner = this
        // binding.toolbarDialog.setNavigationOnClickListener { dismiss() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseViewModels()
    }

    private fun initialiseViewModels() {
        launchPinViewModel.paymentPinAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(paymentPinAction: LaunchPinAction) {
        when (paymentPinAction) {
            LaunchPinAction.SUCCESS -> {
                if (activity is MainActivity) {
                    mainViewModel.unlock()
                } else if (activity is PaymentActivity) {
                    paymentViewModel.unlock()
                } else {
                    receiveViewModel.unlock()
                }
            }
            LaunchPinAction.LOGOUT -> {
                if (activity is MainActivity) {
                    mainViewModel.logout()
                } else if (activity is PaymentActivity) {
                    paymentViewModel.logout()
                } else {
                    receiveViewModel.logout()
                }
            }
        }
    }
}
