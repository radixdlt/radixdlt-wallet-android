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
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.activity.PaymentViewModel
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.databinding.DialogPaymentPinBinding
import kotlinx.android.synthetic.main.dialog_payment_pin.*

class PaymentPinDialog : FullScreenDialog() {

    private lateinit var ctx: Context

    private val paymentPinViewModel: PaymentPinViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        toolbarDialog.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbarDialog.setNavigationContentDescription(R.string.setup_pin_dialog_content_description_back_button)
        toolbarDialog.setNavigationOnClickListener { dismiss() }
        initialiseViewModels()
    }

    private fun initialiseViewModels() {
        paymentPinViewModel.paymentPinAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(paymentPinAction: PaymentPinAction?) {
        when(paymentPinAction) {
            PaymentPinAction.SUCCESS -> {
                dismiss()
                paymentViewModel.pay()

            }
        }
    }
}
