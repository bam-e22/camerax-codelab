package k.t.cameraxsample.analyzer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import k.t.cameraxsample.BaseFragment
import k.t.cameraxsample.R
import k.t.cameraxsample.databinding.FragmentRecognizeBinding

class RecognizeFragment : BaseFragment() {
    private lateinit var binding: FragmentRecognizeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recognize, container, false)
        return binding.root
    }
}