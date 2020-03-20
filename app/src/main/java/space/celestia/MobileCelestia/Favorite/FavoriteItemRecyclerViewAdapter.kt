package space.celestia.MobileCelestia.Favorite

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import space.celestia.MobileCelestia.Common.CommonSectionV2
import space.celestia.MobileCelestia.Common.CommonTextViewHolder


import space.celestia.MobileCelestia.Favorite.FavoriteItemFragment.Listener

import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.MobileCelestia.Core.CelestiaScript

interface FavoriteBaseItem : RecyclerViewItem {
    val children: List<FavoriteBaseItem>
    val isLeaf: Boolean
    val title: String
}

enum class FavoriteType {
    Script
}

class FavoriteRoot : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf(FavoriteTypeItem(FavoriteType.Script))
    override val title: String
        get() = "Favorites"
    override val isLeaf: Boolean
        get() = false
}

class FavoriteTypeItem(val type: FavoriteType) : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = currentScripts.map { FavoriteScriptItem(it) }
    override val title: String
        get() = type.toString()
    override val isLeaf: Boolean
        get() = false
}

class FavoriteScriptItem(val script: CelestiaScript) : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf()
    override val title: String
        get() = script.title
    override val isLeaf: Boolean
        get() = true
}

public fun updateCurrentScripts(scripts: List<CelestiaScript>) {
    currentScripts = scripts
}

private var currentScripts: List<CelestiaScript> = listOf()

class FavoriteItemRecyclerViewAdapter(
    item: FavoriteBaseItem,
    private val listener: Listener?
) : SeparatorHeaderRecyclerViewAdapter(listOf(CommonSectionV2(item.children))) {

    override fun onItemSelected(item: RecyclerViewItem) {
        (item as? FavoriteBaseItem)?.let {
            listener?.onFavoriteItemSelected(it)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is FavoriteTypeItem) {
            return FAVORITE_TYPE
        }
        if (item is FavoriteScriptItem) {
            return FAVORITE_SCRIPT
        }
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == FAVORITE_TYPE || viewType == FAVORITE_SCRIPT) {
            return CommonTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is CommonTextViewHolder && item is FavoriteBaseItem) {
            holder.title.text = item.title
            holder.accessory.visibility = if (item.isLeaf) View.GONE else View.VISIBLE
            return
        }
        super.bindVH(holder, item)
    }

    private companion object {
        const val FAVORITE_TYPE = 0
        const val FAVORITE_SCRIPT = 1
    }
}
