package com.miakiller.app.model

import android.graphics.drawable.Drawable

/**
 * MIUI 广告开关项
 */
data class MiuiAdSwitch(
    val name: String,              // 显示名称
    val description: String,       // 描述
    val settingsKey: String,       // settings key
    val settingsTable: String,     // secure / system / global
    val disableValue: String = "0", // 关闭广告的值
    val enableValue: String = "1",  // 开启广告的值 (默认状态)
    val category: String,          // 分类
    var isDisabled: Boolean = false // 当前是否已关闭
)

/**
 * 应用信息
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val isFrozen: Boolean = false,
    val isEnabled: Boolean = true,
    val versionName: String = "",
    val uid: Int = 0
)

/**
 * 自启动项
 */
data class AutoStartItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val receivers: List<String>,  // 开机广播接收器
    val isBlocked: Boolean = false
)

/**
 * 权限信息
 */
data class PermissionInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val permissions: List<AppPermission>
)

data class AppPermission(
    val name: String,
    val displayName: String,
    val isGranted: Boolean,
    val isDangerous: Boolean
)

/**
 * Hosts 规则
 */
data class HostsRule(
    val domain: String,
    val description: String,
    val category: String,
    var isEnabled: Boolean = true
)

/**
 * 操作结果
 */
data class OperationResult(
    val success: Boolean,
    val message: String,
    val details: List<String> = emptyList()
)
