package com.snapfilters.feature.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapfilters.core.designsystem.theme.SnapTheme

data class GalleryItem(
    val id: String,
    val title: String,
    val filterName: String?,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    items: List<GalleryItem> = emptyList(),
    onPhotoClick: (String) -> Unit = {},
    onCameraClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cinematic Gallery",
                        color = SnapTheme.colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SnapTheme.colors.surfaceBase
                ),
                actions = {
                    IconButton(onClick = onCameraClick) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Open Camera",
                            tint = SnapTheme.colors.accent
                        )
                    }
                }
            )
        },
        containerColor = SnapTheme.colors.surfaceBase
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No captures yet",
                        color = SnapTheme.colors.textSecondary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Take cinematic photos with real-time LUT filters",
                        color = SnapTheme.colors.textSecondary.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SnapTheme.colors.surfaceRaised)
                            .clickable { onPhotoClick(item.id) },
                        contentAlignment = Alignment.BottomStart
                    ) {
                        item.filterName?.let { filter ->
                            Text(
                                text = filter,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
