package com.shash.utube.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.shash.utube.utils.Constants.YOUTUBE_URL


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
    builder.setPositiveButton("Open Settings",
        DialogInterface.OnClickListener { _, _ -> // The app will redirect to the 'Display over other apps' in Settings.
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
        })
    dialog = builder.create()
    // The Dialog will show in the screen
    dialog.show()
}

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

fun Activity.showToast(text:String){
    Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
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
}