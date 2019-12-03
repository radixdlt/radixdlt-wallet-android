package com.radixdlt.android.apps.wallet.ui.fragment.payment.summary

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.FragmentPaymentSummaryBinding
import com.radixdlt.android.apps.wallet.ui.activity.PaymentAction
import com.radixdlt.android.apps.wallet.ui.activity.PaymentActivity
import com.radixdlt.android.apps.wallet.ui.activity.PaymentViewModel
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import org.jetbrains.anko.px2dip

class PaymentSummaryFragment : Fragment() {

    private lateinit var ctx: Context

    val args: PaymentSummaryFragmentArgs by navArgs()
    private val paymentSummaryViewModel: PaymentSummaryViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = initialiseDataBinding(inflater, container)
        initialiseToolbar()
        paymentSummaryViewModel.showTransactionSummary(args)

        return view
    }

    private fun initialiseToolbar() {
        initialiseToolbar(R.string.payment_summary_fragment_toolbar_title)
        (activity as? AppCompatActivity)?.apply {
            supportActionBar?.elevation = px2dip(0)
        }
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentPaymentSummaryBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_payment_summary, container, false)
        binding.viewmodel = paymentSummaryViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        (activity as PaymentActivity).setToolbarVisible()
        activity?.apply { paymentViewModel.paymentAction.observe(this, Observer(::action)) }
        paymentSummaryViewModel.paymentSummaryAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(action: PaymentAction?) {
        when (action) {
            PaymentAction.PAY -> showPaymentStatusDialog()
            PaymentAction.USE_PIN -> navigateToPinAuthentication()
        }
    }

    private fun action(action: PaymentSummaryAction) {
        when (action) {
            is PaymentSummaryAction.CopyToClipboard -> copyAndShowSnackbar(action.message)
            PaymentSummaryAction.Authenticate -> authenticate()
        }
    }

    private fun copyAndShowSnackbar(message: String) {
        view?.let {
            copyToClipboard(it.context, message)
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showPaymentStatusDialog() {
        val action = PaymentSummaryFragmentDirections
            .actionNavigationPaymentSummaryToNavigationPaymentStatus(
                args.addressTo,
                args.amount,
                args.rri,
                args.note
            )
        navigate(action)
    }

    private fun authenticate() {
        if (ctx.defaultPrefs()[Pref.USE_BIOMETRICS, false]) {
            navigateToBiometricsAuthentication()
        } else {
            navigateToPinAuthentication()
        }
    }

    private fun navigateToPinAuthentication() {
        val action = PaymentSummaryFragmentDirections
            .actionNavigationPaymentSummaryToNavigationPaymentPin()
        navigate(action)
    }

    private fun navigateToBiometricsAuthentication() {
        val action = PaymentSummaryFragmentDirections
            .actionNavigationPaymentSummaryToNavigationPaymentBiometrics()
        navigate(action)
    }

    private fun navigate(action: NavDirections) {
        lifecycleScope.launchWhenCreated {
            findNavController().navigate(action)
        }
    }
}
