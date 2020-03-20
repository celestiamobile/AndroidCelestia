package space.celestia.MobileCelestia.Browser

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.celestia.MobileCelestia.Core.CelestiaAppCore
import space.celestia.MobileCelestia.Core.CelestiaBrowserItem

import space.celestia.MobileCelestia.R

class BrowserFragment : Fragment(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val browserItemMenu by lazy {
        val sim = CelestiaAppCore.shared().simulation
        listOf(
            BrowserItemMenu(sim.universe.solBrowserRoot(), R.drawable.browser_tab_sso),
            BrowserItemMenu(sim.starBrowserRoot(), R.drawable.browser_tab_star),
            BrowserItemMenu(sim.universe.dsoBrowserRoot(), R.drawable.browser_tab_dso)
        )
    }

    private val toolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar) }
    private var titles = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_browser, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nav = view.findViewById<BottomNavigationView>(R.id.navigation)
        for (i in 0 until browserItemMenu.count()) {
            val item = browserItemMenu[i]
            nav.menu.add(Menu.NONE, i, Menu.NONE, item.item.name).setIcon(item.icon)
        }
        toolbar.setNavigationOnClickListener {
            pop()
        }
        nav.setOnNavigationItemSelectedListener(this)
        replace(browserItemMenu[0].item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        replace(browserItemMenu[item.itemId].item)
        return true
    }

    private fun replace(browserItem: CelestiaBrowserItem) {
        val current = childFragmentManager.findFragmentById(R.id.browser_container)
        var trans = childFragmentManager.beginTransaction()
        if (current != null) {
            trans = trans.hide(current).remove(current)
        }
        trans.add(R.id.browser_container, BrowserCommonFragment.newInstance(browserItem))
        trans.commitAllowingStateLoss()
        toolbar.navigationIcon = null
        toolbar.title = browserItem.name
        titles = arrayListOf(browserItem.name!!)
    }

    public fun push(browserItem: CelestiaBrowserItem) {
        val frag = BrowserCommonFragment.newInstance(browserItem)
        childFragmentManager.beginTransaction()
            .addToBackStack(null)
            .show(frag)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .add(R.id.browser_container, frag)
            .commitAllowingStateLoss()
        toolbar.title = browserItem.name
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_action_arrow_back)
        titles.add(browserItem.name!!)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun pop() {
        childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        if (titles.size > 1) {
            titles.removeLast()
            toolbar.title = titles.last()
            if (titles.size == 1) {
                toolbar.navigationIcon = null
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BrowserFragment()
    }
}
