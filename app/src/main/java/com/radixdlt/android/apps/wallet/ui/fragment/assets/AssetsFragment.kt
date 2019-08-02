package com.radixdlt.android.apps.wallet.ui.fragment.assets

import android.app.Activity
import android.content.Context
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
import com.radixdlt.android.apps.wallet.ui.activity.main.MainLoadingState
import com.radixdlt.android.apps.wallet.ui.activity.main.MainViewModel
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

    private lateinit var mainViewModel: MainViewModel
    private lateinit var assetsViewModel: AssetsViewModel

    private lateinit var assetsAdapter: AssetsAdapter

    private lateinit var ctx: Context

    private var assetSearched: String? = null

    private var loadingAssets = false

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
        ctx = view.context
        initialiseRecyclerView()
        initialiseSwipeRefreshLayout()
        initialiseSearchView()
        initialiseViewModels()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        setPayButtonOnClickListener()
        setReceiveButtonOnClickListener()
    }

    private fun initialiseViewModels() {
        mainViewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]
        assetsViewModel = ViewModelProviders.of(this, viewModelFactory)[AssetsViewModel::class.java]
        mainViewModel.mainLoadingState.observe(viewLifecycleOwner, Observer(::loadingState))
        assetsViewModel.assetsState.observe(viewLifecycleOwner, Observer(::assetsStateChanged))
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
                setLayoutResourcesWithAssets()
                delay(30)
                assetsAdapter.filter.filter(assetSearched)
            }
        }
    }

    private fun setPayButtonOnClickListener() {
        payButton.setOnClickListener {
            SendRadixActivity.newIntent(activity!!)
        }
    }

    private fun setReceiveButtonOnClickListener() {
        receiveButton.setOnClickListener {
            val receiveRadixDialog = ReceiveRadixDialog.newInstance()
            receiveRadixDialog.setTargetFragment(
                this@AssetsFragment,
                REQUEST_CODE_RECEIVE_RADIX
            )
            receiveRadixDialog.show(fragmentManager!!, null)
        }
    }

    private fun loadingState(loading: MainLoadingState) {
        when (loading) {
            MainLoadingState.LOADING -> setLayoutResourcesWithLoadingIndicator()
            MainLoadingState.FINISHED -> setLayoutResourcesAfterDelay()
        }
    }

    private fun assetsStateChanged(state: AssetsState) {
        when (state) {
            is AssetsState.Loading -> setLoadingAssets()
            is AssetsState.ShowAssets -> showOwnedAssets(state.assets)
            is AssetsState.Error -> toast("There was an error!")
        }
    }

    private fun setLoadingAssets() {
        loadingAssets = true
        setLayoutResources()
    }

    private fun showOwnedAssets(tokenTypes: List<Asset>) {
        loadingAssets = false
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

    private fun initialiseSwipeRefreshLayout() {
        swipe_refresh_layout.setColorSchemeResources(
            R.color.colorPrimary, R.color.colorAccent, R.color.colorAccentSecondary
        )
    }

    private fun setLayoutResources() {
        if (loadingAssets) {
            setLayoutResourcesWithLoadingIndicator()
        } else if (assetsAdapter.itemCount == 0 && assetSearched.isNullOrBlank()) {
            setLayoutResourcesWithEmptyAssets()
        } else {
            setLayoutResourcesWithAssets()
        }
    }

    private fun setLayoutResourcesAfterDelay() {
        lifecycleScope.launch {
            delay(500)
            view?.let { setLayoutResources() }
        }
    }

    private fun setLayoutResourcesWithLoadingIndicator() {
        swipe_refresh_layout.isRefreshing = true
        assetsMessageTextView.text = getString(R.string.assets_fragment_loading_assets_textview)
    }

    private fun setLayoutResourcesWithEmptyAssets() {
        swipe_refresh_layout.isRefreshing = false
        assetsImageView.visibility = View.VISIBLE
        assetsMessageTextView.text = getString(R.string.assets_fragment_no_owned_assets_textview)
        assetsMessageTextView.visibility = View.VISIBLE
    }

    private fun setLayoutResourcesWithAssets() {
        assetsImageView.visibility = View.GONE
        assetsMessageTextView.visibility = View.GONE
        swipe_refresh_layout.isRefreshing = false
        swipe_refresh_layout.isEnabled = false
    }

    private val itemClick = fun(rri: String, name: String, balance: String) {
        val action = AssetsFragmentDirections
            .actionNavigationAssetsToNavigationAssetTransactions(rri, name, balance)
        findNavController().navigate(action)
    }

    private fun copyAddressToClipBoard(address: String) {
        copyToClipboard(ctx, address)

        val addressToShow = if (address == QueryPreferences.getPrefAddress(ctx)) {
            getString(R.string.common_address_text)
        } else {
            address
        }

        toast("$addressToShow ${getString(R.string.toast_copied_clipboard)}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == REQUEST_CODE_RECEIVE_RADIX) {
            copyAddressToClipBoard(QueryPreferences.getPrefAddress(ctx))
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
