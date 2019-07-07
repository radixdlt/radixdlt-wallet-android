package com.radixdlt.android.apps.wallet.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.radixdlt.android.R
import kotlinx.android.synthetic.main.activity_receipt.*
import kotlinx.android.synthetic.main.activity_receipt.toolbar
import org.jetbrains.anko.startActivity

class ReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)
        setToolbar()
        setCloseButtonOnClickListener()

        val receipt = intent.getStringExtra(EXTRA_RECEIPT)
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.title = getString(R.string.send_radix_activity_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setCloseButtonOnClickListener() {
        closeButton.setOnClickListener {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_RECEIPT = "com.radixdlt.android.receipt"

        fun newIntent(ctx: Context, receipt: String) {
            ctx.startActivity<ReceiptActivity>(EXTRA_RECEIPT to receipt)
        }
    }
}
