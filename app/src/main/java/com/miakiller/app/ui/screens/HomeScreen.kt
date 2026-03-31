package com.miakiller.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miakiller.app.ui.theme.*
import com.miakiller.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAdSwitch: () -> Unit,
    onNavigateToFreeze: () -> Unit,
    onNavigateToAutoStart: () -> Unit,
    onNavigateToPermission: () -> Unit,
    onNavigateToHosts: () -> Unit
) {
    val isShizukuAvailable by viewModel.isShizukuAvailable.collectAsState()
    val isShizukuGranted by viewModel.isShizukuGranted.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val message by viewModel.message.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === 顶部状态卡片 ===
        StatusCard(isShizukuAvailable, isShizukuGranted) {
            viewModel.requestShizukuPermission()
        }

        // === 一键操作 ===
        if (isShizukuGranted) {
            OneClickCard(isLoading) {
                viewModel.oneClickDisableAds()
            }
        }

        // === 统计卡片 ===
        if (isShizukuGranted) {
            StatsRow(stats)
        }

        // === 功能入口 ===
        Text(
            "功能",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        FeatureCard(
            icon = Icons.Default.Block,
            title = "广告开关管理",
            subtitle = "查看并关闭MIUI系统各处广告开关",
            color = AdRed,
            onClick = onNavigateToAdSwitch
        )

        FeatureCard(
            icon = Icons.Default.AcUnit,
            title = "冻结管理",
            subtitle = "冻结/解冻不需要的应用，释放资源",
            color = FreezeBlue,
            onClick = onNavigateToFreeze
        )

        FeatureCard(
            icon = Icons.Default.PlayArrow,
            title = "自启动管理",
            subtitle = "阻止应用在开机时自动启动",
            color = AutoStartPurple,
            onClick = onNavigateToAutoStart
        )

        FeatureCard(
            icon = Icons.Default.Security,
            title = "权限管理",
            subtitle = "查看和管理应用的敏感权限",
            color = PermissionOrange,
            onClick = onNavigateToPermission
        )

        FeatureCard(
            icon = Icons.Default.Dns,
            title = "Hosts广告屏蔽",
            subtitle = "通过Hosts文件屏蔽广告域名",
            color = HostsGreen,
            onClick = onNavigateToHosts
        )

        // === 提示消息 ===
        message?.let { msg ->
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    TextButton(onClick = { viewModel.clearMessage() }) {
                        Text("关闭")
                    }
                }
            ) {
                Text(msg)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatusCard(
    isAvailable: Boolean,
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Success else if (isAvailable) Warning else Danger
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle
                                  else Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = when {
                            isGranted -> "Shizuku 已连接"
                            isAvailable -> "Shizuku 未授权"
                            else -> "Shizuku 未运行"
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            isGranted -> "所有功能可用"
                            isAvailable -> "点击下方按钮授予权限"
                            else -> "请先安装并启动 Shizuku"
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            if (isAvailable && !isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Warning
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("授予 Shizuku 权限")
                }
            }

            if (!isAvailable) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "使用说明:\n" +
                            "1. 从应用商店安装 Shizuku\n" +
                            "2. 打开 Shizuku，通过无线调试启动\n" +
                            "3. 或连接电脑执行: adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun OneClickCard(isLoading: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE53935),
                            Color(0xFFD32F2F),
                            Color(0xFFC62828)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "一键关闭所有广告",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "关闭MIUI/HyperOS系统中所有已知的广告开关",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Danger
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Block, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isLoading) "正在关闭..." else "立即执行",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(stats: MainViewModel.DashboardStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            modifier = Modifier.weight(1f),
            label = "广告已关闭",
            value = "${stats.disabledAdSwitches}/${stats.totalAdSwitches}",
            color = AdRed
        )
        StatItem(
            modifier = Modifier.weight(1f),
            label = "已冻结应用",
            value = "${stats.frozenAppsCount}",
            color = FreezeBlue
        )
        StatItem(
            modifier = Modifier.weight(1f),
            label = "屏蔽域名",
            value = "${stats.blockedHostsCount}",
            color = HostsGreen
        )
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
