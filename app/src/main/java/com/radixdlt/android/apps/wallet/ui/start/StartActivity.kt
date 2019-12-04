package com.radixdlt.android.apps.wallet.ui.start

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.identity.AndroidRadixIdentity
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.main.MainActivity
import com.radixdlt.android.apps.wallet.util.KEYSTORE_FILE
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.VAULT_SECRET
import com.radixdlt.android.apps.wallet.util.Vault
import com.radixdlt.client.core.crypto.ECKeyPair
import okio.ByteString
import java.io.File

class StartActivity : AppCompatActivity() {

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        uri = intent.data

        val myKeyFile = File(filesDir, KEYSTORE_FILE)
        if (myKeyFile.exists() && !defaultPrefs()[Pref.PASSWORD, false]) {

            val secret = loadKey() ?: run {
                // delete everything from walelt and restart here?
                return
            }

            val privateKeyDecoded = ByteString.decodeHex(secret).toByteArray()

            Identity.myIdentity = AndroidRadixIdentity(ECKeyPair(privateKeyDecoded))
            openWallet()
        }
    }

    private fun loadKey(): String? {
        return Vault.getVault().getString(VAULT_SECRET, null)
    }

    private fun openWallet() {
        MainActivity.newIntent(this)
        finishAffinity()
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
