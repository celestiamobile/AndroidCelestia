package space.celestia.mobilecelestia.favorite

import java.io.Serializable

class BookmarkNode(var name: String, var url: String, var children: ArrayList<BookmarkNode>?) : Serializable {
    val isLeaf: Boolean
        get() = children == null
}