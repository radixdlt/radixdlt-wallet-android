package com.radixdlt.android.apps.wallet.ui.send

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.NavArgs
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.ui.main.LaunchAuthenticationAction
import com.radixdlt.android.apps.wallet.ui.send.payment.input.PaymentInputFragment
import com.radixdlt.android.apps.wallet.ui.send.payment.input.PaymentInputFragmentArgs
import com.radixdlt.android.apps.wallet.ui.send.payment.summary.PaymentSummaryFragment
import com.radixdlt.android.apps.wallet.ui.send.payment.summary.PaymentSummaryFragmentArgs
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.Pref.set
import kotlinx.android.synthetic.main.activity_payment.*
import org.jetbrains.anko.startActivity

class PaymentActivity : AppCompatActivity() {

    private val paymentViewModel by viewModels<PaymentViewModel>()

    private val navController by lazy {
        Navigation.findNavController(this, R.id.nav_host_fragment_payment)
    }

    private val graph by lazy {
        navController.navInflater.inflate(R.navigation.nav_graph_payment)
    }

    private var addressExtra: String? = null
    private var tokenTypeExtra: String? = null
    private var uri: Uri? = null

    var args: NavArgs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        setSupportActionBar(toolbar as Toolbar)
        initialiseViewModel()

        addressExtra = intent.getStringExtra(EXTRA_TRANSACTION_ADDRESS)
        tokenTypeExtra = intent.getStringExtra(EXTRA_TRANSACTION_TOKEN_TYPE)
        uri = intent.getParcelableExtra(EXTRA_URI)

        when {
            addressExtra != null -> navigateToPaymentInputWithAddressAndTokenData()
            uri != null -> navigateToPaymentInputWithUriData()
            else -> navigateToPaymentInput()
        }
    }

    private fun initialiseViewModel() {
        paymentViewModel.launchAuthenticationAction.observe(
            this,
            Observer(::launchAuthenticationAction)
        )
    }

    private fun setGraph(@IdRes idRes: Int) {
        graph.startDestination = idRes
        navController.graph = graph
    }

    private fun unlock() {
        val fragmentId = if (defaultPrefs()[Pref.LOCK_ACTIVE, false]) {
            defaultPrefs()[Pref.FRAGMENT_ID, R.id.navigation_payment_input]
        } else {
            R.id.navigation_payment_input
        }

        if (fragmentId != R.id.navigation_payment_input) {
            navigateTo(fragmentId)
        } else {
            val bundle = (args as PaymentInputFragmentArgs).toBundle()
            graph.startDestination = R.id.navigation_payment_input
            navController.setGraph(graph, bundle)
        }
        lockActive = false
    }

    private fun navigateTo(fragmentId: Int) {
        when (fragmentId) {
            R.id.navigation_payment_summary -> {
                val bundle = Bundle().also {
                    it.putString("addressFrom", (args as PaymentSummaryFragmentArgs).addressFrom)
                    it.putString("addressTo", (args as PaymentSummaryFragmentArgs).addressTo)
                    it.putString("amount", (args as PaymentSummaryFragmentArgs).amount)
                    it.putString("rri", (args as PaymentSummaryFragmentArgs).rri)
                    it.putString("note", (args as PaymentSummaryFragmentArgs).note)
                }
                graph.startDestination = R.id.navigation_payment_input
                navController.setGraph(graph, bundle)
                navController.navigate(fragmentId, bundle)
            }
            R.id.navigation_payment_asset_selection -> {
                graph.startDestination = R.id.navigation_payment_input
                navController.graph = graph
                navController.navigate(fragmentId)
            }
            R.id.navigation_payment_status -> {
                finish()
            }
            else -> {
                navController.navigate(fragmentId, null)
            }
        }
    }

    private fun launchAuthenticationAction(action: LaunchAuthenticationAction) {
        when (action) {
            LaunchAuthenticationAction.UNLOCK -> unlock()
            LaunchAuthenticationAction.USE_PIN -> usePin()
            LaunchAuthenticationAction.LOGOUT -> logout()
        }
    }

    fun setToolbarVisible() {
        toolbar.visibility = View.VISIBLE
    }

    private fun usePin() {
        setGraph(R.id.navigation_launch_pin)
    }

    private fun logout() {
        navController.navigate(R.id.navigation_settings_delete_wallet)
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

    override fun onResume() {
        super.onResume()

        if (RadixWalletApplication.wasInBackground && !openedShareDialog &&
            !lockActive
        ) {
            lockActive = true
            if (defaultPrefs()[Pref.AUTHENTICATE_ON_LAUNCH, false]) {
                toolbar.visibility = View.GONE
                if (defaultPrefs()[Pref.USE_BIOMETRICS, false]) {
                    setGraph(R.id.navigation_launch_biometrics)
                } else {
                    setGraph(R.id.navigation_launch_pin)
                }
            }
        }

        (this.application as RadixWalletApplication).stopActivityTransitionTimer()
    }

    override fun onPause() {
        super.onPause()
        if (!defaultPrefs()[Pref.AUTHENTICATE_ON_LAUNCH, false] ||
            !defaultPrefs()[Pref.MNEMONIC_SEED, false] || lockActive
        ) {

            return
        }

        defaultPrefs()[Pref.FRAGMENT_ID] = navController.currentDestination?.id
        defaultPrefs()[Pref.LOCK_ACTIVE] = true
        when (navController.currentDestination?.id) {
            R.id.navigation_payment_summary -> {
                val navHost =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_payment)
                navHost?.let { navFragment ->
                    navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                        this.args = (fragment as PaymentSummaryFragment).args
                    }
                }
            }
            R.id.navigation_payment_input -> {
                val navHost =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_payment)
                navHost?.let { navFragment ->
                    navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                        this.args = (fragment as PaymentInputFragment).saveDataForAuthentication()
                    }
                }
            }
        }
        (this.application as RadixWalletApplication).startActivityTransitionTimer()
    }

    override fun onDestroy() {
        (application as RadixWalletApplication).stopActivityTransitionTimer()
        defaultPrefs()[Pref.FRAGMENT_ID] = R.id.navigation_assets
        defaultPrefs()[Pref.LOCK_ACTIVE] = false
        super.onDestroy()
    }

    companion object {
        internal var lockActive: Boolean = false
        internal var openedShareDialog = false
        @JvmField
        internal var openedPermissionDialog = false

        private const val EXTRA_TRANSACTION_ADDRESS = "com.radixdlt.android.address"
        private const val EXTRA_TRANSACTION_TOKEN_TYPE = "com.radixdlt.android.token_type"
        private const val EXTRA_URI = "com.radixdlt.android.address"

        fun newIntent(ctx: Context) {
            ctx.startActivity<PaymentActivity>()
        }

        fun newIntent(ctx: Context, uri: Uri) {
            ctx.startActivity<PaymentActivity>(
                EXTRA_URI to uri)
        }
    }
}
