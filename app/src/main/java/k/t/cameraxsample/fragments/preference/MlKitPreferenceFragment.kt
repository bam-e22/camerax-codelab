package k.t.cameraxsample.fragments.preference

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import androidx.camera.core.CameraSelector
import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import k.t.cameraxsample.R
import timber.log.Timber

class MlKitPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_mlkit, rootKey)

        setUpCameraXTargetResolutionPreference(R.string.pref_key_camerax_rear_camera_target_resolution, CameraSelector.LENS_FACING_BACK)
        setUpCameraXTargetResolutionPreference(R.string.pref_key_camerax_front_camera_target_resolution, CameraSelector.LENS_FACING_FRONT)
    }

    private fun setUpCameraXTargetResolutionPreference(key: Int, lensFacing: Int) {
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