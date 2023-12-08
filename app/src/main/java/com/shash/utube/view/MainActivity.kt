package com.shash.utube.view

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.shash.utube.R
import com.shash.utube.service.FloatingWindowService

class MainActivity : AppCompatActivity() {
    // The reference variables for the
    // Button, AlertDialog, EditText
    // classes are created
    private var minimizeBtn: Button? = null
    private var dialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // The Buttons and the EditText are connected with
        // the corresponding component id used in layout file
        minimizeBtn = findViewById(R.id.minimizeIV)

        if (checkOverlayDisplayPermission()) {
            // FloatingWindowGFG service is started
            if (!isMyServiceRunning){
                startService(Intent(this@MainActivity, FloatingWindowService::class.java))
            }
            // The MainActivity closes here
            finish()
        } else {
            // If permission is not given,
            // it shows the AlertDialog box and
            // redirects to the Settings
            requestOverlayDisplayPermission()
        }

        // The Main Button that helps to minimize the app
        minimizeBtn?.setOnClickListener {
            // First it confirms whether the
            // 'Display over other apps' permission in given
            if (checkOverlayDisplayPermission()) {
                // FloatingWindowGFG service is started
                if (!isMyServiceRunning){
                    startService(Intent(this@MainActivity, FloatingWindowService::class.java))
                }
                // The MainActivity closes here
                finish()
            } else {
                // If permission is not given,
                // it shows the AlertDialog box and
                // redirects to the Settings
                requestOverlayDisplayPermission()
            }
        }
    }// If this service is found as a running,

    // it will return true or else false.
    // The ACTIVITY_SERVICE is needed to retrieve a
    // ActivityManager for interacting with the global system
    // It has a constant String value "activity".
    private val isMyServiceRunning:Boolean get() {
            // The ACTIVITY_SERVICE is needed to retrieve a
            // ActivityManager for interacting with the global system
            // It has a constant String value "activity".
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

            // A loop is needed to get Service information that
            // are currently running in the System.
            // So ActivityManager.RunningServiceInfo is used.
            // It helps to retrieve a
            // particular service information, here its this service.
            // getRunningServices() method returns a list of the
            // services that are currently running
            // and MAX_VALUE is 2147483647. So at most this many services
            // can be returned by this method.
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                // If this service is found as a running,
                // it will return true or else false.
                if (FloatingWindowService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }


    private fun requestOverlayDisplayPermission() {
        // An AlertDialog is created
        val builder = AlertDialog.Builder(this)

        // This dialog can be closed, just by taping
        // anywhere outside the dialog-box
        builder.setCancelable(true)

        // The title of the Dialog-box is set
        builder.setTitle("Screen Overlay Permission Needed")

        // The message of the Dialog-box is set
        builder.setMessage("Enable 'Display over other apps' from System Settings.")

        // The event of the Positive-Button is set
        builder.setPositiveButton(
            "Open Settings"
        ) { dialog, which -> // The app will redirect to the 'Display over other apps' in Settings.
            // This is an Implicit Intent. This is needed when any Action is needed
            // to perform, here it is
            // redirecting to an other app(Settings).
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            // This method will start the intent. It takes two parameter, one is the Intent and the other is
            // an requestCode Integer. Here it is -1.
            startActivityForResult(intent, RESULT_OK)
        }
        dialog = builder.create()
        // The Dialog will
        // show in the screen
        dialog!!.show()
    }

    private fun checkOverlayDisplayPermission(): Boolean {
        // Android Version is lesser than Marshmallow or
        // the API is lesser than 23
        // doesn't need 'Display over other apps' permission enabling.
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            // If 'Display over other apps' is not enabled
            // it will return false or else true
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }
}