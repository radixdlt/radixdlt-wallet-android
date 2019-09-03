package com.radixdlt.android.apps.wallet.ui.fragment.payment.status

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.helper.CustomTabsHelper
import com.radixdlt.android.apps.wallet.helper.WebviewFallback
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.util.URL_PRIVACY_POLICY
import com.radixdlt.android.databinding.DialogPaymentStatusBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentStatusDialog : FullScreenDialog() {

    private val args: PaymentStatusDialogArgs by navArgs()
    private val viewModel: PaymentStatusViewModel by viewModels()

    private lateinit var customTabsIntent: CustomTabsIntent

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
        val binding: DialogPaymentStatusBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_payment_status, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createCustomTabsBuilder(view.context)
        viewModel.paymentStatusAction.observe(viewLifecycleOwner, Observer(::action))
        // For performance reasons where UI thread would be momentarily blocked, dispatch to
        // a background thread
        lifecycleScope.launch(Dispatchers.Default) {
            viewModel.makePayment(args)
        }
    }

    private fun createCustomTabsBuilder(context: Context) {
        customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setShowTitle(true)
            .enableUrlBarHiding()
            .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back))
            .build()
    }

    private fun action(action: PaymentStatusAction) {
        when (action) {
            PaymentStatusAction.ClosePaymentAction -> closePayment()
            PaymentStatusAction.TryPaymentAgainAction -> tryAgain()
            PaymentStatusAction.OpenExplorerAction -> openExplorer()
        }
    }

    private fun closePayment() {
        activity?.finish()
    }

    private fun tryAgain() {
        findNavController().popBackStack(R.id.navigation_payment_input, false)
    }

    private fun openExplorer() {
        activity?.apply {
            CustomTabsHelper.openCustomTab(
                this,
                customTabsIntent,
                Uri.parse(URL_PRIVACY_POLICY),
                WebviewFallback()
            )
        }
    }
}
