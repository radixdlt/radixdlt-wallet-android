package com.radixdlt.android.apps.wallet.ui.main.learn

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.connectivity.ConnectivityState
import com.radixdlt.android.apps.wallet.databinding.FragmentLearnBinding
import com.radixdlt.android.apps.wallet.ui.main.MainActivity
import com.radixdlt.android.apps.wallet.ui.main.MainViewModel
import com.radixdlt.android.apps.wallet.util.URL_KNOWLEDGE_BASE
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import com.radixdlt.android.apps.wallet.util.showErrorSnackbarAboveNavigationView

class LearnFragment : Fragment() {

    private lateinit var binding: FragmentLearnBinding
    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_learn, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setNavAndBottomNavigationVisible()
        observeMainViewModel()
        lifecycleScope.launchWhenResumed {
            initialiseWebView()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initialiseWebView() {
        binding.learnWebView.loadUrl(URL_KNOWLEDGE_BASE)
        binding.learnWebView.settings.javaScriptEnabled = true
        setupProgressChange()
        setupBackKeyListener()
    }

    private fun observeMainViewModel() {
        mainViewModel.connectivityLiveData.observe(this, Observer(::connectivityChange))
    }

    private fun connectivityChange(connectivityState: ConnectivityState) {
        when (connectivityState) {
            ConnectivityState.Connected -> {
                binding.learnWebView.visibility = View.VISIBLE
                binding.learnImageView.visibility = View.GONE
            }
            ConnectivityState.Disconnected -> informOfNoInternetConnection()
        }
    }

    private fun informOfNoInternetConnection() {
        // If WebView is already visible, keep it visible
        if (binding.learnWebView.visibility != View.VISIBLE) {
            binding.learnImageView.visibility = View.VISIBLE
            binding.learnWebView.visibility = View.GONE
            showErrorSnackbarAboveNavigationView(
                R.string.learn_fragment_no_internet_connection_snackbar
            )
        } else {
            showErrorSnackbarAboveNavigationView(
                R.string.learn_fragment_no_internet_connection_snackbar
            )
        }
    }

    private fun setupProgressChange() {
        // Easier to do this than overriding Activity.onBackPressed()
        binding.learnWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                if (newProgress == 100) {
                    binding.learnProgressBar?.visibility = View.GONE
                } else {
                    binding.learnProgressBar?.visibility = View.VISIBLE
                    binding.learnProgressBar?.progress = newProgress
                }
            }
        }
    }

    private fun setupBackKeyListener() {
        binding.learnWebView.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && binding.learnWebView.canGoBack()) {
                binding.learnWebView.goBack()
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onResume() {
        super.onResume()
        initialiseToolbar(R.string.app_name, false)
    }
}
