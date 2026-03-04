package space.celestia.celestiaxr.common

import space.celestia.celestia.XRRenderer
import java.util.concurrent.Executor

class CelestiaExecutor(private val renderer: XRRenderer): Executor {
    override fun execute(command: Runnable?) {
        if (command == null) return
        renderer.enqueueTask {
            command.run()
        }
    }
}
