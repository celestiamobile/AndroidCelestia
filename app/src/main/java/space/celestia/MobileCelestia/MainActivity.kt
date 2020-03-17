package space.celestia.MobileCelestia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.MobileCelestia.Loading.LoadingFragment
import space.celestia.MobileCelestia.Utils.AssetUtils
import space.celestia.MobileCelestia.Utils.PreferenceManager
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private val celestiaFolderName = "CelestiaResources"
    private val celestiaCfgName = "celestia.cfg"

    // Fragments
    private val celestiaFragment = CelestiaFragment()
    private val loadingFragment = LoadingFragment()

    private val preferenceManager by lazy { PreferenceManager(this, "celestia") }
    private val celestiaParentPath by lazy { this.filesDir.absolutePath }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) { return }

        // Add fragments
        supportFragmentManager
            .beginTransaction()
            .add(R.id.celestia_fragment_container, celestiaFragment)
            .commitAllowingStateLoss()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.loading_fragment_container, loadingFragment)
            .commitAllowingStateLoss()

        // Check if data is already copied
        if (preferenceManager[PreferenceManager.Preference.AssetCopied] == null) {
            copyAssets()
        } else {
            copyAssetSuccess()
        }
    }

    private fun copyAssets() {
        loadingFragment.update("Copying data...")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                AssetUtils.copyFileOrDir(this@MainActivity,celestiaFolderName, celestiaParentPath)
                preferenceManager[PreferenceManager.Preference.AssetCopied] = "true"
                withContext(Dispatchers.Main) {
                    copyAssetSuccess()
                }
            } catch (exp: IOException) {
                Log.e(TAG, "Copy data failed, ${exp.localizedMessage}")

                withContext(Dispatchers.Main) {
                    loadingFragment.update("Copying data failed...")
                }
            }
        }
    }

    private fun copyAssetSuccess() {
        celestiaFragment.requestLoadCelestia("$celestiaParentPath/$celestiaFolderName", "$celestiaParentPath/$celestiaFolderName/$celestiaCfgName", {
            GlobalScope.launch(Dispatchers.Main) {
                loadingFragment.update(it)
            }
        }, { success ->
            GlobalScope.launch(Dispatchers.Main) {
                if (success) {
                    supportFragmentManager.beginTransaction().remove(loadingFragment).commitAllowingStateLoss()
                } else {
                    loadingFragment.update("Loading Celestia failed...")
                }
            }
        })
    }

    companion object {
        init {
            System.loadLibrary("celestia")
        }
    }
}
