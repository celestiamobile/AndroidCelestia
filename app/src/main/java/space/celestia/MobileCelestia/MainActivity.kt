package space.celestia.MobileCelestia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.MobileCelestia.Utils.AssetUtils
import space.celestia.MobileCelestia.Utils.PreferenceManager
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val celestiaFolderName = "CelestiaResources"
    private val celestiaFragment = CelestiaFragment()
    private val preferenceManager by lazy { PreferenceManager(this, "celestia") }
    private val celestiaParentPath by lazy { this.filesDir.absolutePath }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.celestia_fragment_container, celestiaFragment)
                .commitAllowingStateLoss()

            if (preferenceManager[PreferenceManager.Preference.AssetCopied] == null) {
                copyAssets()
            } else {
                copyAssetSuccess()
            }
        }
    }

    private fun copyAssets() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                AssetUtils.copyFileOrDir(this@MainActivity,"CelestiaResources", celestiaParentPath)
                preferenceManager[PreferenceManager.Preference.AssetCopied] = "true"
                withContext(Dispatchers.Main) {
                    copyAssetSuccess()
                }
            } catch (exp: IOException) {
                // TODO: shows error
                print("error")
            }
        }
    }

    private fun copyAssetSuccess() {
        // TODO: loading process and error handling
        celestiaFragment.requestLoadCelestia("$celestiaParentPath/$celestiaFolderName")
    }

    companion object {
        init {
            System.loadLibrary("celestia")
        }
    }
}
