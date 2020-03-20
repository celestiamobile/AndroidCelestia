package space.celestia.MobileCelestia.Favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import space.celestia.MobileCelestia.Common.TitledFragment
import space.celestia.MobileCelestia.Common.pop
import space.celestia.MobileCelestia.Common.push
import space.celestia.MobileCelestia.Common.replace

import space.celestia.MobileCelestia.R

class FavoriteFragment : Fragment() {

    private val toolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener {
            popItem()
        }
        replaceItem(FavoriteRoot())
    }

    private fun replaceItem(item: FavoriteBaseItem) {
        replace(FavoriteItemFragment.newInstance(item), R.id.favorite_container)
        toolbar.navigationIcon = null
        toolbar.title = item.title
    }

    public fun pushItem(item: FavoriteBaseItem) {
        val frag = FavoriteItemFragment.newInstance(item)
        push(frag, R.id.favorite_container)
        toolbar.title = item.title
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_action_arrow_back)
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
        fun newInstance() = FavoriteFragment()
    }

}
