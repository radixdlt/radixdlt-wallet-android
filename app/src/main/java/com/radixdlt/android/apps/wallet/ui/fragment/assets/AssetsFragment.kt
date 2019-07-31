package com.radixdlt.android.apps.wallet.ui.fragment.assets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.searchview.Search
import com.radixdlt.android.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.tool_bar_search.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class AssetsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var assetsViewModel: AssetsViewModel

    private lateinit var assetsAdapter: AssetsAdapter

    private var refreshing = false

    private var assetSearched: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_assets, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseRecyclerView()
        initialiseSwipeRefreshLayout()
        initialiseViewModels()
        initialiseSearchView()
        initialiseLoadingState()
    }

    private fun initialiseSearchView() {
        searchView.setLogoIcon(R.drawable.ic_search_24) // replace with search
        searchView.setOnQueryTextListener(object : Search.OnQueryTextListener {
            override fun onQueryTextSubmit(query: CharSequence?): Boolean {
                assetsAdapter.filter.filter(query.toString().toLowerCase(Locale.ROOT))
                return false
            }

            override fun onQueryTextChange(newText: CharSequence?) {
                assetsAdapter.filter.filter(newText.toString().toLowerCase(Locale.ROOT))
            }
        })

        checkAssetWasSearchedAndFilter()
    }

    private fun checkAssetWasSearchedAndFilter() {
        if (!assetSearched.isNullOrEmpty()) {
            lifecycleScope.launch {
                setLayoutResourcesWithTransactions()
                delay(30)
                assetsAdapter.filter.filter(assetSearched)
            }
        }
    }

    private fun initialiseViewModels() {
        assetsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AssetsViewModel::class.java)

        assetsViewModel.assetsState.observe(this, Observer(::assetsStateChanged))
    }

    private fun assetsStateChanged(state: AssetsState) {
        when (state) {
            is AssetsState.Loading -> { swipe_refresh_layout.isRefreshing = true }
            is AssetsState.ShowAssets -> showOwnedAssets(state.assets)
            is AssetsState.Error -> {}
        }
    }

    private fun showOwnedAssets(tokenTypes: List<Asset>) {
        assetsAdapter.originalList(tokenTypes)
        assetsAdapter.replace(tokenTypes)
        setLayoutResources()
    }

    private fun initialiseRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        assetsAdapter = AssetsAdapter(itemClick)
        recyclerView.adapter = assetsAdapter
    }

    private fun initialiseLoadingState() {
        if (assetsAdapter.itemCount == 0 && assetSearched.isNullOrBlank()) {
            swipe_refresh_layout.isRefreshing = true
        }

        setLayoutResources()
    }

    private fun initialiseSwipeRefreshLayout() {
        swipe_refresh_layout.setColorSchemeResources(
            R.color.colorPrimary, R.color.colorAccent, R.color.colorAccentSecondary
        )
        swipe_refresh_layout.setOnRefreshListener(::refreshTransactions)
    }

    private fun refreshTransactions() {
        swipe_refresh_layout.isRefreshing = false
        refreshing = true
    }

    private fun setLayoutResources() {
        if (assetsAdapter.itemCount == 0 && assetSearched.isNullOrBlank()) {
            setLayoutResourcesWithEmptyTransactions()
        } else {
            setLayoutResourcesWithTransactions()
        }
    }

    private fun setLayoutResourcesWithEmptyTransactions() {
        walletBackGroundFrameLayout.visibility = View.VISIBLE
    }

    private fun setLayoutResourcesWithTransactions() {
        walletBackGroundFrameLayout.visibility = View.GONE
        swipe_refresh_layout.isRefreshing = false
    }

    private val itemClick = fun(rri: String, name: String, balance: String) {
        assetSearched = searchView?.text.toString()
        val action = AssetsFragmentDirections
            .actionNavigationAssetsToNavigationAssetTransactions(rri, name, balance)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
