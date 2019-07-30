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
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionDetails
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionEntity
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
import javax.inject.Inject

class TransactionDetailsActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var transactionsViewModel: TransactionDetailsViewModel

    companion object {
        private const val EXTRA_TRANSACTION_DETAILS = "com.radixdlt.android.transaction_details"

        fun newIntent(ctx: Context, transactionEntityDetails: TransactionEntity) {
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
            intent.getParcelableExtra<TransactionEntity>(EXTRA_TRANSACTION_DETAILS)

        initialiseHeaderAnimation()
        initialiseToolbar()
        initialiseViewModel(transactionDetailsExtra)
        initialiseClickListeners(transactionDetailsExtra)
        bindIndividualTransactionDetailsData(transactionDetailsExtra)
    }

    private fun initialiseClickListeners(transactionDetailsExtra: TransactionEntity) {
        transactionSendTokens.setOnClickListener {
            SendRadixActivity.newIntent(
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

    private fun bindIndividualTransactionDetailsData(transactionDetailsExtra: TransactionEntity) {
        transactionAddress.text =
            setAddressWithColors(this, transactionDetailsExtra.address)
        transactionAmount.text = formatCharactersForAmount(
            transactionDetailsExtra.formattedAmount.split(".")[0],
            transactionDetailsExtra.formattedAmount.split(".")[1]
        )

        setResources()
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

        transactionDate.text = formatDateTime(transactionDetailsExtra.dateUnix)
    }

    private fun setTokenType(transactionEntity: TransactionEntity) {
//        if (transactionEntity.tokenClassISO != GENESIS_XRD) {
            testTokensTextView.text = transactionEntity.rri.split("/")[2]
            testTokensTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
//        }
    }

    private fun initialiseViewModel(transactionDetailsExtra: TransactionEntity) {
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
        collapsing_toolbar.title = getString(R.string.transaction_details_activity_title)
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

    private fun setResources() {
        if (transactionAmount.text.toString()[0] == "+".single()) {
            addressTextView.text = getString(R.string.transaction_details_activity_received)
            titleTransactionTextView.text = getString(R.string.transaction_details_activity_from)
            transactionAmount.setTextColor(ContextCompat.getColor(this, R.color.green))
            circleImageView.setImageResource(R.drawable.receive_image_item_wallet)
        } else {
            addressTextView.text = getString(R.string.transaction_details_activity_sent)
            titleTransactionTextView.text = getString(R.string.transaction_details_activity_to)
            circleImageView.setImageResource(R.drawable.send_image_item_wallet)
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
