package space.celestia.celestiaui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import space.celestia.celestiaui.R

@Composable
fun groupedListContentPadding(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    additionalBottomPadding: Dp = 0.dp,
): PaddingValues {
    val direction = LocalLayoutDirection.current
    val edgePadding = dimensionResource(R.dimen.list_content_padding_vertical)
    return PaddingValues(
        start = paddingValues.calculateStartPadding(direction),
        top = paddingValues.calculateTopPadding() + edgePadding,
        end = paddingValues.calculateEndPadding(direction),
        bottom = paddingValues.calculateBottomPadding() + edgePadding + additionalBottomPadding,
    )
}

@Composable
fun GroupedListItem(
    index: Int = 0,
    count: Int = 1,
    modifier: Modifier = Modifier,
    horizontalInset: Dp = dimensionResource(R.dimen.list_item_medium_margin_horizontal),
    bottomSpacing: Dp = if (index < count - 1) ListItemDefaults.SegmentedGap else 0.dp,
    content: @Composable () -> Unit,
) {
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset)
            .padding(bottom = bottomSpacing),
        contentPadding = PaddingValues(0.dp),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

fun LazyListScope.groupedItem(
    index: Int = 0,
    count: Int = 1,
    key: Any? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    item(key = key) {
        val itemScope = this
        GroupedListItem(index, count) {
            itemScope.content()
        }
    }
}

fun <T> LazyListScope.groupedItems(
    items: List<T>,
    key: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    itemsIndexed(
        items = items,
        key = if (key == null) null else { _, item -> key(item) },
    ) { index, item ->
        val itemScope = this
        GroupedListItem(index, items.size) {
            itemScope.itemContent(item)
        }
    }
}
