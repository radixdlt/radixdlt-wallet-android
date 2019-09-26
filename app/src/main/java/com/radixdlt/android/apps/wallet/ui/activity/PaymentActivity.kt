package com.radixdlt.android.apps.wallet.ui.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import com.radixdlt.android.R
import kotlinx.android.synthetic.main.activity_payment.*
import org.jetbrains.anko.startActivity

class PaymentActivity : BaseActivity() {

    private var addressExtra: String? = null
    private var tokenTypeExtra: String? = null
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        setSupportActionBar(toolbar as Toolbar)

        addressExtra = intent.getStringExtra(EXTRA_TRANSACTION_ADDRESS)
        tokenTypeExtra = intent.getStringExtra(EXTRA_TRANSACTION_TOKEN_TYPE)
        uri = intent.getParcelableExtra(EXTRA_URI)

        when {
            addressExtra != null -> navigateToPaymentInputWithAddressAndTokenData()
            uri != null -> navigateToPaymentInputWithUriData()
            else -> navigateToPaymentInput()
        }
    }

    private fun navigateToPaymentInput() {
        findNavController(R.id.nav_host_fragment_payment)
            .setGraph(R.navigation.nav_graph_payment)
    }

    private fun navigateToPaymentInputWithUriData() {
        Bundle().apply {
            putParcelable("uri", uri)
        }.also {
            findNavController(R.id.nav_host_fragment_payment)
                .setGraph(R.navigation.nav_graph_payment, it)
        }
    }

    private fun navigateToPaymentInputWithAddressAndTokenData() {
        Bundle().apply {
            putString("address", addressExtra)
            putString("token", tokenTypeExtra)
        }.also {
            findNavController(R.id.nav_host_fragment_payment)
                .setGraph(R.navigation.nav_graph_payment, it)
        }
    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.nav_host_fragment_payment).navigateUp()

    override fun onBackPressed() {
        val paymentInputFragment =
            findNavController(R.id.nav_host_fragment_payment).currentDestination?.id
        if (paymentInputFragment == R.id.navigation_payment_input) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val EXTRA_TRANSACTION_ADDRESS = "com.radixdlt.android.address"
        private const val EXTRA_TRANSACTION_TOKEN_TYPE = "com.radixdlt.android.token_type"
        private const val EXTRA_URI = "com.radixdlt.android.address"

        fun newIntent(ctx: Context) {
            ctx.startActivity<PaymentActivity>()
        }

        fun newIntent(ctx: Context, address: String, tokenType: String) {
            ctx.startActivity<PaymentActivity>(
                EXTRA_TRANSACTION_ADDRESS to address,
                EXTRA_TRANSACTION_TOKEN_TYPE to tokenType
            )
        }

        fun newIntent(ctx: Context, uri: Uri) {
            ctx.startActivity<PaymentActivity>(EXTRA_URI to uri)
        }
    }
}
