package k.t.cameraxsample.analyzer.mlkit

import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import k.t.cameraxsample.analyzer.VisionImageProcessor
import k.t.cameraxsample.analyzer.mlkit.inferenceinfo.InferenceInfoGraphic
import k.t.cameraxsample.graphics.GraphicOverlay
import k.t.cameraxsample.utils.ScopedExecutor
import java.util.*

/**
 * Abstract base class for ML Kit frame processors.
 * Subclasses need to implement
 * [onSuccess] to define what they want to with the detection result
 * and [detectInImage] to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T>(context: Context) : VisionImageProcessor {

    private val executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)

    // Whether this processor is already shut down
    private var isShutdown = false

    private val fpsTimer = Timer()
    private var framesPerSecond: Int = 0
    private var frameProcessedInOneSecondInterval: Int = 0

    init {
        fpsTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                framesPerSecond = frameProcessedInOneSecondInterval
                frameProcessedInOneSecondInterval = 0
            }
        }, 0L, 1000L)
    }

    abstract fun detectInImage(inputImage: InputImage): Task<T>
    abstract fun onSuccess(results: T, graphicOverlay: GraphicOverlay)
    abstract fun onFailure(e: Exception)

    @ExperimentalGetImage
    override fun processImageProxy(imageProxy: ImageProxy, graphicOverlay: GraphicOverlay) {
        val frameStartMs = SystemClock.elapsedRealtime()
        if (isShutdown) {
            return
        }
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        requestDetectInImage(inputImage, graphicOverlay, frameStartMs)
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun requestDetectInImage(inputImage: InputImage, graphicOverlay: GraphicOverlay, frameStartMs: Long): Task<T> {
        val detectorStartMs = SystemClock.elapsedRealtime()
        return detectInImage(inputImage)
            .addOnSuccessListener(executor) { results: T ->
                val endMs = SystemClock.elapsedRealtime()
                val frameLatencyMs = endMs - frameStartMs
                val detectorLatencyMs = endMs - detectorStartMs
                frameProcessedInOneSecondInterval++

                graphicOverlay.clear()
                
                graphicOverlay.add(
                    InferenceInfoGraphic(graphicOverlay, frameLatencyMs, detectorLatencyMs, framesPerSecond)
                )

                this@VisionProcessorBase.onSuccess(results, graphicOverlay)
                graphicOverlay.postInvalidate()
            }
            .addOnFailureListener { e ->
                graphicOverlay.clear()
                graphicOverlay.postInvalidate()
                Toast.makeText(
                    graphicOverlay.context,
                    "Failed to process.\nError: " +
                            e.localizedMessage +
                            "\nCause: " +
                            e.cause,
                    Toast.LENGTH_SHORT
                ).show()
                this@VisionProcessorBase.onFailure(e)
            }
    }

    override fun stop() {
        isShutdown = true
        executor.shutdown()
    }
}