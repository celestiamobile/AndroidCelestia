package space.celestia.mobilecelestia

import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import space.celestia.mobilecelestia.celestia.shouldRenderForLifecycle
import space.celestia.mobilecelestia.celestia.renderSurfaceSize

class PictureInPictureLifecycleTest {
    @Test
    fun pipResizeKeepsOriginalRenderBuffer() {
        val originalSize = IntSize(1080, 2400)
        assertEquals(originalSize, renderSurfaceSize(IntSize(270, 600), originalSize, 1f))
        assertEquals(originalSize, renderSurfaceSize(IntSize(180, 400), originalSize, 1f))
        assertEquals(originalSize, renderSurfaceSize(IntSize(180, 400), originalSize, 1f / 3f))
    }

    @Test
    fun pipDoesNotApplyResolutionScalingTwice() {
        val originalSize = renderSurfaceSize(IntSize(1080, 2400), null, 1f / 3f)
        assertEquals(IntSize(360, 800), originalSize)
        assertEquals(originalSize, renderSurfaceSize(IntSize(180, 400), originalSize, 1f / 3f))
    }

    @Test
    fun leavingPipRestoresLayoutDrivenRendering() {
        assertEquals(IntSize(2400, 1080), renderSurfaceSize(IntSize(2400, 1080), null, 1f))
    }

    @Test
    fun automaticEntryKeepsRenderingBeforePipModeCallback() {
        assertTrue(shouldRenderForLifecycle(Lifecycle.State.RESUMED, true))
        assertTrue(shouldRenderForLifecycle(Lifecycle.State.STARTED, true))
    }

    @Test
    fun closingPipOrBackgroundingWithoutEntryPausesRendering() {
        assertFalse(shouldRenderForLifecycle(Lifecycle.State.CREATED, true))
        assertFalse(shouldRenderForLifecycle(Lifecycle.State.DESTROYED, true))
    }

    @Test
    fun disabledPipPreservesPauseAndResumeBehavior() {
        assertTrue(shouldRenderForLifecycle(Lifecycle.State.RESUMED, false))
        assertFalse(shouldRenderForLifecycle(Lifecycle.State.STARTED, false))
        assertFalse(shouldRenderForLifecycle(Lifecycle.State.CREATED, false))
    }
}
