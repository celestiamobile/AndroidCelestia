package space.celestia.mobilecelestia.favorite

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import space.celestia.mobilecelestia.common.pop
import space.celestia.mobilecelestia.common.push
import space.celestia.mobilecelestia.common.replace

import space.celestia.mobilecelestia.R

class FavoriteFragment : Fragment(), Toolbar.OnMenuItemClickListener {

    private val toolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar) }
    private var listener: Listener? = null

    val currentFrag: FavoriteItemFragment
        get() = childFragmentManager.fragments.last() as FavoriteItemFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setOnMenuItemClickListener(this)
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
        reloadMenu(item)
    }

    public fun add(item: FavoriteBaseItem) {
        val frag = currentFrag
        (frag.favoriteItem as MutableFavoriteBaseItem).append(item)
        frag.reload()
    }

    public fun remove(index: Int) {
        val frag = currentFrag
        (frag.favoriteItem as MutableFavoriteBaseItem).remove(index)
        frag.reload()
    }

    public fun rename(item: MutableFavoriteBaseItem, newName: String) {
        val frag = currentFrag
        item.rename(newName)
        frag.reload()
    }

    private fun reloadMenu(item: FavoriteBaseItem) {
        if (item is MutableFavoriteBaseItem) {
            toolbar.menu.clear()
            toolbar.menu.add(Menu.NONE, MENU_ITEM_ADD, Menu.NONE, "Add").setIcon(R.drawable.ic_add)
        } else {
            toolbar.menu.clear()
        }
    }

    fun popItem() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
        }
        val frag = childFragmentManager.fragments[index] as FavoriteItemFragment
        toolbar.title = frag.title
        reloadMenu(frag.favoriteItem!!)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item == null) { return true }
        when (item.itemId) {
            0 -> {
                listener?.addFavoriteItem(currentFrag.favoriteItem as MutableFavoriteBaseItem)
            } else -> {}
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FavoriteFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener?.saveFavorites()
        listener = null
    }

    interface Listener {
        fun addFavoriteItem(item: MutableFavoriteBaseItem)
        fun saveFavorites()
    }

    companion object {
        fun newInstance() = FavoriteFragment()

        const val MENU_ITEM_ADD = 0
    }

}
