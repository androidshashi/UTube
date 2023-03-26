package com.shash.utube.view

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.shash.utube.R
import com.shash.utube.utils.Common
import com.shash.utube.utils.MyBrowser


class FloatingWindowGFG : Service() {
    //The reference variables for the
    //ViewGroup, WindowManager.LayoutParams, WindowManager, Button, EditText classes are created
    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var windowManager: WindowManager? = null
    private var maximizeBtn: ImageView? = null
    private var minimizeBtn: ImageView? = null
    private var moveBtn: ImageView? = null
    private var closeBtn: ImageView? = null
    private var webView:WebView? =null
    private var progressBar: ProgressBar? = null
    private  var height:Int  =0
    private  var width:Int  =0

    companion object{
        const val channelID = "Utube_service_channel"
        const val title = "Utube is running..."
        const val text = "Enjoy in background."
    }

    //As FloatingWindowGFG inherits Service class, it actually overrides the onBind method
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        generateForegroundNotification()
        return START_STICKY

        //Normal Service To test sample service comment the above    generateForegroundNotification() && return START_STICKY
        // Uncomment below return statement And run the app.
//        return START_NOT_STICKY
    }

    //Notififcation for ON-going
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123333

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, FLAG_IMMUTABLE)
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("Utube", "bg")
                )
                val notificationChannel =
                    NotificationChannel(
                        channelID, "Service Notifications",
                        NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, channelID)

            builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" is running").toString())
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
        maximizeBtn = floatView!!.findViewById(R.id.maximizeIV)
        minimizeBtn = floatView!!.findViewById(R.id.minimizeIV)
        closeBtn = floatView!!.findViewById(R.id.closeIV)
        moveBtn = floatView!!.findViewById(R.id.moveIV)
        webView = floatView!!.findViewById(R.id.webView)
        progressBar = floatView!!.findViewById(R.id.progressBar)

        maximizeBtn?.setOnClickListener {
            updateWindowSize(h=1.0f,w=1.0f, keyboard = true)
        }

        minimizeBtn?.setOnClickListener {
            updateWindowSize()
        }

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
        windowManager!!.addView(floatView, floatWindowLayoutParam)

        //The button that helps to maximize the app
        closeBtn?.setOnClickListener(View.OnClickListener { //stopSelf() method is used to stop the service if
            //it was previously started
            stopSelf()
            //The window is removed from the screen
            windowManager!!.removeView(floatView)
            //The app will maximize again. So the MainActivity class will be called again.
            val backToHome = Intent(this@FloatingWindowGFG, MainActivity::class.java)
            //1) FLAG_ACTIVITY_NEW_TASK flag helps activity to start a new task on the history stack.
            //If a task is already running like the floating window service, a new activity will not be started.
            //Instead the task will be brought back to the front just like the MainActivity here
            //2) FLAG_ACTIVITY_CLEAR_TASK can be used in the conjunction with FLAG_ACTIVITY_NEW_TASK. This flag will
            //kill the existing task first and then new activity is started.
//            backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            startActivity(backToHome)
        })

        //Another feature of the floating window is, the window is movable.
        //The window can be moved at any position on the screen.
        floatView!!.setOnTouchListener(object : OnTouchListener {
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
                        windowManager!!.updateViewLayout(floatView, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })



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