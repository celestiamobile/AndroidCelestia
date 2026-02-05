package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import space.celestia.mobilecelestia.R

@Composable
fun Stepper(touchDown: (Boolean) -> Unit, touchUp: (Boolean) -> Unit) {
    val startInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource = remember { MutableInteractionSource() }
    val isStartPressed = startInteractionSource.collectIsPressedAsState()
    val isEndPressed = endInteractionSource.collectIsPressedAsState()

    LaunchedEffect(isStartPressed.value) {
        if (isStartPressed.value) {
            touchDown(true)
        } else {
            touchUp(true)
        }
    }

    LaunchedEffect(isEndPressed.value) {
        if (isEndPressed.value) {
            touchDown(false)
        } else {
            touchUp(false)
        }
    }

    Row(
        modifier = Modifier
            .width(dimensionResource(id = R.dimen.stepper_width))
            .height(dimensionResource(id = R.dimen.stepper_height))
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.stepper_corner_radius)))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    interactionSource = startInteractionSource,
                    indication = ripple(),
                    onClick = {}
                )
                .padding(dimensionResource(R.dimen.stepper_button_padding)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_remove),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        VerticalDivider(
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.stepper_separator_width))
                .padding(vertical = dimensionResource(id = R.dimen.stepper_separator_top_margin))
                .background(MaterialTheme.colorScheme.outline)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    interactionSource = endInteractionSource,
                    indication = ripple(),
                    onClick = {}
                )
                .padding(dimensionResource(R.dimen.stepper_button_padding)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
