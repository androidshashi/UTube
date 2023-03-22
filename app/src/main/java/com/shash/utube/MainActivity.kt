package com.shash.utube

import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    private var windowMain: ConstraintLayout? = null
    private lateinit var _windowManager: WindowManager
    private var btnGhost: FloatingActionButton? = null
    private var webView: WebView? = null
    private var _isMinimized = false;
    private val _url = "https://www.youtube.com/"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val expandParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowMain = inflater.inflate(R.layout.activity_main, null) as ConstraintLayout?
        //
        windowManager.addView(windowMain, expandParams)

        //
        btnGhost = windowMain?.findViewById(R.id.ghostFab)

        btnGhost?.setOnClickListener {
            expandParams.gravity = Gravity.START or Gravity.TOP

            if (_isMinimized) {
                expandParams.height = WindowManager.LayoutParams.MATCH_PARENT
                expandParams.width = WindowManager.LayoutParams.MATCH_PARENT
                _isMinimized = false
            } else {
                expandParams.width = 400
                expandParams.height = 300
                _isMinimized = true
            }
            expandParams.x = 0;
            expandParams.y = 0;
            windowManager.updateViewLayout(windowMain, expandParams)
        }

        btnGhost?.setOnLongClickListener {
            windowManager.removeView(windowMain)
            finish()
            true
        }
        //
        webView = windowMain?.findViewById(R.id.webView)
        // WebViewClient allows you to handle
        // onPageFinished and override Url loading.
        webView?.webViewClient = WebViewClient()

        // this will load the url of the website
        webView?.loadUrl(_url)

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        webView?.settings?.javaScriptEnabled = true

        // if you want to enable zoom feature
        webView?.settings?.setSupportZoom(true)
    }

    override fun onBackPressed() {
        // if your webView can go back it will go back
        if (webView != null && webView!!.canGoBack()) {
            webView?.goBack()
        } else super.onBackPressed()

    }
}