package com.miakiller.app.service

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo as AndroidPermissionInfo
import com.miakiller.app.MiAdKillerApp
import com.miakiller.app.model.AppPermission
import com.miakiller.app.model.OperationResult
import com.miakiller.app.model.PermissionInfo
import com.miakiller.app.util.AppLogger
import com.miakiller.app.util.IconHelper
import com.miakiller.app.util.ShizukuHelper

/**
 * 权限管理服务
 */
object PermissionService {

    private const val TAG = "PermissionService"
    private val context: Context get() = MiAdKillerApp.instance

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

    fun getAppPermissions(packageName: String): PermissionInfo? {
        val pm = context.packageManager
        return try {
            val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val requestedPermissions = pkgInfo.requestedPermissions ?: return null

            val permissions = requestedPermissions.mapNotNull { perm ->
                try {
                    val isDangerous = try {
                        val permInfo = pm.getPermissionInfo(perm, 0)
                        permInfo.protection == AndroidPermissionInfo.PROTECTION_DANGEROUS
                    } catch (e: Exception) {
                        false
                    }
                    if (!isDangerous) return@mapNotNull null

                    val isGranted = pm.checkPermission(perm, packageName) == PackageManager.PERMISSION_GRANTED

                    AppPermission(
                        name = perm,
                        displayName = permissionDisplayNames[perm] ?: perm.substringAfterLast("."),
                        isGranted = isGranted,
                        isDangerous = true
                    )
                } catch (e: Exception) {
                    null
                }
            }

            if (permissions.isEmpty()) return null

            val info = PermissionInfo(
                packageName = packageName,
                appName = try { appInfo.loadLabel(pm).toString() } catch (e: Exception) { packageName },
                permissions = permissions
            )
            info.iconBitmap = IconHelper.loadAppIcon(pm, packageName)
            info
        } catch (e: Exception) {
            AppLogger.w(TAG, "获取权限信息失败: $packageName", e)
            null
        }
    }

    fun getAppsWithDangerousPermissions(): List<PermissionInfo> {
        AppLogger.i(TAG, "开始扫描应用权限")
        val pm = context.packageManager
        return try {
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            AppLogger.i(TAG, "共 ${packages.size} 个包待扫描")

            packages.mapNotNull { pkg ->
                if (pkg.packageName == context.packageName) return@mapNotNull null
                try {
                    getAppPermissions(pkg.packageName)
                } catch (e: Exception) {
                    AppLogger.w(TAG, "扫描权限异常: ${pkg.packageName}", e)
                    null
                }
            }.sortedByDescending {
                it.permissions.count { p -> p.isGranted }
            }.also {
                AppLogger.i(TAG, "权限扫描完成, ${it.size} 个应用有危险权限")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "权限扫描失败", e)
            emptyList()
        }
    }

    fun revokePermission(packageName: String, permission: String): OperationResult {
        AppLogger.i(TAG, "撤销权限: $packageName / $permission")
        val result = ShizukuHelper.executeCommand("pm revoke $packageName $permission")
        return if (result.success || result.error.isEmpty()) {
            OperationResult(true, "已撤销: ${permissionDisplayNames[permission] ?: permission}")
        } else {
            OperationResult(false, "撤销失败: ${result.error}")
        }
    }

    fun grantPermission(packageName: String, permission: String): OperationResult {
        AppLogger.i(TAG, "授予权限: $packageName / $permission")
        val result = ShizukuHelper.executeCommand("pm grant $packageName $permission")
        return if (result.success || result.error.isEmpty()) {
            OperationResult(true, "已授予: ${permissionDisplayNames[permission] ?: permission}")
        } else {
            OperationResult(false, "授予失败: ${result.error}")
        }
    }

    fun revokeAllDangerousPermissions(packageName: String): OperationResult {
        val permInfo = getAppPermissions(packageName) ?: return OperationResult(false, "获取权限信息失败")
        var success = 0; var fail = 0
        val details = mutableListOf<String>()
        permInfo.permissions.filter { it.isGranted }.forEach { perm ->
            val result = revokePermission(packageName, perm.name)
            if (result.success) { success++; details.add("[OK] ${perm.displayName}") }
            else { fail++; details.add("[FAIL] ${perm.displayName}: ${result.message}") }
        }
        return OperationResult(fail == 0, "撤销完成: 成功 $success, 失败 $fail", details)
    }
}
