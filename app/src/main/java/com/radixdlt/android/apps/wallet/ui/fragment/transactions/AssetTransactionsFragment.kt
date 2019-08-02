package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.ui.activity.SendRadixActivity
import com.radixdlt.android.apps.wallet.ui.activity.TransactionDetailsActivity
import com.radixdlt.android.apps.wallet.ui.adapter.StickyHeaderItemDecoration
import com.radixdlt.android.apps.wallet.ui.dialog.ReceiveRadixDialog
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.toast
import com.radixdlt.client.core.atoms.particles.RRI
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_asset_transactions.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class AssetTransactionsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var assetTransactionsViewModel: AssetTransactionsViewModel

    private val args: AssetTransactionsFragmentArgs by navArgs()
    private val rri: String by lazy { args.rri }
    private val name: String by lazy { args.name }
    private val balance: String by lazy { args.balance }

    private lateinit var assetTransactionsAdapter: AssetTransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.title = name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_asset_transactions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showAssetBalance(balance)
        assetSymbolTextView.text = RRI.fromString(rri).name
        initialiseRecyclerView()
        initialiseViewModels(rri)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        payButtonClickListener()
        receiveButtonClickListener()
    }

    private fun payButtonClickListener() {
        payButton.setOnClickListener {
            SendRadixActivity.newIntent(activity!!)
        }
    }

    private fun receiveButtonClickListener() {
        receiveButton.setOnClickListener {
            val receiveRadixDialog = ReceiveRadixDialog.newInstance()
            receiveRadixDialog.setTargetFragment(
                this@AssetTransactionsFragment,
                REQUEST_CODE_RECEIVE_RADIX
            )
            receiveRadixDialog.show(fragmentManager!!, null)
        }
    }

    private fun initialiseRecyclerView() {
        assetTransactionsRecyclerView.layoutManager = LinearLayoutManager(activity)
        assetTransactionsAdapter = AssetTransactionsAdapter(itemClick = click)
        val stickHeaderDecoration = StickyHeaderItemDecoration(assetTransactionsAdapter)
        assetTransactionsRecyclerView.addItemDecoration(stickHeaderDecoration)
        assetTransactionsRecyclerView.adapter = assetTransactionsAdapter
    }

    private fun initialiseViewModels(asset: String) {
        assetTransactionsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AssetTransactionsViewModel::class.java)

        assetTransactionsViewModel.getAllTransactionsAsset(asset)
        assetTransactionsViewModel.assetTransactionsState.observe(
            this,
            Observer(::showAssetTransactions)
        )
        assetTransactionsViewModel.assetBalance.observe(this, Observer(::showAssetBalance))
    }

    private fun showAssetTransactions(assetTransactionsState: AssetTransactionsState) {
        when (assetTransactionsState) {
            is AssetTransactionsState.ShowAssetTransactions -> showAssetTransactions(
                assetTransactionsState.assets
            )
        }
    }

    private fun showAssetBalance(balance: String) {
        val total = BigDecimal(balance)
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
        assetBalanceTextView.text = total
    }

    private fun showAssetTransactions(transactions: List<TransactionEntity2>) {
        assetTransactionsAdapter.replace(transactions)
    }

    private val click = fun(transactionEntity2: TransactionEntity2, longClick: Boolean) {
        if (!longClick) {
            navigateToDetails(transactionEntity2)
        } else {
            toast(transactionEntity2.address)
        }
    }

    private fun navigateToDetails(transactionEntity: TransactionEntity2) {
        TransactionDetailsActivity.newIntent(activity!!, transactionEntity)
    }

    private fun copyAddressToClipBoard(address: String) {
        copyToClipboard(activity!!, address)

        val addressToShow = if (address == QueryPreferences.getPrefAddress(activity!!)) {
            "Address"
        } else {
            address
        }

        toast("$addressToShow ${activity!!.getString(R.string.toast_copied_clipboard)}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == REQUEST_CODE_RECEIVE_RADIX) {
            copyAddressToClipBoard(QueryPreferences.getPrefAddress(activity!!))
        }
    }

    companion object {
        private const val REQUEST_CODE_RECEIVE_RADIX = 0
    }
}
