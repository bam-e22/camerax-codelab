package k.t.cameraxsample.fragments.mlkit

import android.app.Application
import android.os.Parcelable
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.concurrent.ExecutionException

class MlKitViewModel(application: Application, var lensFacing: Int, selectedMode: AnalyzeModel) : AndroidViewModel(application) {
    private val onReadyEvent = MutableLiveData<Boolean>()

    var cameraSelector: CameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    var selectedModel: AnalyzeModel = selectedMode
        private set

    var cameraProvider: ProcessCameraProvider? = null
        private set

    fun initializeCamera(): LiveData<Boolean> {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                onReadyEvent.value = true
            } catch (e: ExecutionException) {
                Timber.e(e, "Unhandled exception")
            } catch (e: InterruptedException) { // Handle any errors (including cancellation) here.
                Timber.e(e, "Unhandled exception")
            }
        }, ContextCompat.getMainExecutor(getApplication()))

        return onReadyEvent
    }

    fun onInitialized() {
        onReadyEvent.value = false
    }

    fun onFlipCamera() {
        if (cameraProvider == null) {
            Timber.d("ignore flip camera")
            return
        }

        val newLensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        val newCameraSelector = CameraSelector.Builder().requireLensFacing(newLensFacing).build()

        try {
            if (cameraProvider?.hasCamera(newCameraSelector) == true) {
                lensFacing = newLensFacing
                cameraSelector = newCameraSelector
                onReadyEvent.value = true
                Timber.d("camera changed")
                return
            }
        } catch (e: CameraInfoUnavailableException) {
            Timber.e(e)
            // fall through
        }

        Timber.e("This device does not have lens with facing: $newLensFacing")
    }
}

@Suppress("UNCHECKED_CAST")
class MlKitViewModelFactory(
    private val application: Application, private val lensFacing: Int, private val selectedMode: AnalyzeModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MlKitViewModel(application, lensFacing, selectedMode) as T
    }
}

@Parcelize
enum class AnalyzeModel : Parcelable {
    FACE_DETECTION
}