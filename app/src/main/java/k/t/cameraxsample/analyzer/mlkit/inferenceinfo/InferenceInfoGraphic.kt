package k.t.cameraxsample.analyzer.mlkit.inferenceinfo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import k.t.cameraxsample.graphics.GraphicOverlay

/**
 * InputImageSize
 * FPS
 * Frame latency
 * Detector latency
 */
class InferenceInfoGraphic(
    graphicOverlay: GraphicOverlay,
    private val frameLatency: Long,
    private val detectorLatency: Long,
    private val framesPerSecond: Int
) : GraphicOverlay.Graphic(graphicOverlay) {

    private val textPaint: Paint = Paint().apply {
        color = TEXT_COLOR
        textSize = TEXT_SIZE
    }

    init {
        postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        val x = TEXT_SIZE * 0.5f
        val y = TEXT_SIZE * 1.5f


        // 1. Draw input image size
        canvas.drawText(
            "Input image size: ${overlay.imageHeight} x ${overlay.imageWidth}",
            x, y, textPaint
        )

        // 2. Draw FPS, inference latency
        canvas.drawText(
            "FPS: $framesPerSecond, Frame latency: $frameLatency ms",
            x, y + TEXT_SIZE, textPaint
        )

        // 3. Draw detector latency
        canvas.drawText(
            "Detector latency: $detectorLatency ms",
            x, y + TEXT_SIZE * 2, textPaint
        )
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 60.0f
    }
}

