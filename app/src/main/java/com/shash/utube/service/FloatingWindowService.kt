package com.shash.utube.service

import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.View.*
import android.webkit.JsResult
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shash.utube.R
import com.shash.utube.utils.*
import com.shash.utube.view.MainActivity

/**
 * This class is responsible for Floating dialog in which YouTube website will be rendered.
 * This has been tested on few devices eg: samsung f62, vivo, honor 8x etc.
 * @author: AndroidShashi
 */
class FloatingWindowService : Service() {
    //The reference variables
    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var windowManager: WindowManager? = null
    private var toggleBtn: ImageView? = null
    private var bottomLayout: LinearLayout? = null
    private var backBtn: ImageView? = null
    private var moveBtn: ImageView? = null
    private var closeBtn: ImageView? = null
    private var shareBtn: ImageView? = null
    private var searchET: EditText? = null
    private var webView: VideoEnabledWebView? = null
    private var height: Int = 0
    private var width: Int = 0
    private var isMinimized = false
    private var onlyButtons = false
    private lateinit var nonVideoLayout: RelativeLayout
    private lateinit var videoLayout: RelativeLayout
    private lateinit var loadingView: View
    private var mWebChromeClient: VideoEnabledWebChromeClient? = null

    companion object {

    }

    //As FloatingWindowGFG inherits Service class, it actually overrides the onBind method
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Show foreground notification above android 8.0
        generateForegroundNotification()
        return START_STICKY
    }

    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    private var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123333

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, FLAG_IMMUTABLE)
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager =
                    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("Utube", "bg")
                )
                val notificationChannel =
                    NotificationChannel(
                        Constants.channelID, "Service Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, Constants.channelID)

            builder.setContentTitle(
                StringBuilder(resources.getString(R.string.app_name)).append(" is running")
                    .toString()
            )
                .setContentText("Enjoy...") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_max)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }
            builder.color = resources.getColor(R.color.purple_200)
            notification = builder.build()
            startForeground(mNotificationId, notification)
        }

    }

    override fun onCreate() {
        super.onCreate()

        //Initialize all the required views
        initViews()

        //Register listeners for click and other events
        listeners()

        //Handle youtube website inside Android Web View
        initWebView()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {

        mWebChromeClient = object : VideoEnabledWebChromeClient(
            nonVideoLayout, videoLayout, loadingView, webView // See all available constructors...
        ) {
            // Subscribe to standard events, such as onProgressChanged()...
            override fun onProgressChanged(view: WebView, progress: Int) {
                // Your code...
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                //Required functionality here
                return super.onJsAlert(view, url, message, result)
            }
        }
        mWebChromeClient?.setOnToggledFullscreen { fullscreen -> // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
            if (fullscreen) {

                floatWindowLayoutParam?.flags =
                    floatWindowLayoutParam?.flags?.or(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                floatWindowLayoutParam?.flags =
                    floatWindowLayoutParam?.flags?.or(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                windowManager?.updateViewLayout(floatView, floatWindowLayoutParam)

            } else {

                floatWindowLayoutParam?.flags =
                    floatWindowLayoutParam?.flags?.and(WindowManager.LayoutParams.FLAG_FULLSCREEN.inv())
                floatWindowLayoutParam?.flags =
                    floatWindowLayoutParam?.flags?.and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv())
                windowManager?.updateViewLayout(floatView, floatWindowLayoutParam)

            }
        }

        webView?.apply {
            webChromeClient = mWebChromeClient
            webViewClient =  InsideWebViewClient()
        }

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        webView?.settings?.apply{
            javaScriptEnabled = true
            // if you want to enable zoom feature
            setSupportZoom(true)
            // on below line setting file access to true.
            allowFileAccess = true
            domStorageEnabled = true
        }

        Log.d("Current url", Common.currentUrl)

        //Just like MainActivity, the url in Maximized will stay
        webView?.loadUrl(Common.currentUrl)

    }

    /**
     * Handle minimized and full screen view
     */
    private fun toggleView(){
        webView?.visibility = VISIBLE
        if (isMinimized) {
            updateWindowSize(h = 1.0f, w = 1.0f, keyboard = true)
            isMinimized = false
            searchET?.visibility = VISIBLE
            toggleBtn?.setImageResource(R.drawable.ic_minimize)
        } else {
            updateWindowSize()
            isMinimized = true
            searchET?.visibility = GONE
            toggleBtn?.setImageResource(R.drawable.ic_max)
        }
    }

    /**
     * Listen for all the user interaction events
     */
    private fun listeners() {
        toggleBtn?.setOnClickListener {
            toggleView()
        }

        toggleBtn?.setOnLongClickListener {
            if(onlyButtons){
                webView?.visibility = VISIBLE

            }else{
                webView?.visibility = GONE
            }

            onlyButtons = !onlyButtons

            true
        }

        backBtn?.setOnClickListener {
            webView?.let {
                if (it.canGoBack()) it.goBack()
            }
        }

        searchET?.setOnKeyListener(OnKeyListener { v, keyCode, event -> // If the event is a key-down event on the "enter" button
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                if (searchET!!.text.isEmpty())
                {
                    return@OnKeyListener true
                }
                // Perform action on key press
                webView?.loadUrl(searchET!!.text.trim().toString())
                webView?.context?.hideKeyboard(webView!!)
                return@OnKeyListener true
            }
            false
        })

        shareBtn?.setOnClickListener {

            if(!isMinimized){
                toggleView()
            }
            it.context.shareText(webView?.url.toString())
        }


        //The button that helps to maximize the app
        closeBtn?.setOnClickListener(View.OnClickListener { //stopSelf() method is used to stop the service if
            //it was previously started
            stopSelf()
            //The window is removed from the screen
            windowManager!!.removeView(floatView)
            //The app will maximize again. So the MainActivity class will be called again.

        })

        //Another feature of the floating window is, the window is movable.
        //The window can be moved at any position on the screen.
        floatView!!.setOnTouchListener(object : OnTouchListener {
            val floatWindowLayoutUpdateParam: WindowManager.LayoutParams =
                floatWindowLayoutParam as WindowManager.LayoutParams
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
                        windowManager!!.updateViewLayout(floatView, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })
    }

    inner class InsideWebViewClient() : WebViewClient() {
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("whatsapp://")
            ) {
                val uri = Uri.parse(url)
                val msg = uri.getQueryParameter("text")
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, msg)
                sendIntent.type = "text/plain"
                sendIntent.flags = FLAG_ACTIVITY_NEW_TASK
                sendIntent.setPackage(WA_PACKAGE)

                view.goBack()
                toggleView()
                ContextCompat.startActivity(view.context, sendIntent, null)
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
                view.context.shareText(url)
                view.loadUrl(url)
                return true
            }
            return false
        }
    }


    /**
     * Initialize all the views
     */
    private fun initViews() {

        //The screen height and width are calculated, cause
        //the height and width of the floating window is set depending on this
        val metrics = applicationContext.resources.displayMetrics
        width = metrics.widthPixels
        height = metrics.heightPixels

        //To obtain a WindowManager of a different Display,
        //we need a Context for that display, so WINDOW_SERVICE is used
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        //A LayoutInflater instance is created to retrieve the LayoutInflater for the floating_layout xml
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //inflate a new view hierarchy from the floating_layout xml
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup?

        //The Buttons and the EditText are connected with
        //the corresponding component id used in floating_layout xml file
        toggleBtn = floatView!!.findViewById(R.id.toggleIV)
        backBtn = floatView!!.findViewById(R.id.backIV)
        closeBtn = floatView!!.findViewById(R.id.closeIV)
        moveBtn = floatView!!.findViewById(R.id.moveIV)
        shareBtn = floatView!!.findViewById(R.id.shareIV)
        searchET = floatView!!.findViewById(R.id.searchET)
        webView = floatView!!.findViewById(R.id.webView)
        bottomLayout = floatView!!.findViewById(R.id.bottomLayout)

        nonVideoLayout = floatView!!.findViewById(R.id.nonVideoLayout) // Your own view, read class comments

        videoLayout =
            floatView!!.findViewById(R.id.videoLayout) // Your own view, read class comments
        //noinspection all
        loadingView= inflater.inflate(R.layout.view_loading_video, null)

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
        windowManager!!.addView(floatView, floatWindowLayoutParam)
    }

    /**
     * Update the floating window size
     */
    private fun updateWindowSize(h: Float?=null, w: Float = 0.45f, keyboard: Boolean = false) {
        val floatWindowLayoutParamUpdateFlag: WindowManager.LayoutParams =
            (floatWindowLayoutParam as WindowManager.LayoutParams).also {
                it.width = (width * w).toInt()
                it.height = if(h==null) bottomLayout!!.height  else (height * h).toInt()
                it.type = LAYOUT_TYPE
                if (keyboard) {
                    it.flags =
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                } else {
                    it.flags =
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                }

                it.format = PixelFormat.TRANSLUCENT
            }
        //Layout Flag is changed to FLAG_NOT_TOUCH_MODAL which helps to take inputs inside floating window, but
        //while in EditText the back button won't work and FLAG_LAYOUT_IN_SCREEN flag helps to keep the window
        //always over the keyboard
        //floatWindowLayoutParamUpdateFlag.flags =

        //WindowManager is updated with the Updated Parameters
        windowManager!!.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag)
    }

    //It is called when stopService() method is called in MainActivity
    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        //Window is removed from the screen
        windowManager!!.removeView(floatView)
    }

}