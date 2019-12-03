package com.radixdlt.android.apps.wallet.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgs
import androidx.navigation.Navigation
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.ui.fragment.transactions.AssetTransactionsFragment
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.Pref.set
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("Registered")
open class BaseActivityNew : AppCompatActivity() {

    private val navController by lazy {
        Navigation.findNavController(this, R.id.my_nav_host_fragment)
    }

    private val graph by lazy {
        navController.navInflater.inflate(R.navigation.nav_graph)
    }

    var args: NavArgs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (RadixWalletApplication.wasInBackground && !lockActive) {
            lockActive = true
            if (defaultPrefs()[Pref.AUTHENTICATE_ON_LAUNCH, false]) {
                toolbar.visibility = View.GONE
                navigation.visibility = View.GONE
                if (defaultPrefs()[Pref.USE_BIOMETRICS, false]) {
                    setGraph(R.id.navigation_launch_biometrics)
                } else {
                    setGraph(R.id.navigation_launch_pin)
                }
            }
        }

        (this.application as RadixWalletApplication).stopActivityTransitionTimer()
    }

    private fun setGraph(@IdRes idRes: Int) {
        graph.startDestination = idRes
        navController.graph = graph
    }

    override fun onPause() {
        super.onPause()

        if (!defaultPrefs()[Pref.AUTHENTICATE_ON_LAUNCH, false] ||
            !defaultPrefs()[Pref.MNEMONIC_SEED, false] || lockActive ||
            navController.currentDestination?.id == R.id.navigation_launch_biometrics ||
            navController.currentDestination?.id == R.id.navigation_launch_pin) {

            return
        }

        defaultPrefs()[Pref.FRAGMENT_ID] = navController.currentDestination?.id
        defaultPrefs()[Pref.LOCK_ACTIVE] = true
        if (navController.currentDestination?.id == R.id.navigation_asset_transactions) {
            val navHost = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment)
            navHost?.let { navFragment ->
                navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
                    this.args = (fragment as AssetTransactionsFragment).args
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
    }
}
