package com.radixdlt.android.apps.wallet.ui.fragment.payment.pin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.DialogPaymentPinBinding
import com.radixdlt.android.apps.wallet.ui.activity.PaymentViewModel
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog

class PaymentPinDialog : FullScreenDialog() {

    private lateinit var ctx: Context

    private val paymentPinViewModel: PaymentPinViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogPaymentPinBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_payment_pin, container, false)
        binding.viewmodel = paymentPinViewModel
        binding.lifecycleOwner = this
        binding.toolbarDialog.setNavigationOnClickListener { dismiss() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseViewModels()
    }

    private fun initialiseViewModels() {
        paymentPinViewModel.paymentPinAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(paymentPinAction: PaymentPinAction?) {
        when (paymentPinAction) {
            PaymentPinAction.SUCCESS -> {
                dismiss()
                paymentViewModel.pay()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}
