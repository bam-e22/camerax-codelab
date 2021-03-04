package k.t.cameraxsample.fragments.mlkit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.vision.face.FaceDetectorOptions
import k.t.cameraxsample.BaseFragment
import k.t.cameraxsample.R
import k.t.cameraxsample.databinding.FragmentMlkitBinding
import timber.log.Timber
import java.util.*

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
                startCamera()
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

        return binding.root
    }

    private fun bindAllCameraUseCase() {
        // TODO
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Use case: Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Face detection
            val highAccuracyOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera

                cameraProvider.bindToLifecycle(
                    this,
                    viewModel.cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                Timber.e(e, "Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onResume() {
        super.onResume()
        bindAllCameraUseCase()
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