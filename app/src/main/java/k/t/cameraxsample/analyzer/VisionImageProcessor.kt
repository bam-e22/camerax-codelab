package k.t.cameraxsample.analyzer

import androidx.camera.core.ImageProxy
import com.google.mlkit.common.MlKitException
import k.t.cameraxsample.graphics.GraphicOverlay

/** An interface to process the images with different vision detectors and custom image models */
interface VisionImageProcessor {

    /** Processes ImageProxy image data, e.g. used for CameraX live preview case. */
    @Throws(MlKitException::class)
    fun processImageProxy(imageProxy: ImageProxy, graphicOverlay: GraphicOverlay)

    fun stop()
}