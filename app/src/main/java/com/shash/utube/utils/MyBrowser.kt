package com.shash.utube.utils

import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Nullable
import com.shash.utube.adblocker.AdBlocker

class MyBrowser : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }

    private val loadedUrls: MutableMap<String, Boolean> = HashMap()

    @Nullable
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val ad: Boolean
        if (!loadedUrls.containsKey(url)) {
            ad = AdBlocker.isAd(url)
            loadedUrls[url] = ad
        } else {
            ad = loadedUrls[url]!!
        }
        return if (ad) AdBlocker.createEmptyResource() else super.shouldInterceptRequest(view, url)
    }
}