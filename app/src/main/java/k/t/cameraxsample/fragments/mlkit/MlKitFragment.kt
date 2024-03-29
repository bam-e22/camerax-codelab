package k.t.cameraxsample.fragments.mlkit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import k.t.cameraxsample.BaseFragment
import k.t.cameraxsample.LaunchSource
import k.t.cameraxsample.R
import k.t.cameraxsample.analyzer.mlkit.facedetection.FaceDetectorProcessor
import k.t.cameraxsample.databinding.FragmentMlkitBinding
import k.t.cameraxsample.utils.getCameraXTargetResolution
import k.t.cameraxsample.utils.getFaceDetectorOptions
import timber.log.Timber

class MlKitFragment : BaseFragment() {
    private lateinit var binding: FragmentMlkitBinding
    private lateinit var viewModel: MlKitViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mlkit, container, false)

        var lensFacing = CameraSelector.LENS_FACING_BACK
        var selectedModel = AnalyzeModel.FACE_DETECTION

        if (savedInstanceState != null) {
            savedInstanceState.getParcelable<AnalyzeModel>(STATE_SELECTED_MODEL)?.let {
                selectedModel = it
            }

            lensFacing = savedInstanceState.getInt(STATE_LENS_FACING, CameraSelector.LENS_FACING_BACK)
        }

        viewModel = ViewModelProvider(this, MlKitViewModelFactory(requireActivity().application, lensFacing, selectedModel))
            .get(MlKitViewModel::class.java)

        binding.mlKitViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.initializeCamera().observe(viewLifecycleOwner, { initialized ->
            if (initialized) {
                viewModel.onInitialized()
                bindAllCameraUseCase()
            }
        })

        val spinnerDataAdapter = ArrayAdapter(requireContext(), R.layout.spinner_style, viewModel.analyzedModelList)
        spinnerDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = spinnerDataAdapter
        binding.spinner.setSelection(viewModel.analyzedModelList.indexOf(selectedModel))
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedAnalyzedModel = viewModel.analyzedModelList[position]
                Timber.d("item selected[$position, $id]: $selectedAnalyzedModel")
                if (selectedAnalyzedModel != viewModel.selectedModel) {
                    viewModel.onAnalyzeModelChanged(selectedAnalyzedModel)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        binding.btnSetting.setOnClickListener {
            val action = MlKitFragmentDirections.actionMlKitFragmentToSettingActivity(LaunchSource.ML_KIT)
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun bindAllCameraUseCase() {
        viewModel.cameraProvider?.unbindAll()
        bindPreviewUseCase()
        bindAnalysisUseCase()
    }

    private fun bindPreviewUseCase() {
        if (viewModel.cameraProvider == null) {
            return
        }

        val previewBuilder = Preview.Builder()
        val targetResolution = getCameraXTargetResolution(requireContext(), viewModel.lensFacing)
        if (targetResolution != null) {
            previewBuilder.setTargetResolution(targetResolution)
        }

        val preview = previewBuilder
            .build()
            .also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

        try {
            viewModel.cameraProvider?.bindToLifecycle(
                this,
                viewModel.cameraSelector,
                preview
            )
        } catch (e: Exception) {
            Timber.e(e, "Preview use case binding failed")
        }
    }

    private fun bindAnalysisUseCase() {
        if (viewModel.cameraProvider == null) {
            return
        }

        if (viewModel.imageProcessor != null) {
            viewModel.imageProcessor?.stop()
        }

        viewModel.imageProcessor = try {
            when (viewModel.selectedModel) {
                AnalyzeModel.FACE_DETECTION -> {
                    Timber.i("Using face detector processor")

                    val faceDetectorOptions = getFaceDetectorOptions(requireContext())
                    FaceDetectorProcessor(requireContext(), faceDetectorOptions)
                }
                else -> throw IllegalArgumentException("Invalid model")
            }

        } catch (e: Exception) {
            Timber.e(e)
            Toast.makeText(requireContext(), "Can not create image processor: " + e.localizedMessage, Toast.LENGTH_LONG).show()
            return
        }

        val builder = ImageAnalysis.Builder()
        val targetResolution = getCameraXTargetResolution(requireContext(), viewModel.lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }

        viewModel.needUpdateGraphicOverlayImageSourceInfo = true

        val analysisUseCase = builder.build()
        analysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
            if (viewModel.needUpdateGraphicOverlayImageSourceInfo) {
                val isImageFlipped = viewModel.lensFacing == CameraSelector.LENS_FACING_FRONT
                val rotationDegree = imageProxy.imageInfo.rotationDegrees

                if (rotationDegree == 0 || rotationDegree == 180) {
                    binding.graphicOverlay.setImageSourceInfo(imageProxy.width, imageProxy.height, isImageFlipped)
                } else {
                    binding.graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width, isImageFlipped)
                }

                viewModel.needUpdateGraphicOverlayImageSourceInfo = false
            }
            try {
                viewModel.imageProcessor?.processImageProxy(imageProxy, binding.graphicOverlay)
            } catch (e: Exception) {
                Timber.e(e)
                val msg = "Failed to process image. Error: ${e.localizedMessage}"
                Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        try {
            viewModel.cameraProvider?.bindToLifecycle(this, viewModel.cameraSelector, analysisUseCase)
        } catch (e: Exception) {
            Timber.e(e, "Analysis use case binding failed")
        }
    }

    override fun onResume() {
        super.onResume()
        bindAllCameraUseCase()
    }

    override fun onPause() {
        super.onPause()
        viewModel.imageProcessor?.stop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(STATE_SELECTED_MODEL, viewModel.selectedModel)
        outState.putInt(STATE_LENS_FACING, viewModel.lensFacing)
    }

    companion object {
        private const val STATE_SELECTED_MODEL = "STATE_SELECTED_MODEL"
        private const val STATE_LENS_FACING = "STATE_LENS_FACING"
    }
}