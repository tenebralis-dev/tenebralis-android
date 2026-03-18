package com.tenebralis.dreamos.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Telegram-style floating dock tab item.
 *
 * @param label       Tab label text (e.g. "对话")
 * @param selectedIcon    Filled icon displayed when selected
 * @param unselectedIcon  Outlined icon displayed when deselected
 * @param badgeCount  Optional unread badge count (reserved for future use)
 */
data class DockTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

/**
 * A Telegram-style floating dock navigation bar.
 *
 * Renders as a single rounded-rectangle surface floating above the bottom edge.
 * Selected tab shows a pill-shaped highlight behind the icon and blue tint.
 *
 * @param selectedIndex Currently selected tab index
 * @param onTabSelected Called with the new index when user taps a tab
 * @param tabs          List of [DockTab] items to display
 * @param modifier      Modifier applied to the outermost container
 */
@Composable
fun TenebralisDock(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<DockTab>,
    modifier: Modifier = Modifier
) {
    // Outer padding creates the "floating" look — space from edges & bottom
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    DockItem(
                        tab = tab,
                        isSelected = selectedIndex == index,
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
    }
}

/**
 * A single item inside the dock.
 *
 * When selected: icon sits inside a pill-shaped primaryContainer background,
 * both icon and label turn primary-colored.
 * When deselected: outlined icon and muted text.
 */
@Composable
private fun RowScope.DockItem(
    tab: DockTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val tintColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "dock_tint"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "dock_label"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null  // Telegram-style: no ripple on dock items
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with optional pill-shaped background
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .then(
                    if (isSelected) {
                        Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // Label
        Text(
            text = tab.label,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
