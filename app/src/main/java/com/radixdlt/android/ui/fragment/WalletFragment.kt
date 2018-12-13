package com.radixdlt.android.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.radixdlt.android.R
import com.radixdlt.android.data.model.transaction.TransactionEntity
import com.radixdlt.android.ui.activity.SendRadixActivity
import com.radixdlt.android.ui.activity.TransactionDetailsActivity
import com.radixdlt.android.ui.adapter.WalletAdapter
import com.radixdlt.android.ui.dialog.ReceiveRadixDialog
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.copyToClipboard
import com.radixdlt.android.util.multiClickingPrevention
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.jetbrains.anko.toast
import javax.inject.Inject

class WalletFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var transactionsViewModel: TransactionsViewModel

    private var transactions = mutableListOf<TransactionEntity>()

    private lateinit var walletAdapter: WalletAdapter

    private var loadedFromNetwork = false

    private val tokenTypesList = arrayListOf<String>()

    private var tokenTypeSelectedPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseRecyclerView()
        initialiseLoadingState()
        initialiseSwipeRefreshLayout()
        initialiseViewModels()
        setListeners()
    }

    private fun initialiseViewModels() {
        transactionsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(TransactionsViewModel::class.java)

        transactionsViewModel.tokenTypesLiveData.observe(this, Observer { tokenTypes ->
            tokenTypes?.apply {
                setTokenTypeSpinner(tokenTypes)
            }
        })

        transactionsViewModel.balance.observe(this, Observer { balance ->
            balance?.apply {
                bindBalance(balance)
            }
        })

        transactionsViewModel.transactionList.observe(this,
            Observer { transactions ->
                transactions?.apply {
                    bindTransactions(transactions)
                }
            }
        )
    }

    private fun setTokenTypeSpinner(tokenTypes: List<String>) {
        tokenTypesList.clear()

        when {
            tokenTypes.isEmpty() -> tokenTypesList.add(getString(R.string.wallet_fragment_total_tokens))
            tokenTypes.size == 1 -> tokenTypesList.addAll(tokenTypes)
            else -> {
                tokenTypesList.add(getString(R.string.wallet_fragment_total_tokens))
                tokenTypesList.addAll(tokenTypes)
            }
        }

        val tokenTypesSpinner = ArrayAdapter(
            activity!!, android.R.layout.simple_spinner_dropdown_item, tokenTypesList
        )

        tokenTypeSpinner.adapter = tokenTypesSpinner

        tokenTypeSpinner.setSelection(tokenTypeSelectedPosition)
    }

    private fun setListeners() {
        sendButtonClickListener()
        receiveFromFaucetButtonClickListener()
        receiveButtonClickListener()
        tokenSpinnerListener()
    }

    private fun sendButtonClickListener() {
        sendButton.setOnClickListener {
            SendRadixActivity.newIntent(activity!!)
        }
    }

    private fun receiveButtonClickListener() {
        receiveButton.setOnClickListener {
            val receiveRadixDialog = ReceiveRadixDialog.newInstance()
            receiveRadixDialog.setTargetFragment(
                this@WalletFragment, REQUEST_CODE_RECEIVE_RADIX
            )
            receiveRadixDialog.show(fragmentManager, null)
        }
    }

    private fun receiveFromFaucetButtonClickListener() {
        receiveFromFaucetButton.setOnClickListener {
            if (multiClickingPrevention(2000)) return@setOnClickListener
            activity!!.toast(activity!!.getString(R.string.toast_request_tokens_faucet))
            transactionsViewModel.requestTokensFromFaucet()
        }
    }

    private fun tokenSpinnerListener() {
        tokenTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Not implemented
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                tokenTypeSelectedPosition = position
                transactionsViewModel.balance.retrieveWalletBalance(tokenTypesList[position])
            }
        }
    }

    private fun initialiseRecyclerView() {
        val itemDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(activity!!, R.drawable.divider)!!)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(itemDecorator)
        walletAdapter = WalletAdapter(transactions, click)
        recyclerView.adapter = walletAdapter
    }

    private fun initialiseLoadingState() {
        if (transactions.isEmpty()) {
            swipe_refresh_layout.isRefreshing = true
            recentTransactionsTextView.text = activity!!.getString(
                R.string.wallet_fragment_retrieving_transactions
            )
            walletBackGroundFrameLayout.visibility = View.VISIBLE
        } else {
            recentTransactionsTextView.text = activity!!.getString(
                R.string.wallet_fragment_all_transactions
            )
        }

        setLayoutResources()
    }

    private fun initialiseSwipeRefreshLayout() {
        swipe_refresh_layout.setColorSchemeResources(
            R.color.colorPrimary, R.color.colorAccent, R.color.colorAccentSecondary
        )
        swipe_refresh_layout.setOnRefreshListener(::refreshTransactions)
    }

    private fun bindBalance(balance: String) {
        balanceTextView.text = balance
    }

    private fun bindTransactions(transactionEntities: MutableList<TransactionEntity>) {
        when {
            transactionEntities.isEmpty() -> {
                // Added a loadedFromNetwork since we query the DB first and then instantly query
                // the network for any old transactions which may have not been stored such as
                // when loading an empty wallet
                if (loadedFromNetwork) {
                    swipe_refresh_layout.isRefreshing = false
                    recentTransactionsTextView.text = activity!!.getString(
                        R.string.wallet_fragment_no_transactions
                    )
                    return
                }
                loadedFromNetwork = true
                return
            }
            else -> addTransactions(transactionEntities)
        }
    }

    private fun refreshTransactions() {
        loadedFromNetwork = false
        swipe_refresh_layout.isRefreshing = true
        transactionsViewModel.refresh()
    }

    private fun addTransactions(transactionEntities: List<TransactionEntity>) {
        // If a list is received, for now simply clear it and show the whole list
        // else we received a single transaction
        if (transactionEntities.size > 1) {
            this.transactions.clear()
            this.transactions.addAll(transactionEntities)
            showTransactions()
        } else {
            // There can be an existing individual transaction hence
            // check if it exists before adding and then showing
            this.transactions.find {
                it.dateUnix == transactionEntities.first().dateUnix
            } ?: run {
                this.transactions.add(0, transactionEntities.first())
                showNewTransaction()
            }
        }
    }

    private fun showTransactions() {
        setLayoutResources()
        transactions.reverse()
        walletAdapter.notifyDataSetChanged()
    }

    private fun showNewTransaction() {
        if (walletBackGroundFrameLayout.visibility == View.VISIBLE) {
            setLayoutResourcesWithTransactions()
        }

        walletAdapter.notifyItemInserted(0)
        recyclerView.smoothScrollToPosition(0)
    }

    private fun setLayoutResources() {
        if (transactions.isEmpty() && loadedFromNetwork) {
            setLayoutResourcesWithEmptyTransactions()
        } else if (transactions.isNotEmpty()) {
            setLayoutResourcesWithTransactions()
        }
    }

    private fun setLayoutResourcesWithEmptyTransactions() {
        recentTransactionsTextView.text = activity!!.getString(
            R.string.wallet_fragment_no_transactions
        )
        walletBackGroundFrameLayout.visibility = View.VISIBLE
    }

    private fun setLayoutResourcesWithTransactions() {
        recentTransactionsTextView.text = activity!!.getString(
            R.string.wallet_fragment_all_transactions
        )
        walletBackGroundFrameLayout.visibility = View.GONE
        swipe_refresh_layout.setBackgroundColor(
            ContextCompat.getColor(activity!!, R.color.mainBackground)
        )
        swipe_refresh_layout.isRefreshing = false
    }

    private val click = fun(transactionEntity: TransactionEntity, longClick: Boolean) {
        if (longClick) {
            copyAddressToClipBoard(transactionEntity.address)
        } else {
            navigateToDetails(transactionEntity)
        }
    }

    private fun navigateToDetails(transactionEntity: TransactionEntity) {
        TransactionDetailsActivity.newIntent(activity!!, transactionEntity)
    }

    private fun copyAddressToClipBoard(address: String) {
        copyToClipboard(activity!!, address)

        val addressToShow = if (address == QueryPreferences.getPrefAddress(activity!!)) {
            "Address"
        } else {
            address
        }

        activity?.toast("$addressToShow ${activity!!.getString(R.string.toast_copied_clipboard)}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == WalletFragment.REQUEST_CODE_RECEIVE_RADIX) {
            copyAddressToClipBoard(QueryPreferences.getPrefAddress(activity!!))
        }
    }

    companion object {
        private const val REQUEST_CODE_RECEIVE_RADIX = 0
    }
}
