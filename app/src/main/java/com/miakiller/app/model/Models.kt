package com.miakiller.app.model

import android.graphics.Bitmap

/**
 * MIUI/澎湃OS 广告开关项
 */
data class MiuiAdSwitch(
    val name: String,
    val description: String,
    val settingsKey: String,
    val settingsTable: String,
    val disableValue: String = "0",
    val enableValue: String = "1",
    val category: String,
    val isDisabled: Boolean = false
)

/**
 * 应用信息
 * 注意: icon 使用 Bitmap 而非 Drawable，避免 Compose 渲染崩溃
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isFrozen: Boolean = false,
    val isEnabled: Boolean = true,
    val versionName: String = ""
) {
    // icon 不放入 data class 的构造函数中，避免 equals/hashCode 问题
    @Transient
    var iconBitmap: Bitmap? = null
}

/**
 * 自启动项
 */
data class AutoStartItem(
    val packageName: String,
    val appName: String,
    val receivers: List<String>,
    val isBlocked: Boolean = false
) {
    @Transient
    var iconBitmap: Bitmap? = null
}

/**
 * 权限信息
 */
data class PermissionInfo(
    val packageName: String,
    val appName: String,
    val permissions: List<AppPermission>
) {
    @Transient
    var iconBitmap: Bitmap? = null
}

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
    val isEnabled: Boolean = true
)

/**
 * 操作结果
 */
data class OperationResult(
    val success: Boolean,
    val message: String,
    val details: List<String> = emptyList()
)
