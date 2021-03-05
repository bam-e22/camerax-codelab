package k.t.cameraxsample.utils

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.preference.PreferenceManager
import k.t.cameraxsample.R

fun getCameraXTargetResolution(context: Context, lensFacing: Int): Size? {
    if (lensFacing != CameraSelector.LENS_FACING_FRONT
        && lensFacing != CameraSelector.LENS_FACING_BACK
    ) {
        throw IllegalArgumentException("lensFacing must be CameraSelector.LENS_FACING_FRONT or CameraSelector.LENS_FACING_BACK")
    }

    val prefKey = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
        context.getString(R.string.pref_key_camerax_rear_camera_target_resolution)
    } else {
        context.getString(R.string.pref_key_camerax_front_camera_target_resolution)
    }

    val sharedPreference = PreferenceManager.getDefaultSharedPreferences(context)
    return try {
        Size.parseSize(sharedPreference.getString(prefKey, null))
    } catch (e: Exception) {
        null
    }
}