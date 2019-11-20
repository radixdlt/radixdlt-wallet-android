package com.radixdlt.android.apps.wallet.ui.activity

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import com.radixdlt.android.apps.wallet.R
import kotlinx.android.synthetic.main.activity_receive_payment.*

class ReceivePaymentActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_payment)
        setSupportActionBar(toolbar as Toolbar)
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
}
