package com.miakiller.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miakiller.app.model.MiuiAdSwitch
import com.miakiller.app.ui.theme.AdRed
import com.miakiller.app.ui.theme.Success
import com.miakiller.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdSwitchScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val adSwitches by viewModel.adSwitches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationResult by viewModel.adOperationResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAdSwitches()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MIUI广告开关") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 一键关闭按钮
                    Button(
                        onClick = { viewModel.oneClickDisableAds() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdRed),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Block, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("一键关闭", fontSize = 12.sp)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && adSwitches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在读取广告开关状态...")
                }
            }
        } else {
            val grouped = adSwitches.groupBy { it.category }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 统计信息
                item {
                    val disabled = adSwitches.count { it.isDisabled }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (disabled == adSwitches.size) Success.copy(alpha = 0.1f)
                                             else AdRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                if (disabled == adSwitches.size) Icons.Default.CheckCircle
                                else Icons.Default.Warning,
                                null,
                                tint = if (disabled == adSwitches.size) Success else AdRed
                            )
                            Column {
                                Text(
                                    "已关闭 $disabled / ${adSwitches.size} 个广告开关",
                                    fontWeight = FontWeight.Bold
                                )
                                if (disabled < adSwitches.size) {
                                    Text(
                                        "还有 ${adSwitches.size - disabled} 个未关闭",
                                        fontSize = 12.sp,
                                        color = AdRed
                                    )
                                }
                            }
                        }
                    }
                }

                // 按分类显示
                grouped.forEach { (category, switches) ->
                    item {
                        Text(
                            category,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }

                    items(switches, key = { it.settingsKey }) { switch ->
                        AdSwitchItem(switch = switch) {
                            viewModel.toggleAdSwitch(switch)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdSwitchItem(
    switch: MiuiAdSwitch,
    onToggle: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (switch.isDisabled) Success.copy(alpha = 0.05f)
        else MaterialTheme.colorScheme.surface,
        label = "bgColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        switch.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    if (switch.isDisabled) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Success.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "已关闭",
                                fontSize = 10.sp,
                                color = Success,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    switch.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    lineHeight = 15.sp
                )
            }

            Switch(
                checked = switch.isDisabled, // checked = 已关闭广告 (好的状态)
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Success,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = AdRed.copy(alpha = 0.5f)
                )
            )
        }
    }
}
