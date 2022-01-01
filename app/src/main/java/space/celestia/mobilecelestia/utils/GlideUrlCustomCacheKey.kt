package space.celestia.mobilecelestia.utils

import com.bumptech.glide.load.model.GlideUrl

class GlideUrlCustomCacheKey(url: String, val key: String) : GlideUrl(url) {
    override fun getCacheKey(): String {
        return key
    }
}