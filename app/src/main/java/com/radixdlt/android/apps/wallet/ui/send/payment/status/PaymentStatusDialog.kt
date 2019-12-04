package com.radixdlt.android.apps.wallet.ui.send.payment.status

import android.content.Context
import android.content.DialogInterface
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
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.DialogPaymentStatusBinding
import com.radixdlt.android.apps.wallet.helper.CustomTabsHelper
import com.radixdlt.android.apps.wallet.helper.WebviewFallback
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.URL_PRIVACY_POLICY

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createCustomTabsBuilder(view.context)
        viewModel.paymentStatusAction.observe(viewLifecycleOwner, Observer(::action))
        // For performance reasons where UI thread would be momentarily blocked wait until onResume
        lifecycleScope.launchWhenResumed {
            viewModel.makePayment(args)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        closePayment()
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogPaymentStatusBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_payment_status, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return binding.root
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
        dismiss()
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
        if (defaultPrefs()[Pref.AUTHENTICATE_ON_LAUNCH, false]) {
            closePayment()
        }
    }
}
