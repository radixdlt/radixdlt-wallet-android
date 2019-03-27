package com.radixdlt.android.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.radixdlt.android.R

class NewPaperKeyWalletActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_paper_key_wallet)
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.nav_host_fragment_paper_key).navigateUp()

    override fun onBackPressed() {
        val mnemonicConfirmation = findNavController(R.id.nav_host_fragment_paper_key).currentDestination?.id
        if (mnemonicConfirmation == R.id.navigation_paper_key_mnemonic_confirmation) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
