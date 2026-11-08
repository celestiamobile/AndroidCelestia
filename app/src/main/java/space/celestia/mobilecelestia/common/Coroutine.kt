package space.celestia.mobilecelestia.common

import android.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import space.celestia.celestia.Renderer
import java.util.concurrent.Executor
import kotlin.coroutines.resume

class CelestiaExecutor(private val renderer: Renderer): Executor {
    override fun execute(command: Runnable?) {
        if (command == null) return
        renderer.enqueueTask {
            command.run()
        }
    }
}

suspend fun Renderer.saveScreenshotAsync(path: String, imageType: Int): Boolean =
    suspendCancellableCoroutine { continuation ->
        val cancellation = CancellationSignal()
        continuation.invokeOnCancellation { cancellation.cancel() }
        saveScreenshot(path, imageType, cancellation) { success ->
            continuation.resume(success)
        }
    }