package com.radixdlt.android.apps.wallet.ui.fragment.assets

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.radixdlt.android.apps.wallet.ui.activity.SendRadixActivity
import com.radixdlt.android.apps.wallet.ui.dialog.ReceiveRadixDialog
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.toast
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_assets.*
import kotlinx.android.synthetic.main.tool_bar_search.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
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
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        payButtonClickListener()
        receiveButtonClickListener()
    }

    private fun initialiseSearchView() {
        searchView.setLogoIcon(R.drawable.ic_search_24)
        searchView.setOnLogoClickListener {
            searchView.findViewById<EditText>(R.id.search_searchEditText).requestFocus()
        }
        searchView.setOnQueryTextListener(object : Search.OnQueryTextListener {
            override fun onQueryTextSubmit(query: CharSequence?): Boolean {
                assetsAdapter.filter.filter(query.toString().toLowerCase(Locale.ROOT))
                assetSearched = searchView?.text.toString()
                return false
            }

            override fun onQueryTextChange(newText: CharSequence?) {
                assetsAdapter.filter.filter(newText.toString().toLowerCase(Locale.ROOT))
                assetSearched = searchView?.text.toString()
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

    private fun payButtonClickListener() {
        payButton.setOnClickListener {
            SendRadixActivity.newIntent(activity!!)
        }
    }

    private fun receiveButtonClickListener() {
        receiveButton.setOnClickListener {
            val receiveRadixDialog = ReceiveRadixDialog.newInstance()
            receiveRadixDialog.setTargetFragment(
                this@AssetsFragment,
                REQUEST_CODE_RECEIVE_RADIX
            )
            receiveRadixDialog.show(fragmentManager!!, null)
        }
    }

    private fun assetsStateChanged(state: AssetsState) {
        when (state) {
            is AssetsState.Loading -> {
                swipe_refresh_layout.isRefreshing = true
            }
            is AssetsState.ShowAssets -> showOwnedAssets(state.assets)
            is AssetsState.Error -> {
                toast("There was an error!")
            }
        }
    }

    private fun showOwnedAssets(tokenTypes: List<Asset>) {
        checkAssetWasBeingSearched()
        assetsAdapter.originalList(tokenTypes)
        assetsAdapter.replace(tokenTypes)
        setLayoutResources()
    }

    private fun checkAssetWasBeingSearched() {
        if (!assetSearched.isNullOrEmpty()) {
            val assetSearched = searchView?.text.toString()
            searchView.text?.clear()
            this.assetSearched = assetSearched
            searchView.setText(assetSearched)
            searchView.findViewById<EditText>(R.id.search_searchEditText)
                .setSelection(assetSearched.length)
        }
        checkAssetWasSearchedAndFilter()
    }

    private fun initialiseRecyclerView() {
        assetsRecyclerView.layoutManager = LinearLayoutManager(activity)
        assetsAdapter = AssetsAdapter(itemClick)
        assetsRecyclerView.adapter = assetsAdapter
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
        refreshing = false
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
        swipe_refresh_layout.isEnabled = false
    }

    private val itemClick = fun(rri: String, name: String, balance: String) {
        val action = AssetsFragmentDirections
            .actionNavigationAssetsToNavigationAssetTransactions(rri, name, balance)
        findNavController().navigate(action)
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

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    companion object {
        private const val REQUEST_CODE_RECEIVE_RADIX = 0
    }
}
