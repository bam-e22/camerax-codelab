package k.t.cameraxsample.analyzer.mlkit

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import k.t.cameraxsample.graphics.GraphicOverlay
import timber.log.Timber

class FaceDetectorProcessor(context: Context, detectorOptions: FaceDetectorOptions?) : VisionProcessorBase<List<Face>>(context) {

    private val detector: FaceDetector

    init {
        val options = detectorOptions
            ?: FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking()
                .build()

        detector = FaceDetection.getClient(options)

        Timber.i("Face detector options: $options")
    }

    override fun stop() {
        super.stop()
        detector.close()
    }

    override fun detectInImage(inputImage: InputImage): Task<List<Face>> {
        return detector.process(inputImage)
    }

    override fun onSuccess(results: List<Face>, graphicOverlay: GraphicOverlay) {
        results.forEachIndexed { index, face ->
            Timber.d("face[$index]= $face")
            graphicOverlay.add(FaceGraphic(graphicOverlay, face))
        }
    }

    override fun onFailure(e: Exception) {
        Timber.e(e)
    }
}