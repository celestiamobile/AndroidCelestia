package space.celestia.mobilecelestia.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import space.celestia.mobilecelestia.common.TitledFragment
import space.celestia.mobilecelestia.common.pop
import space.celestia.mobilecelestia.common.push
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.core.CelestiaAppCore

import space.celestia.mobilecelestia.R

class SettingsFragment : Fragment() {

    private val toolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener {
            popItem()
        }
        val main = SettingsItemFragment.newInstance()
        replace(main, main.title)
    }

    fun replace(fragment: Fragment, title: String) {
        replace(fragment, R.id.settings_container)
        toolbar.title = title
        toolbar.navigationIcon = null
    }

    fun push(fragment: Fragment, title: String) {
        push(fragment, R.id.settings_container)
        toolbar.title = title
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_action_arrow_back)
    }

    public fun pushMainSettingItem(item: SettingsItem) {
        if (item is SettingsMultiSelectionItem) {
            push(SettingsMultiSelectionFragment.newInstance(item), item.name)
        } else if (item is SettingsSingleSelectionItem) {
            push(SettingsSingleSelectionFragment.newInstance(item), item.name)
        } else if (item is SettingsCurrentTimeItem) {
            push(SettingsCurrentTimeFragment.newInstance(), item.name)
        } else if (item is SettingsRenderInfoItem) {
            push(SimpleTextFragment.newInstance(item.name, CelestiaAppCore.shared().renderInfo), item.name)
        } else if (item is SettingsAboutItem) {
            push(AboutFragment.newInstance(), item.name)
        }
    }

    public fun reload() {
        val frag = childFragmentManager.findFragmentById(R.id.settings_container)
        if (frag is SettingsBaseFragment)
            frag.reload()
    }

    fun popItem() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
        }
        toolbar.title = (childFragmentManager.fragments[index] as TitledFragment).title
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }

}
