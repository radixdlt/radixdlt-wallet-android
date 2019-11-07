package com.radixdlt.android.apps.wallet.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import com.radixdlt.android.apps.wallet.R
import kotlinx.android.synthetic.main.activity_webview.*
import timber.log.Timber

/**
 * This Activity is used as a fallback when there is no browser installed that supports
 * Chrome Custom Tabs
 */
class WebViewActivity : BaseActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val url = intent.getStringExtra(EXTRA_URL)
        setToolBar(url)

        webview.webViewClient = WebViewClient()
        val webSettings = webview.settings
        webSettings.javaScriptEnabled = true

        webview.loadUrl(url)
    }

    private fun setToolBar(url: String?) {
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = url
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                clearWebViewHistory()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearWebViewHistory() {
        webview.clearCache(true)
        webview.clearHistory()
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies { b ->
            Timber.d("Cookies removed $b")
        }
    }

    override fun onBackPressed() {
        clearWebViewHistory()
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_URL = "extra.url"
    }
}
