package com.radixdlt.android.apps.wallet.ui.receive

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.ui.main.LaunchAuthenticationAction
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.Pref.set
import kotlinx.android.synthetic.main.activity_receive_payment.*

class ReceiveActivity : AppCompatActivity() {

    private val receivePaymentViewModel by viewModels<ReceiveViewModel>()

    private val navController by lazy {
        Navigation.findNavController(this, R.id.nav_host_fragment_receive_payment)
    }

    private val graph by lazy {
        navController.navInflater.inflate(R.navigation.nav_graph_receive_payment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_payment)
        setSupportActionBar(toolbar as Toolbar)
        initialiseViewModel()
    }

    private fun initialiseViewModel() {
        receivePaymentViewModel.launchAuthenticationAction.observe(this, Observer(::launchAuthenticationAction))
    }

    private fun setGraph(@IdRes idRes: Int) {
        graph.startDestination = idRes
        navController.graph = graph
    }

    private fun unlock() {
        // Will need below when adding a new fragment to generate receipts
//        val fragmentId = if (defaultPrefs()[Pref.LOCK_ACTIVE, false]) {
//            defaultPrefs()[Pref.FRAGMENT_ID, R.id.navigation_receive_payment]
//        } else {
//            R.id.navigation_receive_payment
//        }

        setGraph(R.id.navigation_receive_payment)
        lockActive = false
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

    override fun onSupportNavigateUp() =
        findNavController(R.id.nav_host_fragment_receive_payment).navigateUp()

    override fun onBackPressed() {
        val paymentInputFragment =
            findNavController(R.id.nav_host_fragment_receive_payment).currentDestination?.id
        if (paymentInputFragment == R.id.navigation_receive_payment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        if (RadixWalletApplication.wasInBackground && !openedShareDialog && !lockActive) {
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
    }
}
