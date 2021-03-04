package k.t.cameraxsample

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceFragmentCompat
import k.t.cameraxsample.databinding.ActivitySettingBinding
import k.t.cameraxsample.fragments.preference.MlKitPreferenceFragment
import kotlinx.parcelize.Parcelize

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivitySettingBinding>(this, R.layout.activity_setting)
        val launchSource = intent.getParcelableExtra(EXTRA_LAUNCH_SOURCE) ?: LaunchSource.ML_KIT

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, launchSource.prefFragmentClass.getDeclaredConstructor().newInstance())
            .commit()
    }

    companion object {
        const val EXTRA_LAUNCH_SOURCE = "EXTRA_LAUNCH_SOURCE"
    }
}

@Parcelize
enum class LaunchSource(val title: String, val prefFragmentClass: Class<out PreferenceFragmentCompat>) : Parcelable {
    ML_KIT("MlKit preference", MlKitPreferenceFragment::class.java)
}