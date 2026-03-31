package com.miakiller.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miakiller.app.util.AppLogger

@Composable
fun LogScreen(onBack: () -> Unit) {
    val logs by AppLogger.logs.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var filterLevel by remember { mutableStateOf<AppLogger.LogEntry.Level?>(null) }

    val filteredLogs = if (filterLevel != null) {
        logs.filter { it.level == filterLevel }
    } else {
        logs
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("运行日志") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
                },
                actions = {
                    // 复制日志
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(AppLogger.exportLogs()))
                    }) {
                        Icon(Icons.Default.ContentCopy, "复制")
                    }
                    // 清除日志
                    IconButton(onClick = { AppLogger.clear() }) {
                        Icon(Icons.Default.Delete, "清除")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 过滤器
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterLevel == null,
                    onClick = { filterLevel = null },
                    label = { Text("全部 (${logs.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = filterLevel == AppLogger.LogEntry.Level.ERROR,
                    onClick = {
                        filterLevel = if (filterLevel == AppLogger.LogEntry.Level.ERROR) null
                                      else AppLogger.LogEntry.Level.ERROR
                    },
                    label = { Text("错误 (${logs.count { it.level == AppLogger.LogEntry.Level.ERROR }})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE53935).copy(alpha = 0.2f)
                    )
                )
                FilterChip(
                    selected = filterLevel == AppLogger.LogEntry.Level.WARN,
                    onClick = {
                        filterLevel = if (filterLevel == AppLogger.LogEntry.Level.WARN) null
                                      else AppLogger.LogEntry.Level.WARN
                    },
                    label = { Text("警告 (${logs.count { it.level == AppLogger.LogEntry.Level.WARN }})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFA726).copy(alpha = 0.2f)
                    )
                )
                FilterChip(
                    selected = filterLevel == AppLogger.LogEntry.Level.INFO,
                    onClick = {
                        filterLevel = if (filterLevel == AppLogger.LogEntry.Level.INFO) null
                                      else AppLogger.LogEntry.Level.INFO
                    },
                    label = { Text("信息", fontSize = 11.sp) }
                )
            }

            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无日志", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredLogs) { entry ->
                        LogEntryItem(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(entry: AppLogger.LogEntry) {
    val bgColor = when (entry.level) {
        AppLogger.LogEntry.Level.ERROR -> Color(0xFFE53935).copy(alpha = 0.08f)
        AppLogger.LogEntry.Level.WARN -> Color(0xFFFFA726).copy(alpha = 0.08f)
        else -> Color.Transparent
    }
    val levelColor = when (entry.level) {
        AppLogger.LogEntry.Level.ERROR -> Color(0xFFE53935)
        AppLogger.LogEntry.Level.WARN -> Color(0xFFFFA726)
        AppLogger.LogEntry.Level.INFO -> Color(0xFF43A047)
        AppLogger.LogEntry.Level.DEBUG -> Color.Gray
    }
    val levelText = when (entry.level) {
        AppLogger.LogEntry.Level.ERROR -> "E"
        AppLogger.LogEntry.Level.WARN -> "W"
        AppLogger.LogEntry.Level.INFO -> "I"
        AppLogger.LogEntry.Level.DEBUG -> "D"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                levelText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = levelColor,
                fontFamily = FontFamily.Monospace
            )
            Text(
                entry.timeString,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontFamily = FontFamily.Monospace
            )
            Text(
                entry.tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            entry.message,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        )
        entry.throwable?.let { throwable ->
            Text(
                throwable,
                fontSize = 10.sp,
                color = Color(0xFFE53935),
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .horizontalScroll(rememberScrollState())
            )
        }
    }
}
