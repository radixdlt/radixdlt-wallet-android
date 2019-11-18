package com.radixdlt.android.apps.wallet.ui.fragment.assets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lapism.searchview.Search
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.data.model.AssetEntity
import com.radixdlt.android.apps.wallet.databinding.FragmentAssetsBinding
import com.radixdlt.android.apps.wallet.ui.activity.PaymentActivity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainLoadingState
import com.radixdlt.android.apps.wallet.ui.activity.main.MainViewModel
import com.radixdlt.android.apps.wallet.ui.dialog.ReceiveRadixDialog
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.toast
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_assets.*
import kotlinx.android.synthetic.main.tool_bar_search.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.dip
import timber.log.Timber
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
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentAssetsBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_assets, container, false)
        initialiseViewModels()
        binding.viewmodel = assetsViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        initialiseRecyclerView()
        initialiseSwipeRefreshLayout()
        initialiseSearchView()
        setOnClickListeners()
        checkReceiveTutorialShown()
    }

    private fun dismissWarningSign() {
        activity?.apply {
            assetsWarningSign.animate().translationYBy(dip(-50).toFloat())
                .setDuration(150)
                .withEndAction {
                    mainViewModel.showBackUpWalletNotification(false)
                    assetsWarningSign.visibility = View.GONE
                }
                .start()
        }
    }

    private fun navigateToBackUpWallet() {
        val action = AssetsFragmentDirections
            .actionNavigationAssetsToNavigationBackupWallet()
        findNavController().navigate(action)
    }

    private fun checkReceiveTutorialShown() {
        if (ctx.defaultPrefs()[Pref.TUTORIAL_RECEIVE, false]) return
        // Android requires that extra tick to position items
        // in dialog layout so we do the workaround below
        lifecycleScope.launch {
            delay(5)
            findNavController().navigate(R.id.navigation_tutorial_receive)
        }
    }

    private fun setOnClickListeners() {
        setPayButtonOnClickListener()
        setReceiveButtonOnClickListener()
    }

    private fun initialiseViewModels() {
        activity?.let {
            mainViewModel = ViewModelProviders.of(it, viewModelFactory)[MainViewModel::class.java]
                .apply {
                    mainLoadingState.observe(viewLifecycleOwner, Observer(::loadingState))
                    setBottomNavigationCheckedItem(R.id.menu_bottom_assets)
                    showBackUpWalletNotification.observe(viewLifecycleOwner, Observer { show ->
                        if (show) {
                            assetsWarningSign.visibility = View.VISIBLE
                        } else {
                            assetsWarningSign.visibility = View.GONE
                        }
                })
            }
        }

        assetsViewModel = ViewModelProviders.of(this, viewModelFactory)[AssetsViewModel::class.java]
        assetsViewModel.assetsAction.observe(viewLifecycleOwner, Observer(::assetsAction))
        assetsViewModel.singleAction.observe(viewLifecycleOwner, Observer(::assetsAction))
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
            activity?.apply {
                if (defaultPrefs()[Pref.WALLET_BACKED_UP, false]) {
                    PaymentActivity.newIntent(this)
                } else {
                    navigateToBackUpWallet()
                }
            }
        }
    }

    private fun setReceiveButtonOnClickListener() {
        receiveButton.setOnClickListener {
            val receiveRadixDialog = ReceiveRadixDialog.newInstance()
            fragmentManager?.apply {
                receiveRadixDialog.show(this, null)
            }
        }
    }

    private fun loadingState(state: MainLoadingState) {
        when (state) {
            MainLoadingState.EXISTING -> {}
            MainLoadingState.LOADING -> setLayoutResourcesWithLoadingIndicator()
            MainLoadingState.FINISHED -> setLayoutResources()
        }
    }

    private fun assetsAction(action: AssetsAction) {
        when (action) {
            is AssetsAction.ShowLoading -> setLoadingAssets()
            is AssetsAction.ShowAssets -> showOwnedAssets(action.assets)
            is AssetsAction.ShowError -> toast(getString(R.string.assets_fragment_error_toast))
            AssetsAction.CloseBackUpMnemonicWarning -> dismissWarningSign()
            AssetsAction.NavigateTo -> navigateToBackUpWallet()
        }
    }

    private fun setLoadingAssets() {
        loadingAssets = true
        setLayoutResources()
    }

    private fun showOwnedAssets(tokenTypes: List<AssetEntity>) {
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
            R.color.colorPrimary, R.color.colorAccent, R.color.radixGreen3
        )
        swipe_refresh_layout.setOnRefreshListener(::refreshTransactions)
    }

    private fun refreshTransactions() {
        swipe_refresh_layout.isRefreshing = false
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

    private fun setLayoutResourcesWithLoadingIndicator() {
        payButton.isEnabled = false
        swipe_refresh_layout.isRefreshing = true
    }

    private fun setLayoutResourcesWithEmptyAssets() {
        payButton.isEnabled = false
        assetsImageView.visibility = View.VISIBLE
        swipe_refresh_layout.isRefreshing = false
        swipe_refresh_layout.isEnabled = false
    }

    private fun setLayoutResourcesWithAssets() {
        payButton.isEnabled = true
        assetsImageView.visibility = View.GONE
        swipe_refresh_layout.isRefreshing = false
        swipe_refresh_layout.isEnabled = false
    }

    private val itemClick = fun(rri: String, name: String, balance: String) {
        val action = AssetsFragmentDirections
            .actionNavigationAssetsToNavigationAssetTransactions(rri, name, balance)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        Timber.tag("observeViewModel").d("ONRESUME")
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
