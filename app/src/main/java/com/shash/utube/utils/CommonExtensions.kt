package com.shash.utube.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.shash.utube.utils.Constants.YOUTUBE_URL
import com.shash.utube.service.FloatingWindowService

/**
 * Share MULTIPLE the file
 */
const val WA_PACKAGE="com.whatsapp"
fun Context.shareText(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

/**
 * Checks for the background service status
 */
fun Activity.isMyServiceRunning(): Boolean {
    // The ACTIVITY_SERVICE is needed to retrieve a
    // ActivityManager for interacting with the global system
    // It has a constant String value "activity".
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?

    // A loop is needed to get Service information that are currently running in the System.
    // So ActivityManager.RunningServiceInfo is used. It helps to retrieve a
    // particular service information, here its this service.
    // getRunningServices() method returns a list of the services that are currently running
    // and MAX_VALUE is 2147483647. So at most this many services can be returned by this method.
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {

        // If this service is found as a running, it will return true or else false.
        if (FloatingWindowService::class.java.name == service.service.className) {
            return true
        }
    }
    return false
}


/**
 * Request overlay permission.
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Activity.requestOverlayDisplayPermission() {
    val dialog: Dialog
    // An AlertDialog is created
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)

    // This dialog can be closed, just by
    // taping outside the dialog-box
    builder.setCancelable(true)

    // The title of the Dialog-box is set
    builder.setTitle("Screen Overlay Permission Needed")

    // The message of the Dialog-box is set
    builder.setMessage("Enable 'Display over other apps' from System Settings.")

    // The event of the Positive-Button is set
    builder.setPositiveButton("Open Settings"
    ) { _, _ -> // The app will redirect to the 'Display over other apps' in Settings.
        // This is an Implicit Intent. This is needed when any Action is needed
        // to perform, here it is
        // redirecting to an other app(Settings).
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )

        // This method will start the intent. It takes two parameter,
        // one is the Intent and the other is
        // an requestCode Integer. Here it is -1.
        startActivityForResult(intent, RESULT_OK)
    }
    dialog = builder.create()
    // The Dialog will show in the screen
    dialog.show()
}

/**
 * Checks for overlay permission
 */
fun Activity.checkOverlayDisplayPermission(): Boolean {
    // Android Version is lesser than Marshmallow
    // or the API is lesser than 23
    // doesn't need 'Display over other apps' permission enabling.
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        // If 'Display over other apps' is not enabled it
        // will return false or else true
        return Settings.canDrawOverlays(this)
    } else {
        true
    }
}

/**
 * Shows a toast
 */
fun Activity.showToast(text:String){
    Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
}

/**
 * Hides soft keyboard
 */
fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}
/**
 * Hides soft keyboard
 */
fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}
/**
 * Hides soft keyboard
 */
fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

object Common {
    //The EditText String will be stored in this variable
    //when MINIMIZE or MAXIMIZE button in pressed
    var currentUrl = YOUTUBE_URL

    //The EditText String will be stored in this variable
    //when SAVE button is pressed
    var savedDesc = ""
}

object Constants{
    var  YOUTUBE_URL = "https://youtube.com"
    const val channelID = "Utube_service_channel"
    const val title = "Utube is running..."
    const val text = "Enjoy YouTube in background."
}

