package space.celestia.MobileCelestia.Settings

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import space.celestia.MobileCelestia.Common.TitledFragment
import space.celestia.MobileCelestia.R
import space.celestia.MobileCelestia.Utils.AssetUtils
import java.lang.Exception

enum class AboutAction {
    VisitOfficialWebsite, VisitOfficialForum;
}

class AboutFragment : TitledFragment() {
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = AboutRecyclerViewAdapter(createAboutItems(), listener)
            }
        }
        return view
    }

    private fun createAboutItems(): List<List<AboutItem>> {
        val array = ArrayList<List<AboutItem>>()

        // Version
        var versionName = "Unknown"
        val pm = context?.packageManager
        val pn = context?.packageName
        if (pm != null && pn != null) {
            val pi = pm.getPackageInfo(pn, 0)
            versionName = "${pi.versionName}(${pi.versionCode})"
        }
        array.add(listOf(
            VersionItem(versionName)
        ))

        // Authors
        getInfo("AUTHORS", "Authors")?.let {
            array.add(it)
        }

        // Translators
        getInfo("TRANSLATORS", "Translators")?.let {
            array.add(it)
        }

        // Links
        array.add(
            listOf(
                ActionItem(AboutAction.VisitOfficialWebsite),
                ActionItem(AboutAction.VisitOfficialForum)
            )
        )

        return array
    }

    private fun getInfo(assetPath: String, title: String): List<AboutItem>? {
        val ctx = context ?: return null
        try {
            val info = AssetUtils.readFileToText(ctx, assetPath)
            return listOf(
                TitleItem(title),
                DetailItem(info)
            )
        } catch (_: Exception) {}
        return null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AboutFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onAboutActionSelected(action: AboutAction)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AboutFragment()
    }
}
