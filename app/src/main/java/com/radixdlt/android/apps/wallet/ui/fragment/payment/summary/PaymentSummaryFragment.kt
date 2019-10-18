package com.radixdlt.android.apps.wallet.ui.fragment.payment.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import com.radixdlt.android.databinding.FragmentPaymentSummaryBinding
import org.jetbrains.anko.px2dip

class PaymentSummaryFragment : Fragment() {

    private val args: PaymentSummaryFragmentArgs by navArgs()
    private val viewModel: PaymentSummaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = initialiseDataBinding(inflater, container)
        initialiseToolbar()
        viewModel.showTransactionSummary(args)

        return view
    }

    private fun initialiseToolbar() {
        initialiseToolbar(R.string.payment_summary_fragment_toolbar_title)
        activity?.apply {
            (this as AppCompatActivity).supportActionBar?.elevation = px2dip(0)
        }
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentPaymentSummaryBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_payment_summary, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.paymentSummaryAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(action: PaymentSummaryAction) {
        when (action) {
            is PaymentSummaryAction.CopyToClipboard -> copyAndShowSnackbar(action.message)
            PaymentSummaryAction.ShowLoadingDialog -> showDialog()
        }
    }

    private fun copyAndShowSnackbar(message: String) {
        view?.let {
            copyToClipboard(it.context, message)
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showDialog() {
        val action = PaymentSummaryFragmentDirections
            .actionNavigationPaymentSummaryToNavigationPaymentStatus(
                args.addressTo,
                args.amount,
                args.rri,
                args.note
            )

        findNavController().navigate(action)
    }
}
