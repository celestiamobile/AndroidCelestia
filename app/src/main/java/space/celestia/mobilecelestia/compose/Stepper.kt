package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import space.celestia.mobilecelestia.R

@Composable
fun Stepper(touchDown: (Boolean) -> Unit, touchUp: (Boolean) -> Unit) {
    Row(modifier = Modifier.size(width = dimensionResource(R.dimen.stepper_width), height = dimensionResource(R.dimen.stepper_height)).background(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(size = dimensionResource(R.dimen.stepper_corner_radius)))) {
        StepperButton(icon = painterResource(R.drawable.ic_remove), contentDescription = "", touchUp = {
            touchUp(true)
        }, touchDown = {
            touchDown(true)
        }, modifier = Modifier.weight(1.0f).fillMaxHeight())

        Box(modifier = Modifier.fillMaxHeight().width(dimensionResource(R.dimen.stepper_separator_width)).padding(vertical = dimensionResource(R.dimen.stepper_separator_top_margin))) {
            Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.outline))
        }

        StepperButton(icon = painterResource(R.drawable.ic_add), contentDescription = "", touchUp = {
            touchUp(false)
        }, touchDown = {
            touchDown(false)
        }, modifier = Modifier.weight(1.0f).fillMaxHeight())
    }
}

@Composable
private fun StepperButton(icon: Painter, contentDescription: String, touchDown: () -> Unit, touchUp: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = modifier
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(Unit) {
            detectTapGestures(onPress = { offset ->
                touchDown()
                val press = PressInteraction.Press(offset)
                interactionSource.emit(press)
                tryAwaitRelease()
                touchUp()
                interactionSource.emit(PressInteraction.Release(press))
            })
        }
        .padding(dimensionResource(R.dimen.stepper_button_padding)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}