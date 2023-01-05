package space.celestia.mobilecelestia.common

import space.celestia.celestia.Renderer
import java.util.concurrent.Executor

class CelestiaExecutor(private val renderer: Renderer): Executor {
    override fun execute(command: Runnable?) {
        if (command == null) return
        renderer.enqueueTask {
            command.run()
        }
    }
}