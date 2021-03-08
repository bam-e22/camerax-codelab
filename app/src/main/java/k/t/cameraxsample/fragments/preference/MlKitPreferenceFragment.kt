package k.t.cameraxsample.fragments.preference

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector
import androidx.core.content.edit
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import k.t.cameraxsample.R
import timber.log.Timber

class MlKitPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_mlkit, rootKey)

        setUpTargetResolutionPreference()
        setUpFaceDetectionPreference()
    }

    private fun setUpTargetResolutionPreference() {
        // Rear camera target resolution
        setUpCameraXTargetResolutionPreference(R.string.pref_key_camerax_rear_camera_target_resolution, CameraSelector.LENS_FACING_BACK)
        // Front camera target resolution
        setUpCameraXTargetResolutionPreference(R.string.pref_key_camerax_front_camera_target_resolution, CameraSelector.LENS_FACING_FRONT)
    }

    private fun setUpFaceDetectionPreference() {
        // 1. Face detection - Landmark mode
        setUpListPreference(R.string.pref_key_face_detection_landmark_mode)
        // 2. Face detection - Contour mode
        setUpListPreference(R.string.pref_key_face_detection_contour_mode)
        // 3. Face detection - Classification mode
        setUpListPreference(R.string.pref_key_face_detection_classification_mode)
        // 4. Face detection - Performance mode
        setUpListPreference(R.string.pref_key_face_detection_performance_mode)
        // 5. Face detection - face tracking: pref_key_face_detection_face_tracking
        // 6. Face detection - Min face size
        @Suppress("SameParameterValue")
        setUpFaceDetectionMinFaceSizePreference(R.string.pref_key_face_detection_min_face_size)
    }

    private fun setUpListPreference(@StringRes key: Int) {
        val listPreference = findPreference<ListPreference>(getString(key)) ?: return

        listPreference.summary = listPreference.entry
        listPreference.setOnPreferenceChangeListener { _, _newValue ->
            val newValue = _newValue as String
            val index = listPreference.findIndexOfValue(newValue)
            listPreference.summary = listPreference.entries[index]
            true
        }
    }

    private fun setUpFaceDetectionMinFaceSizePreference(@StringRes key: Int) {
        val minFaceSizePreference = findPreference<EditTextPreference>(getString(key)) ?: return
        minFaceSizePreference.summary = minFaceSizePreference.text
        minFaceSizePreference.setOnPreferenceChangeListener { _, _newValue ->
            val newFaceSize = (_newValue as String).toFloat()
            try {
                if (newFaceSize in 0.0f..1.0f) {
                    minFaceSizePreference.summary = newFaceSize.toString()
                    return@setOnPreferenceChangeListener true
                }
            } catch (e: NumberFormatException) {
                // Fall through intentionally.
            }
            Toast.makeText(
                activity, R.string.pref_toast_invalid_min_face_size, Toast.LENGTH_LONG
            )
                .show()
            false
        }
    }

    private fun setUpCameraXTargetResolutionPreference(@StringRes key: Int, lensFacing: Int) {
        val preference = findPreference<ListPreference>(getString(key)) ?: return
        val cameraCharacteristic = getCameraCharacteristic(requireContext(), lensFacing)

        val outputSizes = if (cameraCharacteristic != null) {
            val streamConfigurationMap = cameraCharacteristic.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            streamConfigurationMap?.getOutputSizes(SurfaceTexture::class.java)
                ?.map { it.toString() }
                ?.toTypedArray()
        } else {
            arrayOf(
                "2000x2000",
                "1600x1600",
                "1200x1200",
                "1000x1000",
                "800x800",
                "600x600",
                "400x400",
                "200x200",
                "100x100",
            )
        }

        with(preference) {
            entries = outputSizes
            entryValues = outputSizes
            summary = if (entry == null) {
                "default"
            } else {
                entry
            }
            setOnPreferenceChangeListener { _, _newValue ->
                val newValue = _newValue as String
                summary = newValue
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit {
                        putString(getString(key), newValue)
                    }
                true
            }
        }
    }

    private fun getCameraCharacteristic(context: Context, lensFacing: Int): CameraCharacteristics? {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return null

        return try {
            cameraManager.cameraIdList
                .asSequence()
                .map { cameraId ->
                    cameraManager.getCameraCharacteristics(cameraId)
                }.firstOrNull { characteristic ->
                    lensFacing == characteristic.get(CameraCharacteristics.LENS_FACING)
                }
        } catch (e: CameraAccessException) {
            // Accessing camera ID info got error
            Timber.e(e)
            null
        }
    }
}