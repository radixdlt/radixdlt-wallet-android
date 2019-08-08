package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        assetTransactionsAppBarLayout.setExpanded(false)
        showBalanceAndIso()
        initialiseRecyclerView()
        initialiseViewModels(rri)
        setOnClickListeners()
    }

    private fun showBalanceAndIso() {
        showAssetBalance(balance)
        val rri = RRI.fromString(rri)
        assetSymbolTextView.text = rri.name
        assetTransactionsRRITextView.setText(
            getString(R.string.common_rri_address_string, rri.address, rri.name),
            TextView.BufferType.EDITABLE
        )
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
            fragmentManager?.apply {
                receiveRadixDialog.show(this, null)
            }
        }
    }

    private fun initialiseRecyclerView() {
        assetTransactionsRecyclerView.layoutManager = LinearLayoutManager(activity)
        assetTransactionsAdapter = AssetTransactionsAdapter(click)
        val stickHeaderDecoration = StickyHeaderItemDecoration(assetTransactionsAdapter)
        assetTransactionsRecyclerView.addItemDecoration(stickHeaderDecoration)
        assetTransactionsRecyclerView.adapter = assetTransactionsAdapter
    }

    private fun initialiseViewModels(asset: String) {
        assetTransactionsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AssetTransactionsViewModel::class.java)

        assetTransactionsViewModel.assetTransactionsState.observe(
            viewLifecycleOwner, Observer(::showAssetTransactions)
        )
        assetTransactionsViewModel.assetBalance.observe(
            viewLifecycleOwner, Observer(::showAssetBalance)
        )
        assetTransactionsViewModel.getAllTransactionsAsset(asset)
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
        val transaction = transactions.first()
        val totalSupply = transaction.tokenTotalSupply?.let {
            it.setScale(2, RoundingMode.HALF_UP).toPlainString()
        } ?: ""
        assetTransactionsTotalSupplyTextView.text = totalSupply
        assetTransactionsSupplyTypeTextView.text = transaction.tokenSupplyType
        assetTransactionsDescriptionTextView.text = transaction.tokenDescription
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
}
