package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
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
import kotlin.math.abs

class AssetTransactionsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var assetTransactionsViewModel: AssetTransactionsViewModel
    private lateinit var assetTransactionsAdapter: AssetTransactionsAdapter

    private val args: AssetTransactionsFragmentArgs by navArgs()
    private val rri: String by lazy { args.rri }
    private val name: String by lazy { args.name }
    private val balance: String by lazy { args.balance }

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
        initialiseCollapsingToolbar()
        initialiseRecyclerView()
        initialiseViewModels(rri)
        setOnClickListeners()
    }

    private fun initialiseCollapsingToolbar() {
        setAppBarAlwaysDraggable()
        showBalanceAndIso()
        // Selected used to trigger state for elevation of view and other state checks
        pullDownDropFrameLayout.isSelected = true
        assetTransactionsDetailsCollapsingToolbar.isSelected = true

        assetTransactionsAppBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

                if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0 &&
                    !pullDownDropFrameLayout.isSelected
                ) {
                    // Collapsed
                    pullDownDropFrameLayout.isSelected = true
                    assetTransactionsDetailsCollapsingToolbar.isSelected = true
                    startAnimation(pullDownDropArrowImageView, false)
                } else if (verticalOffset == 0 && pullDownDropFrameLayout.isSelected) {
                    // Expanded
                    pullDownDropFrameLayout.isSelected = false
                    assetTransactionsDetailsCollapsingToolbar.isSelected = false
                    startAnimation(pullDownDropArrowImageView, true)
                }
            })

        pullDownDropFrameLayout.setOnClickListener {
            val isSelected = assetTransactionsDetailsCollapsingToolbar.isSelected
            assetTransactionsAppBarLayout.setExpanded(isSelected, true)
        }
    }

    private fun startAnimation(imageView: ImageView, expanded: Boolean) {
        val drawable = imageView.drawable
        if (drawable != null && drawable is Animatable2) {
            (drawable as Animatable2).start()
            (drawable as Animatable2).registerAnimationCallback(object :
                Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    if (expanded) {
                        pullDownDropArrowImageView.setImageResource(
                            R.drawable.animated_drop_arrow_up
                        )
                    } else {
                        pullDownDropArrowImageView.setImageResource(
                            R.drawable.animated_drop_arrow_down
                        )
                    }
                }
            })
        }
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

    private fun setAppBarAlwaysDraggable() {
        val params = assetTransactionsAppBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return true
            }
        })
        params.behavior = behavior
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
        val totalSupply = transaction.tokenTotalSupply
            ?.setScale(2, RoundingMode.HALF_UP)?.toPlainString()
            ?: ""
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
