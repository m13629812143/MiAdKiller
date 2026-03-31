package com.miakiller.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 安全的应用图标 Composable
 * 如果 Bitmap 为 null 或渲染失败，显示默认 Android 图标
 */
@Composable
fun SafeAppIcon(
    bitmap: Bitmap?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    if (bitmap != null && !bitmap.isRecycled) {
        try {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } catch (e: Exception) {
            DefaultAppIcon(modifier = modifier, size = size)
        }
    } else {
        DefaultAppIcon(modifier = modifier, size = size)
    }
}

@Composable
private fun DefaultAppIcon(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Android,
            contentDescription = null,
            modifier = Modifier.size(size * 0.8f)
        )
    }
}
