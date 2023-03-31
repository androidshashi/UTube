package com.shash.utube.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.monstertechno.adblocker.AdBlockerWebView
import com.shash.utube.adblocker.AdBlocker


open class MyBrowser : WebViewClient() {

    companion object{
        const val TAG ="MyBrowser"
    }

    @Nullable
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return if (AdBlockerWebView.blockAds(
                view,
                url
            )
        ) AdBlocker.createEmptyResource() else super.shouldInterceptRequest(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {


        val url = request?.url.toString()
        val context = view?.context
        Log.d(TAG, "URL=$url")

        if (url.startsWith("whatsapp://")
        ) {
            val uri = Uri.parse(url)
            val msg = uri.getQueryParameter("text")
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, msg)
            sendIntent.type = "text/plain"
            sendIntent.setPackage(WA_PACKAGE)

            if (context != null) {
                ContextCompat.startActivity(context, sendIntent, null)
            } else
                Log.d(TAG, "No context")
            return true
        } else if (url.startsWith("https://www.facebook.com/sharer.php") || url.startsWith(
                "https://twitter.com/share"
            ) || url.startsWith(
                "https://plus.google.com/share"
            )|| url.startsWith(
                "https://www.pinterest.com/pin/"
            )|| url.startsWith(
                "https://www.linkedin.com"
            )|| url.startsWith(
                "mailto:?"
            )
        ) {
            context?.shareText(url)
            view?.loadUrl(url)
            return true
        }
        return false
    }
}