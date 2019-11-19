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
import com.radixdlt.android.apps.wallet.databinding.FragmentAccountBinding
import com.radixdlt.android.apps.wallet.util.URL_KNOWLEDGE_BASE

class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseWebView(FragmentAccountBinding.inflate(inflater, container, false))

    @SuppressLint("SetJavaScriptEnabled")
    private fun initialiseWebView(binding: FragmentAccountBinding): View {
        binding.learnWebView.loadUrl(URL_KNOWLEDGE_BASE)
        binding.learnWebView.settings.javaScriptEnabled = true
        setupProgressChange(binding)
        setupBackKeyListener(binding)
        return binding.root
    }

    private fun setupProgressChange(binding: FragmentAccountBinding) {
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

    private fun setupBackKeyListener(binding: FragmentAccountBinding) {
        binding.learnWebView.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && binding.learnWebView.canGoBack()) {
                binding.learnWebView.goBack()
                return@OnKeyListener true
            }
            false
        })
    }
}
