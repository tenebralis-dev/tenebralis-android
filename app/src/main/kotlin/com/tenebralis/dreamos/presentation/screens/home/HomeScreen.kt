package com.tenebralis.dreamos.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onRouteNavigate: (String) -> Unit
) {
    val appPages = remember { dreamOsHomePages() }
    val dockItems = remember { dreamOsDockItems() }
    val pagerState = rememberPagerState(pageCount = { appPages.size })
    val homeDateTime = rememberHomeDateTimeUi()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    HomeFirstPage(
                        apps = appPages[page],
                        dateTime = homeDateTime,
                        onAppClick = { item -> onRouteNavigate(item.route) }
                    )
                } else {
                    HomeAppGrid(
                        apps = appPages[page],
                        onAppClick = { item -> onRouteNavigate(item.route) }
                    )
                }
            }

            PageIndicator(
                pageCount = appPages.size,
                currentPage = pagerState.currentPage
            )

            HomeDock(
                dockItems = dockItems,
                onDockClick = { item -> onRouteNavigate(item.route) }
            )
        }
    }
}

@Composable
private fun HomeFirstPage(
    apps: List<HomeAppItem>,
    dateTime: HomeDateTimeUi,
    onAppClick: (HomeAppItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HomeDateTimeCard(
            timeText = dateTime.timeText,
            dateText = dateTime.dateText,
            weekText = dateTime.weekText
        )
        HomeAppGrid(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            apps = apps,
            onAppClick = onAppClick
        )
    }
}

@Composable
private fun HomeDateTimeCard(
    timeText: String,
    dateText: String,
    weekText: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = weekText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun rememberHomeDateTimeUi(): HomeDateTimeUi {
    val locale = remember { Locale.getDefault() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", locale) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy/MM/dd", locale) }
    val weekFormatter = remember { DateTimeFormatter.ofPattern("EEEE", locale) }
    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalDateTime.now()
            dateTime = now
            val msToNextMinute = ((60 - now.second) * 1_000L) - (now.nano / 1_000_000L)
            delay(msToNextMinute.coerceAtLeast(250L))
        }
    }

    return HomeDateTimeUi(
        timeText = dateTime.format(timeFormatter),
        dateText = dateTime.format(dateFormatter),
        weekText = dateTime.format(weekFormatter)
    )
}

private data class HomeDateTimeUi(
    val timeText: String,
    val dateText: String,
    val weekText: String
)

@Composable
private fun HomeAppGrid(
    modifier: Modifier = Modifier,
    apps: List<HomeAppItem>,
    onAppClick: (HomeAppItem) -> Unit
) {
    val rows = remember(apps) { apps.chunked(3) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        rows.forEach { rowItems ->
            val emptySlots = 3 - rowItems.size
            val leadingSlots = emptySlots / 2
            val trailingSlots = emptySlots - leadingSlots

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(leadingSlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
                rowItems.forEach { item ->
                    HomeAppIcon(
                        item = item,
                        onClick = { onAppClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(trailingSlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeAppIcon(
    item: HomeAppItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isActive) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun HomeDock(
    dockItems: List<HomeDockItem>,
    onDockClick: (HomeDockItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockItems.forEach { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDockClick(item) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
