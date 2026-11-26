package space.celestia.celestiaui.resource

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CelestiaJavascriptInterfaceTest {
    @Test
    fun acceptsLegacyACKWithoutPageMetadata() {
        val handler = RecordingHandler()
        CelestiaJavascriptInterface.SendACKHandler().executeWithContent("""{"id":"guide"}""", handler)
        val context = requireNotNull(handler.ack)
        assertEquals("guide", context.id)
        assertNull(context.title)
        assertNull(context.subtitle)
        assertNull(context.shareURL)
    }

    @Test
    fun forwardsGuideTitleSubtitleAndShareURL() {
        val handler = RecordingHandler()
        CelestiaJavascriptInterface.SendACKHandler().executeWithContent(
            """{"id":"guide","title":"Title \"quoted\"","subtitle":"Subtitle <b>text</b>","shareURL":"https://celestia.mobi/resources/guide/guide?lang=fr"}""",
            handler
        )
        val context = requireNotNull(handler.ack)
        assertEquals("guide", context.id)
        assertEquals("Title \"quoted\"", context.title)
        assertEquals("Subtitle <b>text</b>", context.subtitle)
        assertEquals("https://celestia.mobi/resources/guide/guide?lang=fr", context.shareURL)
    }

    @Test
    fun preservesEmptySubtitleAndDisabledSharing() {
        val handler = RecordingHandler()
        CelestiaJavascriptInterface.SendACKHandler().executeWithContent(
            """{"id":"guide","title":"Title","subtitle":"","shareURL":null}""",
            handler
        )
        val context = requireNotNull(handler.ack)
        assertEquals("", context.subtitle)
        assertNull(context.shareURL)
    }

    private class RecordingHandler: CelestiaJavascriptInterface.MessageHandler {
        var ack: CelestiaJavascriptInterface.SendACKContext? = null

        override fun receivedACK(context: CelestiaJavascriptInterface.SendACKContext) {
            ack = context
        }

        override fun runScript(type: String, content: String, scriptName: String?, scriptLocation: String?) = Unit
        override fun shareURL(title: String, url: String) = Unit
        override fun openAddonNext(id: String) = Unit
        override fun runDemo() = Unit
        override fun openSubscriptionPage(preferredPlayOfferId: String?) = Unit
    }
}
