package com.radixdlt.android.apps.wallet.ui.send.payment.assetselection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.searchview.Search
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.databinding.FragmentPaymentAssetSelectionBinding
import com.radixdlt.android.apps.wallet.ui.send.PaymentActivity
import com.radixdlt.android.apps.wallet.ui.send.PaymentViewModel
import com.radixdlt.android.apps.wallet.ui.main.assets.AssetPayment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_payment_asset_selection.*
import kotlinx.android.synthetic.main.tool_bar_search.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.px2dip
import java.util.Locale
import javax.inject.Inject

class PaymentAssetSelectionFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var paymentAssetSelectionViewModel: PaymentAssetSelectionViewModel

    private val paymentViewModel: PaymentViewModel by activityViewModels()

    private lateinit var paymentSelectionAdapter: PaymentSelectionAdapter

    private lateinit var ctx: Context

    private var assetSearched: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = initialiseDataBinding(inflater, container)
        initialiseToolBar(view)

        return view
    }

    private fun initialiseToolBar(view: View) {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        actionBar?.title = getString(R.string.payment_asset_selection_fragment_title)
        actionBar?.elevation = view.context.px2dip(0)
    }

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentPaymentAssetSelectionBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_payment_asset_selection,
                container,
                false
            )

        paymentAssetSelectionViewModel = ViewModelProviders
            .of(this, viewModelFactory)[PaymentAssetSelectionViewModel::class.java]

        binding.viewmodel = paymentAssetSelectionViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        (activity as PaymentActivity).setToolbarVisible()
        initialiseRecyclerView()
        initialiseSearchView()
        initialiseViewModels()
    }

    private fun initialiseRecyclerView() {
        assetSelectionRecyclerView.layoutManager = LinearLayoutManager(activity)
        paymentSelectionAdapter = PaymentSelectionAdapter(itemClick)
        assetSelectionRecyclerView.adapter = paymentSelectionAdapter
    }

    private val itemClick = fun(rri: String) {
        paymentViewModel.selectedAsset = rri
        lifecycleScope.launch {
            delay(500)
            findNavController().navigateUp()
        }
    }

    private fun initialiseSearchView() {
        searchView.setLogoIcon(R.drawable.ic_search_24)
        searchView.setOnLogoClickListener {
            searchView.findViewById<EditText>(R.id.search_searchEditText).requestFocus()
        }
        searchView.setOnQueryTextListener(object : Search.OnQueryTextListener {
            override fun onQueryTextSubmit(query: CharSequence?): Boolean {
                paymentSelectionAdapter.filter.filter(query.toString().toLowerCase(Locale.ROOT))
                assetSearched = searchView?.text.toString()
                return false
            }

            override fun onQueryTextChange(newText: CharSequence?) {
                paymentSelectionAdapter.filter.filter(newText.toString().toLowerCase(Locale.ROOT))
                assetSearched = searchView?.text.toString()
            }
        })
    }

    private fun initialiseViewModels() {
        paymentAssetSelectionViewModel = ViewModelProviders
            .of(this, viewModelFactory)[PaymentAssetSelectionViewModel::class.java]
            .apply {
                assetsOwned.observe(viewLifecycleOwner, Observer(::showAssets))
            }
        paymentAssetSelectionViewModel.selectedAsset = paymentViewModel.selectedAsset
        paymentAssetSelectionViewModel.retrieveAssets()
    }

    private fun showAssets(assets: List<AssetPayment>) {
        paymentSelectionAdapter.originalList(assets)
        paymentSelectionAdapter.replace(assets)
    }
}
