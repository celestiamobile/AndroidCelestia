package space.celestia.celestiaui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.IconButtonWidthOption
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import space.celestia.celestiaui.R

@Composable
fun Stepper(
    minusLabel: String,
    plusLabel: String,
    touchDown: (Boolean) -> Unit,
    touchUp: (Boolean) -> Unit,
    onClick: (Boolean) -> Unit,
) {
    val minimumButtonWidth = LocalViewConfiguration.current.minimumTouchTargetSize.width
    val buttonWidth = maxOf(
        IconButtonDefaults.smallContainerSize(IconButtonWidthOption.Wide).width,
        minimumButtonWidth,
    )
    val gap = ButtonGroupDefaults.ConnectedSpaceBetween
    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(
                menuState = menuState,
                colors = IconButtonDefaults.filledTonalIconButtonColors(),
            )
        },
        modifier = Modifier.width(buttonWidth * 2 + gap),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stepperButton(
            label = minusLabel,
            icon = R.drawable.ic_remove,
            leading = true,
            compressionLimit = buttonWidth - minimumButtonWidth,
            onPress = { touchDown(true) },
            onRelease = { touchUp(true) },
            onClick = { onClick(true) },
        )
        stepperButton(
            label = plusLabel,
            icon = R.drawable.ic_add,
            leading = false,
            compressionLimit = buttonWidth - minimumButtonWidth,
            onPress = { touchDown(false) },
            onRelease = { touchUp(false) },
            onClick = { onClick(false) },
        )
    }
}

private fun ButtonGroupScope.stepperButton(
    label: String,
    @DrawableRes icon: Int,
    leading: Boolean,
    compressionLimit: Dp,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onClick: () -> Unit,
) {
    customItem(
        buttonGroupContent = {
            HoldButton(onPress = onPress, onRelease = onRelease, onActivate = onClick) { modifier, interactionSource ->
                FilledTonalIconButton(
                    onClick = {},
                    shapes = IconButtonDefaults.shapes(
                        shape = if (leading) ButtonGroupDefaults.connectedLeadingButtonShape else ButtonGroupDefaults.connectedTrailingButtonShape,
                        pressedShape = if (leading) ButtonGroupDefaults.connectedLeadingButtonPressShape else ButtonGroupDefaults.connectedTrailingButtonPressShape,
                    ),
                    modifier = modifier
                        .weight(1f)
                        .animateWidth(interactionSource, compressionLimit),
                    interactionSource = interactionSource,
                ) {
                    Icon(painter = painterResource(icon), contentDescription = label)
                }
            }
        },
        menuContent = { menuState ->
            DropdownMenuItem(
                text = { Text(label) },
                leadingIcon = { Icon(painter = painterResource(icon), contentDescription = null) },
                onClick = {
                    onClick()
                    menuState.dismiss()
                },
            )
        },
    )
}
