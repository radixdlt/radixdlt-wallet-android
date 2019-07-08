package com.radixdlt.android.apps.wallet.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.util.formatDateTime
import com.radixdlt.client.application.translate.data.receipt.Receipt
import com.radixdlt.client.application.translate.data.receipt.ReceiptItem
import kotlinx.android.synthetic.main.activity_receipt.*
import org.jetbrains.anko.startActivity
import java.math.BigDecimal

class ReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)
        setToolbar()
        setCloseButtonOnClickListener()

        val receiptAsBytes = intent.getByteArrayExtra(EXTRA_RECEIPT)

        val receipt = Receipt.fromSerializedJsonBytes(receiptAsBytes)

        populateViewWith(receipt)
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

    @SuppressLint("SetTextI18n")
    private fun populateViewWith(receipt: Receipt) {
        merchantNameTextView.text = receipt.merchant.name
        val cost = receipt.items.stream()
            .map { i -> i.calculateCost()}
            .reduce(BigDecimal.ZERO, BigDecimal::add)
        totalPriceTextView.text = "$${cost.setScale(2).toPlainString()}"
        dateTextView.text = formatDateTime(receipt.date)

        receipt.items.distinctBy { it.article.name }.forEach { receiptItem ->
            when (receiptItem.article.name) {
                "Fine-ground coffee" -> {
                    addView(receiptItem, receipt.items.count { it.article.name == "Fine-ground coffee"})
                }
                "Brownie" -> {
                    addView(receiptItem, receipt.items.count { it.article.name == "Brownie"})
                }
                "Croissant" -> {
                    addView(receiptItem, receipt.items.count { it.article.name == "Croissant"})
                }
                "Macaron" -> {
                    addView(receiptItem, receipt.items.count { it.article.name == "Macaron"})
                }
            }
        }
    }

    private fun addView(receiptItem: ReceiptItem, count: Int) {
        val linearLayout: LinearLayout = View.inflate(this, R.layout.layout_purchase_item, null) as LinearLayout

        (linearLayout[0] as TextView).text = receiptItem.article.name
        (linearLayout[1] as TextView).text = count.toString()
        (linearLayout[2] as TextView).text = (receiptItem.calculateCost() * count.toBigDecimal()).setScale(2).stripTrailingZeros().toPlainString()

        itemsLinearLayout.addView(linearLayout)
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

        fun newIntent(ctx: Context, receiptBytes: ByteArray) {
            ctx.startActivity<ReceiptActivity>(EXTRA_RECEIPT to receiptBytes)
        }
    }
}
