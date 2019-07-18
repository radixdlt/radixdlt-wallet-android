package com.radixdlt.android.apps.wallet.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.isRadixAddress
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.tool_bar.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import timber.log.Timber

class MainActivity : BaseActivity() {

    private lateinit var options: NavOptions.Builder
    private var uri: Uri? = null


    private val compositeDisposable = CompositeDisposable()

    companion object {
        private const val RC_BARCODE_CAPTURE = 9000

        private const val EXTRA_URI = "com.radixdlt.android.uri"

        fun newIntent(ctx: Context) {
            ctx.startActivity<MainActivity>()
        }

        fun newIntent(ctx: Context, uri: Uri) {
            ctx.startActivity<MainActivity>(EXTRA_URI to uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        initialiseNavigation()
        detectIfConnectedToRadixNetwork()

        uri = intent.getParcelableExtra(EXTRA_URI)

        uri?.let {
            if (it.path!!.contains("payment", true)) {
                SendRadixActivity.newIntent(this, it)
            } else {
                navigation.selectedItemId = R.id.navigation_contacts
                ConversationActivity.newIntent(this, it)
            }
        }
    }

    /**
     * Quick and dirty implementation which should be moved to ViewModel but
     * this wallet will become deprecated so good enough until v2 with new design
     * is launched.
     * */
    private fun detectIfConnectedToRadixNetwork() {
        Identity.api!!.networkState
            .distinct { it.nodeStates.values.first().status.name }
            .subscribe {
                when (it.nodeStates.values.first().status.name) {
                    "CONNECTING" -> setConnectionText("CONNECTING")
                    "CONNECTED" -> setConnectionText("CONNECTED")
                    "FAILED" -> {
                        showToastOnMainThread("Unable to connect to the Radix network!")
                        setConnectionText("DISCONNECTED")
                    }
                }
                Timber.tag("CONNECTED").d(it.nodeStates.values.first().status.name)
            }.addTo(compositeDisposable)
    }

    private fun showToastOnMainThread(text: String) {
        runOnUiThread {
            toast(text)
        }
    }

    private fun setConnectionText(text: String) {
        runOnUiThread {
            toolbarConnectionTextView.text = text
        }
    }

    /**
     * Currently this is very neat but it seems like clicking on already selected tab
     * replaces the fragment for the same one causing everything to reload including the animation.
     * For now, resorted to doing it the manual way.
     * */
//    private fun setUpViews() {
//        NavigationUI.setupWithNavController(navigation, Navigation.findNavController(this,
//                R.id.my_nav_host_fragment))
//    }

    private fun initialiseNavigation() {
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        options = NavOptions.Builder()
                        .setEnterAnim(R.anim.nav_default_enter_anim)
                        .setExitAnim(R.anim.nav_default_exit_anim)
                        .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                        .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
    }

    private val onNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->

                if (item.itemId == navigation.selectedItemId) {
                    return@OnNavigationItemSelectedListener false
                }

                val navController = Navigation
                        .findNavController(this, R.id.my_nav_host_fragment)

                when (item.itemId) {
                    R.id.navigation_wallet -> {
                        navController.navigate(R.id.navigation_wallet, null,
                                options.setPopUpTo(R.id.navigation_wallet, true).build())
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_contacts -> {
                        navController.navigate(R.id.navigation_contacts, null,
                                options.setPopUpTo(R.id.navigation_wallet, false).build())
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_account -> {
                        navController.navigate(R.id.navigation_account, null,
                                options.setPopUpTo(R.id.navigation_wallet, false).build())
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_more_options -> {
                        navController.navigate(R.id.navigation_more_options, null,
                                options.setPopUpTo(R.id.navigation_wallet, false).build())
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode: Barcode = data.getParcelableExtra(
                        BarcodeCaptureActivity.BARCODE_OBJECT
                    )
                    if (isRadixAddress(barcode.displayValue)) {
                        ConversationActivity.newIntent(
                            this,
                            barcode.displayValue
                        )
                    } else {
                        toast(getString(R.string.invalid_address_toast))
                    }
                    Timber.d("Barcode read: ${barcode.displayValue}")
                } else {
                    Timber.d("No barcode captured, intent data is null")
                }
            } else {
                val failureString = this.getString(R.string.barcode_error) +
                        CommonStatusCodes.getStatusCodeString(resultCode)
                Timber.e(failureString)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigation.menu.findItem(R.id.navigation_wallet).isChecked = true
    }

    override fun onStop() {
        super.onStop()
        openedShareDialog = false
        Timber.d("onStop")
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        compositeDisposable.clear()
        if (QueryPreferences.getPrefPasswordEnabled(this)) {
            QueryPreferences.setPrefAddress(this, "")
        }
        super.onDestroy()
    }
}
