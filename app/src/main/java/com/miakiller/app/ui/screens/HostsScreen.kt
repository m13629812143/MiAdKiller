package com.miakiller.app.ui.screens

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
import com.miakiller.app.model.HostsRule
import com.miakiller.app.ui.theme.HostsGreen
import com.miakiller.app.ui.theme.Success
import com.miakiller.app.ui.theme.Warning
import com.miakiller.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val hostsRules by viewModel.hostsRules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationResult by viewModel.adOperationResult.collectAsState()
    var showResult by remember { mutableStateOf(false) }

    val grouped = hostsRules.groupBy { it.category }
    val enabledCount = hostsRules.count { it.isEnabled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hosts广告屏蔽") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 恢复按钮
                    IconButton(onClick = { viewModel.restoreHosts() }) {
                        Icon(Icons.Default.Restore, "恢复")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.applyHostsRules()
                    showResult = true
                },
                containerColor = HostsGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("应用规则 ($enabledCount)")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 说明卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = HostsGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Dns, null, tint = HostsGreen)
                            Text(
                                "Hosts域名屏蔽",
                                fontWeight = FontWeight.Bold,
                                color = HostsGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "通过将广告域名指向本机(127.0.0.1)来屏蔽广告请求。\n\n" +
                            "注意: 非Root设备可能无法直接修改系统hosts文件。" +
                            "如果应用失败，规则会保存到 /sdcard/Download/ 供其他工具导入。",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = HostsGreen.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // 统计
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "共 ${hostsRules.size} 条规则，已启用 $enabledCount 条",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Row {
                        TextButton(onClick = {
                            hostsRules.forEach { viewModel.toggleHostsRule(it.copy(isEnabled = false)) }
                            viewModel.loadHostsRules()
                        }) {
                            Text("全选", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 按分类显示
            grouped.forEach { (category, rules) ->
                item {
                    Text(
                        category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HostsGreen,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(rules, key = { it.domain }) { rule ->
                    HostsRuleItem(rule = rule) {
                        viewModel.toggleHostsRule(rule)
                    }
                }
            }

            // 操作结果
            if (showResult && operationResult != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (operationResult!!.success) Success.copy(alpha = 0.1f)
                                             else Warning.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                operationResult!!.message,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (operationResult!!.details.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                operationResult!!.details.forEach { detail ->
                                    Text(
                                        detail,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 底部间距 (FAB)
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun HostsRuleItem(
    rule: HostsRule,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    rule.domain,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    rule.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Checkbox(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = HostsGreen
                )
            )
        }
    }
}
