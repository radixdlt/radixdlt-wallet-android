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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.searchview.Search
import com.radixdlt.android.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.tool_bar_search.*
import javax.inject.Inject

class AssetsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var assetsViewModel: AssetsViewModel

    private lateinit var assetsAdapter: AssetsAdapter

    private var loadedFromNetwork = false
    private var refreshing = false

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
        initialiseLoadingState()
        initialiseSwipeRefreshLayout()
        initialiseViewModels()
        initialiseSearchView()
    }

    private fun initialiseSearchView() {
        searchView.setLogoIcon(R.drawable.ic_search_24) // replace with search
        searchView.setOnQueryTextListener(object : Search.OnQueryTextListener {
            override fun onQueryTextSubmit(query: CharSequence?): Boolean {
                assetsAdapter.filter.filter(query.toString().toLowerCase())
                return false
            }

            override fun onQueryTextChange(newText: CharSequence?) {
                assetsAdapter.filter.filter(newText.toString().toLowerCase())
            }
        })
    }

    private fun initialiseViewModels() {
        assetsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AssetsViewModel::class.java)

        assetsViewModel.assetsState.observe(this, Observer(::assetsStateChanged))
    }

    private fun assetsStateChanged(state: AssetsState) {
        when (state) {
            is AssetsState.Loading -> {}
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
        assetsAdapter = AssetsAdapter(itemClick = click)
        recyclerView.adapter = assetsAdapter
    }

    private fun initialiseLoadingState() {
        if (assetsAdapter.itemCount == 0) {
            swipe_refresh_layout.isRefreshing = true
            walletBackGroundFrameLayout.visibility = View.VISIBLE
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
        loadedFromNetwork = false
        swipe_refresh_layout.isRefreshing = false
        refreshing = true
//        assetsViewModel.refresh()
    }

    private fun setLayoutResources() {
        if (assetsAdapter.itemCount == 0 && loadedFromNetwork) {
            setLayoutResourcesWithEmptyTransactions()
        } else if (assetsAdapter.itemCount > 0) {
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

    private val click = fun(item: String, longClick: Boolean) {
        if (!longClick) {
            val action = AssetsFragmentDirections.actionNavigationAssetsToNavigationAssetTransactions(item)
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
