package k.t.cameraxsample.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

fun Context.allPermissionGranted() = REQUIRED_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}