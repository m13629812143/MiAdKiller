package com.miakiller.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.miakiller.app.model.AppInfo
import com.miakiller.app.ui.theme.FreezeBlue
import com.miakiller.app.ui.theme.Success
import com.miakiller.app.ui.theme.Warning
import com.miakiller.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreezeScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val appList by viewModel.appList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showSystemApps by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(showSystemApps) {
        viewModel.loadApps(includeSystem = showSystemApps)
    }

    val filteredApps = appList.filter {
        searchQuery.isBlank() ||
        it.appName.contains(searchQuery, ignoreCase = true) ||
        it.packageName.contains(searchQuery, ignoreCase = true)
    }

    val frozenApps = filteredApps.filter { it.isFrozen }
    val activeApps = filteredApps.filter { !it.isFrozen }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("冻结管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 智能冻结按钮
                    Button(
                        onClick = { viewModel.freezeSuggestedApps() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = FreezeBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("智能冻结", fontSize = 12.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索应用名或包名...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 过滤选项
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showSystemApps,
                        onCheckedChange = { showSystemApps = it }
                    )
                    Text("显示系统应用", fontSize = 13.sp)
                }
                Text(
                    "${filteredApps.size} 个应用",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Tab切换
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("全部 (${activeApps.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("已冻结 (${frozenApps.size})") }
                )
            }

            // 应用列表
            if (isLoading && appList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val displayList = if (selectedTab == 0) activeApps else frozenApps

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(displayList, key = { it.packageName }) { app ->
                        AppFreezeItem(
                            app = app,
                            onFreeze = { viewModel.freezeApp(app.packageName) },
                            onUnfreeze = { viewModel.unfreezeApp(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppFreezeItem(
    app: AppInfo,
    onFreeze: () -> Unit,
    onUnfreeze: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (app.isFrozen) 0.7f else 1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 应用图标
            app.icon?.let { drawable ->
                Image(
                    bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } ?: Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Android, null, modifier = Modifier.size(32.dp))
            }

            // 应用信息
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        app.appName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    if (app.isSystemApp) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Warning.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "系统",
                                fontSize = 9.sp,
                                color = Warning,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (app.isFrozen) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = FreezeBlue.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "已冻结",
                                fontSize = 9.sp,
                                color = FreezeBlue,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    app.packageName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    maxLines = 1
                )
            }

            // 冻结/解冻按钮
            if (app.isFrozen) {
                FilledTonalButton(
                    onClick = onUnfreeze,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Success.copy(alpha = 0.2f),
                        contentColor = Success
                    )
                ) {
                    Text("解冻", fontSize = 12.sp)
                }
            } else {
                FilledTonalButton(
                    onClick = onFreeze,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = FreezeBlue.copy(alpha = 0.2f),
                        contentColor = FreezeBlue
                    )
                ) {
                    Text("冻结", fontSize = 12.sp)
                }
            }
        }
    }
}
