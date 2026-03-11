package space.celestia.celestiaui.utils

import android.app.Activity
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.celestia.celestiafoundation.utils.FileUtils
import space.celestia.celestiaui.info.model.CelestiaAction

sealed class AppURLResult {
    data class Success(val url: AppURL): AppURLResult()
    
    sealed class Failure: AppURLResult() {
        data class InternalError(val error: Throwable) : Failure()
        data class UnsupportedFile(val filename: String) : Failure()
        data object MissingFileName : Failure()

        data object UnknownHost : Failure()
        data object UnknownPath : Failure()
        data object MissingParameter : Failure()
        data object MissingPath : Failure()
        data object UnsupportedScheme : Failure()
    }
}

sealed class AppURL {
    data class Script(val path: String): AppURL()
    data class CelURL(val url: String): AppURL()
    data class Addon(val id: String): AppURL()
    data class Article(val id: String): AppURL()
    data class Object(val path: String, val action: Action?): AppURL() {
        sealed class Action {
            data object Select: Action()
            data class SelectAnd(val action: CelestiaAction): Action()
        }
    }
    
    companion object {
        suspend fun fromUri(uri: Uri, activity: Activity): AppURLResult {
            if (uri.scheme == "content") {
                return fromContentUri(uri, activity)
            } else if (uri.scheme == "cel") {
                return AppURLResult.Success(CelURL(uri.toString()))
            } else if (uri.scheme == "https") {
                if (uri.host == "celestia.mobi") {
                    val segments = uri.pathSegments.filter { it.isNotEmpty() }
                    if (segments.size >= 2 && segments[0] == "resources") {
                        when (segments[1]) {
                            "item" -> {
                                val id = if (segments.size > 2) segments[2] else uri.getQueryParameter("item")
                                if (id.isNullOrEmpty()) return AppURLResult.Failure.MissingParameter
                                return AppURLResult.Success(Addon(id))
                            }
                            "guide" -> {
                                val id = if (segments.size > 2) segments[2] else uri.getQueryParameter("guide")
                                if (id.isNullOrEmpty()) return AppURLResult.Failure.MissingParameter
                                return AppURLResult.Success(Article(id))
                            }
                            else -> {
                                return AppURLResult.Failure.UnknownPath
                            }
                        }
                    } else {
                        return AppURLResult.Failure.UnknownPath
                    }
                } else {
                    return AppURLResult.Failure.UnknownHost
                }
            } else if (uri.scheme == "celaddon") {
                if (uri.host == "item") {
                    val id = uri.getQueryParameter("item") ?: return AppURLResult.Failure.MissingParameter
                    return AppURLResult.Success(Addon(id))
                } else {
                    return AppURLResult.Failure.UnknownHost
                }
            } else if (uri.scheme == "celguide") {
                if (uri.host == "guide") {
                    val id = uri.getQueryParameter("guide") ?: return AppURLResult.Failure.MissingParameter
                    return AppURLResult.Success(Article(id))
                } else {
                    return AppURLResult.Failure.UnknownHost
                }
            } else if (uri.scheme == "celestia") {
                when (uri.host) {
                    "article" -> {
                        val id = uri.pathSegments.firstOrNull({ !it.isEmpty() }) ?: return AppURLResult.Failure.MissingParameter
                        return AppURLResult.Success(Article(id))
                    }
                    "addon" -> {
                        val id = uri.pathSegments.firstOrNull({ !it.isEmpty() }) ?: return AppURLResult.Failure.MissingParameter
                        return AppURLResult.Success(Addon(id))
                    }
                    "object" -> {
                        val path = uri.pathSegments.filter({ !it.isEmpty() }).joinToString("/")
                        if (path.isEmpty()) return AppURLResult.Failure.MissingPath
                        var action: Object.Action? = null
                        uri.getQueryParameter("action")?.let { value ->
                            objectURLActionMap[value]?.let {
                                action = it
                            }
                        }
                        return AppURLResult.Success(Object(path, action))
                    }
                    else -> {
                        return AppURLResult.Failure.UnknownHost
                    }
                }
            } else {
                return AppURLResult.Failure.UnsupportedScheme
            }
        }
        
        private suspend fun fromContentUri(uri: Uri, activity: Activity): AppURLResult {
            // Content scheme, copy the resource to a temporary directory
            var itemName = uri.lastPathSegment ?: return AppURLResult.Failure.MissingFileName
            // Check file name
            val possibleFilUri = itemName.toUri()
            if (possibleFilUri.scheme == "file") {
                val possibleFileName = possibleFilUri.lastPathSegment
                if (possibleFileName != null) {
                    itemName = possibleFileName
                }
            }
            // Check file type
            if (!itemName.endsWith(".cel") && !itemName.endsWith(".celx")) {
                return AppURLResult.Failure.UnsupportedFile(itemName)
            }
            itemName = itemName.substringAfter("/")
            return withContext(Dispatchers.IO) {
                try {
                    val path = "${activity.cacheDir.absolutePath}/$itemName"
                    if (!FileUtils.copyUri(activity, uri, path)) {
                        throw RuntimeException("Failed to open $itemName")
                    }
                    return@withContext AppURLResult.Success(Script(path))
                } catch (error: Throwable) {
                    return@withContext AppURLResult.Failure.InternalError(error)
                }
            }
        }

        private val objectURLActionMap = hashMapOf<String, Object.Action>(
            "select" to Object.Action.Select,
            "go" to Object.Action.SelectAnd(CelestiaAction.GoTo),
            "center" to Object.Action.SelectAnd(CelestiaAction.Center),
            "follow" to Object.Action.SelectAnd(CelestiaAction.Follow),
            "chase" to Object.Action.SelectAnd(CelestiaAction.Chase),
            "track" to Object.Action.SelectAnd(CelestiaAction.Track),
            "syncOrbit" to Object.Action.SelectAnd(CelestiaAction.SyncOrbit),
            "lock" to Object.Action.SelectAnd(CelestiaAction.Lock),
            "land" to Object.Action.SelectAnd(CelestiaAction.GoToSurface),
        )

    }
}