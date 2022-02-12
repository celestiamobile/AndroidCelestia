package space.celestia.mobilecelestia.resource

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import java.lang.ref.WeakReference

class CelestiaJavascriptInterface(handler: MessageHandler) {
    interface MessageHandler {
        fun runScript(type: String, content: String)
        fun shareURL(title: String, url: String)
    }

    class MessagePayload(val operation: String, val content: String)
    abstract class BaseJavascriptHandler {
        abstract val operation: String
        abstract fun executeWithContent(content: String, handler: MessageHandler)
    }

    class RunScriptContext(val scriptContent: String, val scriptType: String)
    class ShareURLContext(val title: String, val url: String)

    abstract class JavascriptHandler<T>(private val clazz: Class<T>): BaseJavascriptHandler() {
        abstract fun execute(context: T, handler: MessageHandler)

        override fun executeWithContent(content: String, handler: MessageHandler) {
            try {
                val obj = Gson().fromJson(content, clazz)
                execute(obj, handler)
            } catch(ignored: Throwable) {}
        }
    }

    class RunScriptHandler: JavascriptHandler<RunScriptContext>(RunScriptContext::class.java) {
        override val operation: String
            get() = "runScript"

        override fun execute(context: RunScriptContext, handler: MessageHandler) {
            val type = context.scriptType
            val content = context.scriptContent

            handler.runScript(type, content)
        }
    }

    class ShareURLHandler: JavascriptHandler<ShareURLContext>(ShareURLContext::class.java) {
        override val operation: String
            get() = "shareURL"

        override fun execute(context: ShareURLContext, handler: MessageHandler) {
            handler.shareURL(context.title, context.url)
        }
    }

    private val handler = WeakReference(handler)

    @JavascriptInterface
    fun sendMessage(message: String) {
        val messageHandler = handler.get() ?: return
        try {
            val payload = Gson().fromJson(message, MessagePayload::class.java)
            for (handler in contextHandlers) {
                if (handler.operation == payload.operation) {
                    handler.executeWithContent(payload.content, messageHandler)
                    break
                }
            }
        } catch (ignored: Throwable) {}
    }

    private companion object {
        val contextHandlers: List<BaseJavascriptHandler> = listOf(
            RunScriptHandler(),
            ShareURLHandler(),
        )
    }
}