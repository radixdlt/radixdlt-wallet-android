package com.radixdlt.android.apps.wallet.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.data.model.TransactionsEntityOM
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionDetails
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.util.doOnLayout
import com.radixdlt.android.apps.wallet.util.formatCharactersForAmount
import com.radixdlt.android.apps.wallet.util.formatDateTime
import com.radixdlt.android.apps.wallet.util.setAddressWithColors
import com.radixdlt.android.apps.wallet.util.setConstraintLayoutMargin
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_transaction_details.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.intentFor
import java.math.RoundingMode
import javax.inject.Inject

class TransactionDetailsActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var transactionsViewModel: TransactionDetailsViewModel

    companion object {
        private const val EXTRA_TRANSACTION_DETAILS =
            "com.radixdlt.android.apps.wallet.ui.activity.transaction_details"

        fun newIntent(ctx: Context, transactionEntityDetails: TransactionsEntityOM) {
            ctx.startActivity(
                ctx.intentFor<TransactionDetailsActivity>(
                    EXTRA_TRANSACTION_DETAILS to transactionEntityDetails
                ),
                ActivityOptionsCompat.makeSceneTransitionAnimation(ctx as MainActivity)
                    .toBundle()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        val transactionDetailsExtra =
            intent.getParcelableExtra<TransactionsEntityOM>(EXTRA_TRANSACTION_DETAILS)
                ?: throw IllegalStateException()

        initialiseHeaderAnimation()
        initialiseToolbar()
        initialiseViewModel(transactionDetailsExtra)
        initialiseClickListeners(transactionDetailsExtra)
        bindIndividualTransactionDetailsData(transactionDetailsExtra)
    }

    private fun initialiseClickListeners(transactionDetailsExtra: TransactionsEntityOM) {
        transactionSendTokens.setOnClickListener {
            PaymentActivity.newIntent(
                this, transactionDetailsExtra.address, transactionDetailsExtra.rri
            )
        }

        openConversationFAB.setOnClickListener {
            ConversationActivity.newIntent(
                this,
                transactionDetailsExtra.address
            )
        }
    }

    private fun bindIndividualTransactionDetailsData(transactionDetailsExtra: TransactionsEntityOM) {
        transactionAddress.text = setAddressWithColors(this, transactionDetailsExtra.address)
        var amount = transactionDetailsExtra.amount
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()

        amount = if (transactionDetailsExtra.sent) "-$amount" else "+$amount"
        transactionAmount.text = formatCharactersForAmount(
            amount.toString().split(".")[0],
            amount.toString().split(".")[1]
        )

        setResources(transactionDetailsExtra)
        setTokenType(transactionDetailsExtra)

        transactionDetailsExtra.message?.let {
            transactionMessage.text = it
        } ?: run {
            messageImageView.visibility = View.GONE
            transactionMessage.visibility = View.GONE
            transaction_details_separator_view4.visibility = View.GONE
            // Add a little bit of top margin
            transactionDate.setConstraintLayoutMargin(0, dip(8), 0, 0)
        }

        transactionDate.text = formatDateTime(transactionDetailsExtra.timestamp)
    }

    private fun setTokenType(transactionEntity: TransactionsEntityOM) {
//        if (transactionEntity.tokenClassISO != GENESIS_XRD) {
        testTokensTextView.text = transactionEntity.rri.split("/")[2]
        testTokensTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
//        }
    }

    private fun initialiseViewModel(transactionDetailsExtra: TransactionsEntityOM) {
        transactionsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(TransactionDetailsViewModel::class.java)

        transactionsViewModel.transactionDetailsAddress(
            transactionDetailsExtra.address,
            transactionDetailsExtra.rri
        )

        transactionsViewModel.transactions.observe(this, Observer {
            bindTransactionDetails(it)
        }
        )
    }

    private fun initialiseToolbar() {
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        assetTransactionsDetailsCollapsingToolbar.title = getString(R.string.transaction_details_activity_title)
    }

    private fun initialiseHeaderAnimation() {
        backdrop.doOnLayout {
            showAppBarCircularRevealAnim()
            return@doOnLayout true
        }
    }

    private fun showAppBarCircularRevealAnim() {
        val cx = backdrop.width / 2
        val cy = backdrop.height / 2
        val radius = backdrop.width.toFloat()
        val anim = ViewAnimationUtils
            .createCircularReveal(appbar, cx, cy, 0f, radius)
        anim.duration = 500
        anim.start()
    }

    private fun setResources(transactionDetailsExtra: TransactionsEntityOM) {
        if (transactionDetailsExtra.sent) {
            addressTextView.text = getString(R.string.transaction_details_activity_sent)
            titleTransactionTextView.text = getString(R.string.transaction_details_activity_to)
            circleImageView.setImageResource(R.drawable.new_send_image_item_wallet)
        } else {
            addressTextView.text = getString(R.string.transaction_details_activity_received)
            titleTransactionTextView.text = getString(R.string.transaction_details_activity_from)
            transactionAmount.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            circleImageView.setImageResource(R.drawable.new_receive_image_item_wallet)
        }
    }

    private fun bindTransactionDetails(transactionDetails: TransactionDetails) {
        // History
        totalTransactions.text = transactionDetails.allTransactions.size.toString()
        totalTransactionsSentAmount.text = transactionDetails.sentTotal
        totalTransactionsSent.text = resources.getQuantityString(
            R.plurals.transaction_details_activity_total_transactions,
            transactionDetails.sentSize, transactionDetails.sentSize
        )
        totalTransactionsReceivedAmount.text = transactionDetails.receivedTotal
        totalTransactionsReceived.text = resources.getQuantityString(
            R.plurals.transaction_details_activity_total_transactions,
            transactionDetails.receivedSize, transactionDetails.receivedSize
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // need to override to show transition!
                finishAfterTransition()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
