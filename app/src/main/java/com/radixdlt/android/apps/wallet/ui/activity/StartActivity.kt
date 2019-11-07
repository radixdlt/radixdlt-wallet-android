package com.radixdlt.android.apps.wallet.ui.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.identity.AndroidRadixIdentity
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.util.KEYSTORE_FILE
import com.radixdlt.android.apps.wallet.util.VAULT_SECRET
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.Vault
import com.radixdlt.client.core.crypto.ECKeyPair
import kotlinx.android.synthetic.main.activity_enter_password.*
import okio.ByteString
import java.io.File

class StartActivity : AppCompatActivity() {

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        uri = intent.data

        val myKeyFile = File(filesDir, KEYSTORE_FILE)
        if (myKeyFile.exists() && QueryPreferences.getPrefPasswordEnabled(this)) {
            subWelcomeTextView.text = getString(R.string.enter_password_activity_sub_welcome_msg)
            createWalletButton.text = getString(R.string.enter_password_activity_unlock_button)
            inputRepeatPasswordTIL.visibility = View.GONE
        } else if (myKeyFile.exists() && !QueryPreferences.getPrefPasswordEnabled(this)) {

            val secret = loadKey() ?: run {
                // delete everything from walelt and restart here?
                return
            }

            val privateKeyDecoded = ByteString.decodeHex(secret).toByteArray()

            Identity.myIdentity = AndroidRadixIdentity(ECKeyPair(privateKeyDecoded))
            openWallet()
        }

        setContentView(R.layout.activity_start)
    }

    private fun loadKey(): String? {
        return Vault.getVault().getString(VAULT_SECRET, null)
    }

    private fun openWallet() {
        when {
            BaseActivity.lockActive && uri == null -> {
                BaseActivity.lockActive = false
                finish()
            }
            uri != null -> {
                BaseActivity.lockActive = false
                MainActivity.newIntent(this, uri!!)
                finishAffinity()
            }
            else -> {
                MainActivity.newIntent(this)
                finishAffinity()
            }
        }
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_start).navigateUp()

    override fun onBackPressed() {
        val createWallet = findNavController(R.id.nav_host_start).currentDestination?.id
        if (createWallet == R.id.navigation_create_wallet) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
