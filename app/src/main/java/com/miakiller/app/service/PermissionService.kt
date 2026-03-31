package com.miakiller.app.service

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo as AndroidPermissionInfo
import com.miakiller.app.MiAdKillerApp
import com.miakiller.app.model.AppPermission
import com.miakiller.app.model.OperationResult
import com.miakiller.app.model.PermissionInfo
import com.miakiller.app.util.ShizukuHelper

/**
 * 权限管理服务
 *
 * 通过 Shizuku (shell权限) 管理应用权限。
 * 可以查看、授予、撤销应用的敏感权限。
 */
object PermissionService {

    private val context: Context get() = MiAdKillerApp.instance

    /** 危险权限的友好名称映射 */
    private val permissionDisplayNames = mapOf(
        "android.permission.CAMERA" to "相机",
        "android.permission.RECORD_AUDIO" to "麦克风",
        "android.permission.ACCESS_FINE_LOCATION" to "精确位置",
        "android.permission.ACCESS_COARSE_LOCATION" to "大致位置",
        "android.permission.ACCESS_BACKGROUND_LOCATION" to "后台位置",
        "android.permission.READ_CONTACTS" to "读取联系人",
        "android.permission.WRITE_CONTACTS" to "写入联系人",
        "android.permission.READ_CALL_LOG" to "读取通话记录",
        "android.permission.WRITE_CALL_LOG" to "写入通话记录",
        "android.permission.READ_PHONE_STATE" to "电话状态",
        "android.permission.CALL_PHONE" to "拨打电话",
        "android.permission.READ_SMS" to "读取短信",
        "android.permission.SEND_SMS" to "发送短信",
        "android.permission.READ_EXTERNAL_STORAGE" to "读取存储",
        "android.permission.WRITE_EXTERNAL_STORAGE" to "写入存储",
        "android.permission.READ_MEDIA_IMAGES" to "读取图片",
        "android.permission.READ_MEDIA_VIDEO" to "读取视频",
        "android.permission.READ_MEDIA_AUDIO" to "读取音频",
        "android.permission.READ_CALENDAR" to "读取日历",
        "android.permission.WRITE_CALENDAR" to "写入日历",
        "android.permission.BODY_SENSORS" to "身体传感器",
        "android.permission.ACTIVITY_RECOGNITION" to "运动识别",
        "android.permission.POST_NOTIFICATIONS" to "发送通知",
        "android.permission.NEARBY_WIFI_DEVICES" to "附近WIFI",
        "android.permission.BLUETOOTH_CONNECT" to "蓝牙连接",
        "android.permission.BLUETOOTH_SCAN" to "蓝牙扫描",
    )

    /**
     * 获取应用的权限信息
     */
    fun getAppPermissions(packageName: String): PermissionInfo? {
        val pm = context.packageManager
        return try {
            val pkgInfo = pm.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val requestedPermissions = pkgInfo.requestedPermissions ?: emptyArray()

            val permissions = requestedPermissions.mapNotNull { perm ->
                val isDangerous = try {
                    val permInfo = pm.getPermissionInfo(perm, 0)
                    permInfo.protection == AndroidPermissionInfo.PROTECTION_DANGEROUS
                } catch (e: Exception) {
                    false
                }

                if (!isDangerous) return@mapIndexedNotNull null

                val isGranted = pm.checkPermission(perm, packageName) == PackageManager.PERMISSION_GRANTED

                AppPermission(
                    name = perm,
                    displayName = permissionDisplayNames[perm] ?: perm.substringAfterLast("."),
                    isGranted = isGranted,
                    isDangerous = true
                )
            }

            PermissionInfo(
                packageName = packageName,
                appName = appInfo.loadLabel(pm).toString(),
                icon = try { appInfo.loadIcon(pm) } catch (e: Exception) { null },
                permissions = permissions
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取所有有危险权限的应用
     */
    fun getAppsWithDangerousPermissions(): List<PermissionInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        return packages.mapNotNull { pkg ->
            if (pkg.packageName == context.packageName) return@mapNotNull null
            getAppPermissions(pkg.packageName)
        }.filter { it.permissions.isNotEmpty() }
            .sortedByDescending { it.permissions.count { p -> p.isGranted } }
    }

    /**
     * 撤销应用权限
     */
    fun revokePermission(packageName: String, permission: String): OperationResult {
        val result = ShizukuHelper.executeCommand(
            "pm revoke $packageName $permission"
        )
        return if (result.success || result.error.isEmpty()) {
            OperationResult(true, "已撤销权限: ${permissionDisplayNames[permission] ?: permission}")
        } else {
            OperationResult(false, "撤销失败: ${result.error}")
        }
    }

    /**
     * 授予应用权限
     */
    fun grantPermission(packageName: String, permission: String): OperationResult {
        val result = ShizukuHelper.executeCommand(
            "pm grant $packageName $permission"
        )
        return if (result.success || result.error.isEmpty()) {
            OperationResult(true, "已授予权限: ${permissionDisplayNames[permission] ?: permission}")
        } else {
            OperationResult(false, "授予失败: ${result.error}")
        }
    }

    /**
     * 批量撤销应用的所有危险权限
     */
    fun revokeAllDangerousPermissions(packageName: String): OperationResult {
        val permInfo = getAppPermissions(packageName) ?: return OperationResult(false, "获取权限信息失败")

        var success = 0
        var fail = 0
        val details = mutableListOf<String>()

        permInfo.permissions.filter { it.isGranted }.forEach { perm ->
            val result = revokePermission(packageName, perm.name)
            if (result.success) {
                success++
                details.add("[成功] ${perm.displayName}")
            } else {
                fail++
                details.add("[失败] ${perm.displayName}: ${result.message}")
            }
        }

        return OperationResult(
            success = fail == 0,
            message = "撤销完成: 成功 $success, 失败 $fail",
            details = details
        )
    }
}
