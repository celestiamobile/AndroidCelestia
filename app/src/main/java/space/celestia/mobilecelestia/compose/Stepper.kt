package space.celestia.mobilecelestia.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import space.celestia.mobilecelestia.common.StepperView

@Composable
fun Stepper(touchDown: (Boolean) -> Unit, touchUp: (Boolean) -> Unit) {
    AndroidView(factory = { context ->
        val stepperView = StepperView(context)
        stepperView.listener = object: StepperView.Listener {
            override fun stepperTouchDown(view: StepperView, left: Boolean) {
                touchDown(left)
            }

            override fun stepperTouchUp(view: StepperView, left: Boolean) {
                touchUp(left)
            }
        }
        stepperView
    })
}