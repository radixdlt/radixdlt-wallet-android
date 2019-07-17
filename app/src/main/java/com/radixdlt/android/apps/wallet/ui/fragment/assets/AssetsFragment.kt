package com.radixdlt.android.apps.wallet.ui.fragment.assets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.searchview.Search
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.adapter.AssetsAdapter
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.tool_bar_search.*
import org.jetbrains.anko.toast
import timber.log.Timber
import javax.inject.Inject

class AssetsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var assetsViewModel: AssetsViewModel

    private var assets = mutableListOf<Asset>()

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

        searchView.setLogoIcon(android.R.drawable.ic_menu_search) // replace with search
        searchView.setOnQueryTextListener(object : Search.OnQueryTextListener {
            override fun onQueryTextSubmit(query: CharSequence?): Boolean {
                assetsAdapter.filter.filter(query.toString().toLowerCase())
                return false
            }

            override fun onQueryTextChange(newText: CharSequence?) {
                assetsAdapter.filter.filter(newText.toString().toLowerCase())
            }
        })

        Identity.api!!.networkState.subscribe {
            Timber.tag("NETSTATE").d(it.toString())
        }
    }

    private fun initialiseViewModels() {
        assetsViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AssetsViewModel::class.java)

        assetsViewModel.transactionList.observe(this,
            Observer { transactions ->
                transactions?.apply {
                    if (loadedFromNetwork && (isEmpty() || refreshing)) {
                        refreshing = false
                        swipe_refresh_layout.isRefreshing = false
                    }
                    loadedFromNetwork = true
                }
            }
        )

        assetsViewModel.assetsLiveData.observe(this, Observer { tokenTypes ->
            tokenTypes?.apply {
                swipe_refresh_layout.isRefreshing = false
                showOwnedAssets((this as AssetsState.Assets).assets)
            }
        })
    }

    private fun showOwnedAssets(tokenTypes: List<Asset>) {
        assets.clear()
        assets.addAll(tokenTypes)

        setLayoutResources()
        assetsAdapter.notifyDataSetChanged()
    }

    private fun initialiseRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        assetsAdapter = AssetsAdapter(assets, click)
        recyclerView.adapter = assetsAdapter
    }

    private fun initialiseLoadingState() {
        if (assets.isEmpty()) {
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
        swipe_refresh_layout.isRefreshing = true
        refreshing = true
        assetsViewModel.refresh()

//        Identity.api!!
//            .pullOnce(RadixAddress.from("JGPU2M7Wss6C3TjAtt3BaLESHGoWWCb5sKw5eQdsfHVk3CuPjpf"))
//            .subscribeOn(Schedulers.io())
//            .subscribe {
//                tokenDef()
//            }
    }

    private fun tokenDef() {
//        val rri = RRI.of(RadixAddress.from(assets.first().address), assets.first().iso)
//        Identity.api!!.observeTokenDef(rri)
//            .firstOrError() // Converting Observable to Single
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe ({
//                Timber.d(it.toString())
//                activity?.toast(it.description)
//                swipe_refresh_layout.isRefreshing = false
//            }, {})
    }

    private fun setLayoutResources() {
        if (assets.isEmpty() && loadedFromNetwork) {
            setLayoutResourcesWithEmptyTransactions()
        } else if (assets.isNotEmpty()) {
            setLayoutResourcesWithTransactions()
        }
    }

    private fun setLayoutResourcesWithEmptyTransactions() {
        walletBackGroundFrameLayout.visibility = View.VISIBLE
    }

    private fun setLayoutResourcesWithTransactions() {
        walletBackGroundFrameLayout.visibility = View.GONE
        swipe_refresh_layout.setBackgroundColor(
            ContextCompat.getColor(activity!!, R.color.mainBackground)
        )
        swipe_refresh_layout.isRefreshing = false
    }

    private val click = fun(item: String, longClick: Boolean) {
        if (longClick) { }
    }
}
