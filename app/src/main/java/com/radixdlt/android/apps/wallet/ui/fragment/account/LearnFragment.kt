package com.radixdlt.android.apps.wallet.ui.fragment.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.connectivity.ConnectivityState
import com.radixdlt.android.apps.wallet.databinding.FragmentLearnBinding
import com.radixdlt.android.apps.wallet.ui.activity.main.MainViewModel
import com.radixdlt.android.apps.wallet.util.URL_KNOWLEDGE_BASE
import com.radixdlt.android.apps.wallet.util.showErrorSnackbarAboveNavigationView

class LearnFragment : Fragment() {

    private lateinit var binding: FragmentLearnBinding
    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLearnBinding.inflate(inflater, container, false)
        observeMainViewModel()
        return initialiseWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initialiseWebView(): View {
        binding.learnWebView.loadUrl(URL_KNOWLEDGE_BASE)
        binding.learnWebView.settings.javaScriptEnabled = true
        setupProgressChange()
        setupBackKeyListener()

        return binding.root
    }

    private fun observeMainViewModel() {
        mainViewModel.connectivityLiveData.observe(this, Observer(::connectivityChange))
    }

    private fun connectivityChange(connectivityState: ConnectivityState) {
        if (connectivityState == ConnectivityState.Connected) {
            binding.learnWebView.visibility = View.VISIBLE
            binding.learnImageView.visibility = View.GONE
        } else {
            informOfNoInternetConnection()
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
                    binding.learnProgressBar.visibility = View.GONE
                } else {
                    binding.learnProgressBar.visibility = View.VISIBLE
                    binding.learnProgressBar.progress = newProgress
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
}
