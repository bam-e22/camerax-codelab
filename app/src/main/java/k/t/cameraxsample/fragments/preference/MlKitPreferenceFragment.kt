package k.t.cameraxsample.fragments.preference

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import k.t.cameraxsample.R

class MlKitPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_mlkit, rootKey)
    }
}