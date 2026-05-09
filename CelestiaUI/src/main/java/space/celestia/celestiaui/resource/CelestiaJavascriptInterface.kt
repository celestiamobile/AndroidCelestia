package space.celestia.celestiaui.resource

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.ref.WeakReference

class CelestiaJavascriptInterface(handler: MessageHandler) {
    interface MessageHandler {
        fun runScript(type: String, content: String, scriptName: String?, scriptLocation: String?)
        fun shareURL(title: String, url: String)
        fun receivedACK(id: String)
        fun openAddonNext(id: String)
        fun runDemo()
        fun openSubscriptionPage(preferredPlayOfferId: String?)
    }

    @Keep
    @Serializable
    class MessagePayload(val operation: String, val content: String, val minScriptVersion: Int)
    abstract class BaseJavascriptHandler {
        abstract val operation: String
        abstract fun executeWithContent(content: String, handler: MessageHandler)
    }

    @Keep
    @Serializable
    class RunScriptContext(val scriptContent: String, val scriptType: String, val scriptName: String? = null, val scriptLocation: String? = null)
    @Keep
    @Serializable
    class ShareURLContext(val title: String, val url: String)
    @Keep
    @Serializable
    class SendACKContext(val id: String)
    @Keep
    @Serializable
    class OpenAddonNextContext(val id: String)
    @Keep
    @Serializable
    class RunDemoContext
    @Keep
    @Serializable
    class OpenSubscriptionPageContext(val preferredPlayOfferId: String? = null)

    abstract class JavascriptHandler<T>(private val serializer: KSerializer<T>): BaseJavascriptHandler() {
        abstract fun execute(context: T, handler: MessageHandler)

        override fun executeWithContent(content: String, handler: MessageHandler) {
            try {
                val obj = json.decodeFromString(serializer, content)
                execute(obj, handler)
            } catch(ignored: Throwable) {}
        }
    }

    class RunScriptHandler: JavascriptHandler<RunScriptContext>(RunScriptContext.serializer()) {
        override val operation: String
            get() = "runScript"

        override fun execute(context: RunScriptContext, handler: MessageHandler) {
            handler.runScript(context.scriptType, context.scriptContent, context.scriptName, context.scriptLocation)
        }
    }

    class ShareURLHandler: JavascriptHandler<ShareURLContext>(ShareURLContext.serializer()) {
        override val operation: String
            get() = "shareURL"

        override fun execute(context: ShareURLContext, handler: MessageHandler) {
            handler.shareURL(context.title, context.url)
        }
    }

    class SendACKHandler: JavascriptHandler<SendACKContext>(SendACKContext.serializer()) {
        override val operation: String
            get() = "sendACK"

        override fun execute(context: SendACKContext, handler: MessageHandler) {
            handler.receivedACK(context.id)
        }
    }

    class OpenAddonNextHandler: JavascriptHandler<OpenAddonNextContext>(OpenAddonNextContext.serializer()) {
        override val operation: String
            get() = "openAddonNext"

        override fun execute(context: OpenAddonNextContext, handler: MessageHandler) {
            handler.openAddonNext(context.id)
        }
    }

    class RunDemoHandler: JavascriptHandler<RunDemoContext>(RunDemoContext.serializer()) {
        override val operation: String
            get() = "runDemo"

        override fun execute(context: RunDemoContext, handler: MessageHandler) {
            handler.runDemo()
        }
    }

    class OpenSubscriptionPageHandler: JavascriptHandler<OpenSubscriptionPageContext>(OpenSubscriptionPageContext.serializer()) {
        override val operation: String
            get() = "openSubscriptionPage"

        override fun execute(context: OpenSubscriptionPageContext, handler: MessageHandler) {
            handler.openSubscriptionPage(preferredPlayOfferId = context.preferredPlayOfferId)
        }
    }

    private val handler = WeakReference(handler)

    @JavascriptInterface
    fun sendMessage(message: String) {
        val messageHandler = handler.get() ?: return
        try {
            val payload = json.decodeFromString<MessagePayload>(message)
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
        val json = Json { ignoreUnknownKeys = true }

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
