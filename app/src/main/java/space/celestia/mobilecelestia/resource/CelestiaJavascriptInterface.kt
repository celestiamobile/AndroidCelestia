package space.celestia.mobilecelestia.resource

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.google.gson.Gson
import java.lang.ref.WeakReference

class CelestiaJavascriptInterface(handler: MessageHandler) {
    interface MessageHandler {
        fun runScript(type: String, content: String, scriptName: String?, scriptLocation: String?)
        fun shareURL(title: String, url: String)
        fun receivedACK(id: String)
        fun openAddonNext(id: String)
        fun runDemo()
        fun openSubscriptionPage()
    }

    @Keep
    class MessagePayload(val operation: String, val content: String, val minScriptVersion: Int)
    abstract class BaseJavascriptHandler {
        abstract val operation: String
        abstract fun executeWithContent(content: String, handler: MessageHandler)
    }

    @Keep
    class RunScriptContext(val scriptContent: String, val scriptType: String, val scriptName: String?, val scriptLocation: String?)
    @Keep
    class ShareURLContext(val title: String, val url: String)
    @Keep
    class SendACKContext(val id: String)
    @Keep
    class OpenAddonNextContext(val id: String)
    @Keep
    class RunDemoContext
    @Keep
    class OpenSubscriptionPageContext

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
            handler.runScript(context.scriptType, context.scriptContent, context.scriptName, context.scriptLocation)
        }
    }

    class ShareURLHandler: JavascriptHandler<ShareURLContext>(ShareURLContext::class.java) {
        override val operation: String
            get() = "shareURL"

        override fun execute(context: ShareURLContext, handler: MessageHandler) {
            handler.shareURL(context.title, context.url)
        }
    }

    class SendACKHandler: JavascriptHandler<SendACKContext>(SendACKContext::class.java) {
        override val operation: String
            get() = "sendACK"

        override fun execute(context: SendACKContext, handler: MessageHandler) {
            handler.receivedACK(context.id)
        }
    }

    class OpenAddonNextHandler: JavascriptHandler<OpenAddonNextContext>(OpenAddonNextContext::class.java) {
        override val operation: String
            get() = "openAddonNext"

        override fun execute(context: OpenAddonNextContext, handler: MessageHandler) {
            handler.openAddonNext(context.id)
        }
    }

    class RunDemoHandler: JavascriptHandler<RunDemoContext>(RunDemoContext::class.java) {
        override val operation: String
            get() = "runDemo"

        override fun execute(context: RunDemoContext, handler: MessageHandler) {
            handler.runDemo()
        }
    }

    class OpenSubscriptionPageHandler: JavascriptHandler<OpenSubscriptionPageContext>(OpenSubscriptionPageContext::class.java) {
        override val operation: String
            get() = "openSubscriptionPage"

        override fun execute(context: OpenSubscriptionPageContext, handler: MessageHandler) {
            handler.openSubscriptionPage()
        }
    }

    private val handler = WeakReference(handler)

    @JavascriptInterface
    fun sendMessage(message: String) {
        val messageHandler = handler.get() ?: return
        try {
            val payload = Gson().fromJson(message, MessagePayload::class.java)
            if (payload.minScriptVersion > supportedScriptVersion) return
            for (handler in contextHandlers) {
                if (handler.operation == payload.operation) {
                    handler.executeWithContent(payload.content, messageHandler)
                    break
                }
            }
        } catch (ignored: Throwable) {}
    }

    private companion object {
        val supportedScriptVersion = 5

        val contextHandlers: List<BaseJavascriptHandler> = listOf(
            RunScriptHandler(),
            ShareURLHandler(),
            SendACKHandler(),
            OpenAddonNextHandler(),
            RunDemoHandler(),
            OpenSubscriptionPageHandler()
        )
    }
}