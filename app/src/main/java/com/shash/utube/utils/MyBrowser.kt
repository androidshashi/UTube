package com.shash.utube.utils

import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Nullable
import com.monstertechno.adblocker.AdBlockerWebView
import com.shash.utube.adblocker.AdBlocker


open class MyBrowser : WebViewClient() {

    @Nullable
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return if (AdBlockerWebView.blockAds(
                view,
                url
            )
        ) AdBlocker.createEmptyResource() else super.shouldInterceptRequest(view, url)
    }
}