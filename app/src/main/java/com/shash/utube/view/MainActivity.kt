package com.shash.utube.view

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.shash.utube.R
import com.shash.utube.utils.Common
import com.shash.utube.utils.MyBrowser
import com.shash.utube.utils.checkOverlayDisplayPermission
import com.shash.utube.utils.requestOverlayDisplayPermission

class MainActivity : AppCompatActivity() {
    //The reference variables for the
    //ViewGroup, WindowManager.LayoutParams, WindowManager, Button, EditText classes are created
    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var _windowManager: WindowManager? = null
    private var maximizeBtn: ImageView? = null
    private var minimizeBtn: ImageView? = null
    private var moveBtn: ImageView? = null
    private var closeBtn: ImageView? = null
    private var webView:WebView? =null
    private var progressBar: ProgressBar? = null
    private  var height:Int  =0
    private  var width:Int  =0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        initViews()


        if (!checkOverlayDisplayPermission()){
            requestOverlayDisplayPermission()
        }

        maximizeBtn?.setOnClickListener {
            updateWindowSize(h=1.0f,w=1.0f, keyboard = true)
        }

        minimizeBtn?.setOnClickListener {
            updateWindowSize()
        }

        //The button that helps to maximize the app
        closeBtn?.setOnClickListener(View.OnClickListener { //stopSelf() method is used to stop the service if

            //The window is removed from the screen
            _windowManager!!.removeView(floatView)
            finish()
        })

        webView?.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar?.visibility = View.VISIBLE
                url?.let {
                    webView?.originalUrl?.let {
                        Common.currentUrl = it
                    }
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

        Log.d("Current url",Common.currentUrl)

        //Just like MainActivity, the url in Maximized will stay
        webView?.loadUrl(Common.currentUrl)

        //WindowManager.LayoutParams takes a lot of parameters to set the
        //the parameters of the layout. One of them is Layout_type.
        LAYOUT_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //If API Level is more than 26, we need TYPE_APPLICATION_OVERLAY
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            //If API Level is lesser than 26, then we can use TYPE_SYSTEM_ERROR,
            //TYPE_SYSTEM_OVERLAY, TYPE_PHONE, TYPE_PRIORITY_PHONE. But these are all
            //deprecated in API 26 and later. Here TYPE_TOAST works best.
            WindowManager.LayoutParams.TYPE_TOAST
        }

        //Now the Parameter of the floating-window layout is set.
        //1) The Width of the window will be 55% of the phone width.
        //2) The Height of the window will be 58% of the phone height.
        //3) Layout_Type is already set.
        //4) Next Parameter is Window_Flag. Here FLAG_NOT_FOCUSABLE is used. But
        //problem with this flag is key inputs can't be given to the EditText.
        //This problem is solved later.
        //5) Next parameter is Layout_Format. System chooses a format that supports translucency by PixelFormat.TRANSLUCENT
        floatWindowLayoutParam = WindowManager.LayoutParams(
            width, height,
            LAYOUT_TYPE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        //The Gravity of the Floating Window is set. The Window will appear in the center of the screen
        floatWindowLayoutParam!!.gravity = Gravity.CENTER
        //X and Y value of the window is set
        floatWindowLayoutParam!!.x = 0
        floatWindowLayoutParam!!.y = 0


        //The ViewGroup that inflates the floating_layout.xml is
        //added to the WindowManager with all the parameters
        _windowManager!!.addView(floatView, floatWindowLayoutParam)



        //Another feature of the floating window is, the window is movable.
        //The window can be moved at any position on the screen.
        floatView!!.setOnTouchListener(object : View.OnTouchListener {
            val floatWindowLayoutUpdateParam: WindowManager.LayoutParams = floatWindowLayoutParam as WindowManager.LayoutParams
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
                        _windowManager!!.updateViewLayout(floatView, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })

    }

    private fun initViews() {
        //The screen height and width are calculated, cause
        //the height and width of the floating window is set depending on this
        val metrics = applicationContext.resources.displayMetrics
        width = metrics.widthPixels
        height = metrics.heightPixels

        //To obtain a WindowManager of a different Display,
        //we need a Context for that display, so WINDOW_SERVICE is used
        _windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        //A LayoutInflater instance is created to retrieve the LayoutInflater for the floating_layout xml
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //inflate a new view hierarchy from the floating_layout xml
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup?

        //The Buttons and the EditText are connected with
        //the corresponding component id used in floating_layout xml file
        maximizeBtn = floatView!!.findViewById(R.id.maximizeIV)
        minimizeBtn = floatView!!.findViewById(R.id.minimizeIV)
        closeBtn = floatView!!.findViewById(R.id.closeIV)
        moveBtn = floatView!!.findViewById(R.id.moveIV)
        webView = floatView!!.findViewById(R.id.webView)
        progressBar = floatView!!.findViewById(R.id.progressBar)
    }

    private fun updateWindowSize(h:Float=0.18f, w:Float=0.45f,keyboard:Boolean=false){

        val floatWindowLayoutParamUpdateFlag: WindowManager.LayoutParams =
            (floatWindowLayoutParam as WindowManager.LayoutParams).also {
                it.width =(width * w).toInt()
                it.height = (height * h).toInt()
                it.type = LAYOUT_TYPE
                if (keyboard){
                    it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                }else{
                    it.flags =   WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                }

                it.format = PixelFormat.TRANSLUCENT
            }
        //Layout Flag is changed to FLAG_NOT_TOUCH_MODAL which helps to take inputs inside floating window, but
        //while in EditText the back button won't work and FLAG_LAYOUT_IN_SCREEN flag helps to keep the window
        //always over the keyboard
        //floatWindowLayoutParamUpdateFlag.flags =

        //WindowManager is updated with the Updated Parameters
        _windowManager!!.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag)
    }

    //It is called when stopService() method is called in MainActivity
    override fun onDestroy() {
        super.onDestroy()
        //Window is removed from the screen
        _windowManager!!.removeView(floatView)
    }
}