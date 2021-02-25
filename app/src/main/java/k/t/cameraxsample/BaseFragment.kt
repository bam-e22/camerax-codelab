package k.t.cameraxsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import k.t.cameraxsample.utils.allPermissionGranted

open class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!requireContext().allPermissionGranted()) {
            navigateToPermissionFragment()
        }
    }

    private fun navigateToPermissionFragment() {
        findNavController().navigate(R.id.permissionFragment)
    }
}