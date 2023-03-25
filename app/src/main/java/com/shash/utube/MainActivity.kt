package com.shash.utube

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shash.utube.utils.checkOverlayDisplayPermission
import com.shash.utube.utils.requestOverlayDisplayPermission


class MainActivity : AppCompatActivity() {
    private var windowMain: ConstraintLayout? = null
    private lateinit var _windowManager: WindowManager
    private var backIV: ImageView? = null
    private var maximizeIV: ImageView? = null
    private var closeIV: ImageView? = null
    private var webView: WebView? = null
    private var _isMinimized = false;
    private val _url = "https://www.youtube.com/"
    private  var height:Int  =0
    private  var width:Int  =0
    private var LAYOUT_TYPE =0;
    private  var expandParams : WindowManager.LayoutParams? = null


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()

        listeners()

        configureMovableWindow()

        configureWebView()

    }

    private fun configureWebView() {
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

    private fun listeners() {
        closeIV?.setOnClickListener {
            windowManager.removeView(windowMain)
            finish()
        }

        maximizeIV?.setOnClickListener {
            if (_isMinimized) {
                expandParams?.height = height
                expandParams?.width = width
                expandParams?.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                maximizeIV?.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_minimize))
                _isMinimized = false
            }else{
                expandParams?.gravity = Gravity.CENTER
                expandParams?.width = (width*0.45).toInt()
                expandParams?.height = (height*0.18).toInt()
                expandParams?.flags =  WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                maximizeIV?.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_max))
                _isMinimized = true
            }
            windowManager.updateViewLayout(windowMain, expandParams)
        }

        backIV?.setOnClickListener {
            if (webView != null && webView!!.canGoBack()) {
                webView?.goBack()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configureMovableWindow() {
        windowMain?.setOnTouchListener(object : View.OnTouchListener {
            val floatWindowLayoutUpdateParam: WindowManager.LayoutParams = expandParams!!
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = floatWindowLayoutUpdateParam.x.toDouble()
                        y = floatWindowLayoutUpdateParam.y.toDouble()
                        //returns the original raw X coordinate of this event
                        px = event.rawX.toDouble()
                        //returns the original raw Y coordinate of this event
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        floatWindowLayoutUpdateParam.x = (x + event.rawX - px).toInt()
                        floatWindowLayoutUpdateParam.y = (y + event.rawY - py).toInt()

                        //updated parameter is applied to the WindowManager
                        _windowManager.updateViewLayout(windowMain, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initViews() {

        if (!checkOverlayDisplayPermission()){
            requestOverlayDisplayPermission()
            return
        }
        _windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        //WindowManager.LayoutParams takes a lot of parameters to set the
        //the parameters of the layout. One of them is Layout_type.
        //The screen height and width are calculated, cause
        //the height and width of the floating window is set depending on this
        val metrics = applicationContext.resources.displayMetrics
        width = metrics.widthPixels
        height = metrics.heightPixels

        LAYOUT_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //If API Level is more than 26, we need TYPE_APPLICATION_OVERLAY
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            //If API Level is lesser than 26, then we can use TYPE_SYSTEM_ERROR,
            //TYPE_SYSTEM_OVERLAY, TYPE_PHONE, TYPE_PRIORITY_PHONE. But these are all
            //deprecated in API 26 and later. Here TYPE_TOAST works best.
            WindowManager.LayoutParams.TYPE_TOAST
        }

        expandParams = WindowManager.LayoutParams(
            width,
            height,
            LAYOUT_TYPE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowMain = inflater.inflate(R.layout.activity_main, null) as ConstraintLayout?
        //
        windowManager.addView(windowMain, expandParams)

        //
        backIV = windowMain?.findViewById(R.id.backIV)
        maximizeIV = windowMain?.findViewById(R.id.maximizeIV)
        closeIV = windowMain?.findViewById(R.id.closeIV)

    }

}