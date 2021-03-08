package k.t.cameraxsample.utils

import android.content.Context
import android.util.Size
import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector
import androidx.preference.PreferenceManager
import com.google.mlkit.vision.face.FaceDetectorOptions
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

fun getFaceDetectorOptions(context: Context): FaceDetectorOptions {
    val landmarkMode: Int = getModeTypePreferenceValue(
        context,
        R.string.pref_key_face_detection_landmark_mode,
        FaceDetectorOptions.LANDMARK_MODE_NONE
    )
    val contourMode: Int = getModeTypePreferenceValue(
        context,
        R.string.pref_key_face_detection_contour_mode,
        FaceDetectorOptions.CONTOUR_MODE_ALL
    )
    val classificationMode: Int = getModeTypePreferenceValue(
        context,
        R.string.pref_key_face_detection_classification_mode,
        FaceDetectorOptions.CLASSIFICATION_MODE_NONE
    )
    val performanceMode: Int = getModeTypePreferenceValue(
        context,
        R.string.pref_key_face_detection_performance_mode,
        FaceDetectorOptions.PERFORMANCE_MODE_FAST
    )

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val enableFaceTracking = sharedPreferences.getBoolean(
        context.getString(R.string.pref_key_face_detection_face_tracking), false
    )
    val minFaceSize =
        sharedPreferences.getString(
            context.getString(R.string.pref_key_face_detection_min_face_size),
            "0.1"
        )!!.toFloat()

    val optionsBuilder = FaceDetectorOptions.Builder()
        .setLandmarkMode(landmarkMode)
        .setContourMode(contourMode)
        .setClassificationMode(classificationMode)
        .setPerformanceMode(performanceMode)
        .setMinFaceSize(minFaceSize)
    if (enableFaceTracking) {
        optionsBuilder.enableTracking()
    }
    return optionsBuilder.build()
}

private fun getModeTypePreferenceValue(context: Context, @StringRes prefKeyResId: Int, defaultValue: Int): Int {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val prefKey = context.getString(prefKeyResId)
    return sharedPreferences.getString(prefKey, defaultValue.toString())!!.toInt()
}