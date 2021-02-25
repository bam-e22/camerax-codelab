package k.t.cameraxsample.permission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import k.t.cameraxsample.R
import k.t.cameraxsample.databinding.FragmentPermissionBinding
import k.t.cameraxsample.utils.REQUIRED_PERMISSIONS
import k.t.cameraxsample.utils.allPermissionGranted
import timber.log.Timber

class PermissionFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!requireContext().allPermissionGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPermissionBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_permission, container, false)

        return binding.root
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        val isAllGranted = isGranted.all { it.value }
        Timber.tag("toddtest").d("isGranted? $isGranted")

        if (isAllGranted) {
            findNavController().popBackStack()
        } else {
            Toast.makeText(
                requireActivity(),
                "Permissions not granted by the user",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}