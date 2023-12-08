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
import com.shash.utube.BuildConfig
import com.shash.utube.R
import com.shash.utube.service.FloatingWindowService
import com.shash.utube.utils.checkOverlayDisplayPermission
import com.shash.utube.utils.isMyServiceRunning
import com.shash.utube.utils.requestOverlayDisplayPermission
import com.shash.utube.utils.showToast

class MainActivity : AppCompatActivity() {
    // The reference variables for the
    // Button, AlertDialog, EditText
    // classes are created
    private var minimizeBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // The Buttons and the EditText are connected with
        // the corresponding component id used in layout file
        minimizeBtn = findViewById(R.id.minimizeIV)

        // Check for permissions
        checkForPermissions()

    }

    /**
     * Checks for required permissions and request if not granted.
     * Registers click listeners
     */
    private fun checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // If this service is found as a running,
            if (checkOverlayDisplayPermission()) {
                // FloatingWindowGFG service is started
                if (!isMyServiceRunning()) {
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
                    if (!isMyServiceRunning()) {
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
        }else{
            showToast("This app supports above Android 7")
        }
    }

}