package com.shash.utube.view

import android.app.ActivityManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shash.utube.R
import com.shash.utube.adblocker.AdBlocker
import com.shash.utube.utils.Common
import com.shash.utube.utils.MyBrowser
import com.shash.utube.utils.checkOverlayDisplayPermission
import com.shash.utube.utils.requestOverlayDisplayPermission

class MainActivity : AppCompatActivity() {
    //The reference variables for the
    //Button, AlertDialog, EditText classes are created
    private var minimizeBtn: FloatingActionButton? = null
    private var webView: WebView? = null
    private var progressBar: ProgressBar? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AdBlocker.initBlocker(this)

        //The Buttons and the EditText are connected with
        //the corresponding component id used in layout file
        minimizeBtn = findViewById(R.id.minimizeIV)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        webView?.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar?.visibility = View.VISIBLE
                webView?.originalUrl?.let {
                    Common.currentUrl = it
                }

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar?.visibility = View.GONE
                webView?.originalUrl?.let {
                    Common.currentUrl = it
                }
            }

        }

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        webView?.settings?.javaScriptEnabled = true

        // if you want to enable zoom feature
        webView?.settings?.setSupportZoom(true)

        webView?.webViewClient = MyBrowser()

        //Just like MainActivity, the url in Maximized will stay
        webView?.loadUrl(Common.currentUrl)

        //If the app is started again while the floating window service is running
        //then the floating window service will stop
        if (isMyServiceRunning) {
            //onDestroy() method in FloatingWindowGFG class will be called here
            stopService(Intent(this@MainActivity, FloatingWindowGFG::class.java))
        }

        minimizeBtn?.setOnClickListener {
            //First it confirms whether the 'Display over other apps' permission in given
            if (checkOverlayDisplayPermission()) {
                //FloatingWindowGFG service is started
                startService(Intent(this@MainActivity, FloatingWindowGFG::class.java))
                //The MainActivity closes here
                finish()
            } else {
                //If permission is not given, it shows the AlertDialog box and
                //redirects to the Settings
                requestOverlayDisplayPermission()

            }
        }



    }//If this service is found as a running, it will return true or else false.

    //The ACTIVITY_SERVICE is needed to retrieve a ActivityManager for interacting with the global system
    //It has a constant String value "activity".
    private val isMyServiceRunning:
    //A loop is needed to get Service information that are currently running in the System.
    //So ActivityManager.RunningServiceInfo is used. It helps to retrieve a
    //particular service information, here its this service.
    //getRunningServices() method returns a list of the services that are currently running
    //and MAX_VALUE is 2147483647. So at most this many services can be returned by this method.
            Boolean
        get() {
            //The ACTIVITY_SERVICE is needed to retrieve a ActivityManager for interacting with the global system
            //It has a constant String value "activity".
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            //A loop is needed to get Service information that are currently running in the System.
            //So ActivityManager.RunningServiceInfo is used. It helps to retrieve a
            //particular service information, here its this service.
            //getRunningServices() method returns a list of the services that are currently running
            //and MAX_VALUE is 2147483647. So at most this many services can be returned by this method.
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                //If this service is found as a running, it will return true or else false.
                if (FloatingWindowGFG::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

}