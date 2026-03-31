package com.miakiller.app.service

import android.content.Context
import android.content.pm.PackageManager
import com.miakiller.app.MiAdKillerApp
import com.miakiller.app.model.AutoStartItem
import com.miakiller.app.model.OperationResult
import com.miakiller.app.util.AppLogger
import com.miakiller.app.util.IconHelper
import com.miakiller.app.util.ShizukuHelper

/**
 * 自启动管理服务
 */
object AutoStartService {

    private const val TAG = "AutoStartService"
    private val context: Context get() = MiAdKillerApp.instance

    fun getAutoStartApps(): List<AutoStartItem> {
        AppLogger.i(TAG, "开始加载自启动应用列表(本地)")
        val pm = context.packageManager
        return try {
            val packages = pm.getInstalledPackages(PackageManager.GET_RECEIVERS)
            packages.mapNotNull { pkg ->
                try {
                    val receivers = pkg.receivers?.map { it.name } ?: emptyList()
                    if (receivers.isEmpty()) return@mapNotNull null
                    val appInfo = pkg.applicationInfo ?: return@mapNotNull null
                    if (pkg.packageName == context.packageName) return@mapNotNull null

                    val item = AutoStartItem(
                        packageName = pkg.packageName,
                        appName = try { appInfo.loadLabel(pm).toString() } catch (e: Exception) { pkg.packageName },
                        receivers = receivers,
                        isBlocked = false
                    )
                    item.iconBitmap = IconHelper.loadAppIcon(pm, pkg.packageName)
                    item
                } catch (e: Exception) {
                    AppLogger.w(TAG, "解析自启动应用失败: ${pkg.packageName}", e)
                    null
                }
            }.sortedBy { it.appName }
        } catch (e: Exception) {
            AppLogger.e(TAG, "加载自启动列表失败", e)
            emptyList()
        }
    }

    fun getAutoStartAppsViaShell(): List<AutoStartItem> {
        AppLogger.i(TAG, "开始加载自启动应用列表(Shell)")

        if (!ShizukuHelper.isGranted.value) {
            AppLogger.w(TAG, "Shizuku未授权，使用本地方式加载")
            return getAutoStartApps()
        }

        val pm = context.packageManager
        val result = ShizukuHelper.executeCommand(
            "dumpsys package | grep -B 1 'android.intent.action.BOOT_COMPLETED'"
        )

        if (!result.success) {
            AppLogger.w(TAG, "Shell命令失败: ${result.error}, fallback到本地方式")
            return getAutoStartApps()
        }

        val packageNames = mutableSetOf<String>()
        result.output.lines().forEach { line ->
            val match = Regex("([a-zA-Z][a-zA-Z0-9_.]+)/").find(line)
            if (match != null) packageNames.add(match.groupValues[1])
        }

        AppLogger.i(TAG, "Shell方式找到 ${packageNames.size} 个自启动包名")

        return packageNames.mapNotNull { pkgName ->
            try {
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                val item = AutoStartItem(
                    packageName = pkgName,
                    appName = try { appInfo.loadLabel(pm).toString() } catch (e: Exception) { pkgName },
                    receivers = listOf("BOOT_COMPLETED"),
                    isBlocked = isAutoStartBlocked(pkgName)
                )
                item.iconBitmap = IconHelper.loadAppIcon(pm, pkgName)
                item
            } catch (e: PackageManager.NameNotFoundException) {
                null // 包已卸载
            } catch (e: Exception) {
                AppLogger.w(TAG, "解析自启动应用失败: $pkgName", e)
                null
            }
        }.sortedBy { it.appName }.also {
            AppLogger.i(TAG, "自启动列表加载完成, 共 ${it.size} 个")
        }
    }

    private fun isAutoStartBlocked(packageName: String): Boolean {
        if (!ShizukuHelper.isGranted.value) return false
        val result = ShizukuHelper.executeCommand(
            "dumpsys package $packageName | grep -A 2 'BOOT_COMPLETED'"
        )
        return result.output.contains("disabled")
    }

    fun blockAutoStart(packageName: String, receivers: List<String>): OperationResult {
        AppLogger.i(TAG, "阻止自启动: $packageName")
        val results = receivers.map { receiver ->
            ShizukuHelper.executeCommand("pm disable --user 0 $packageName/$receiver")
        }
        ShizukuHelper.executeCommand(
            "am start -a miui.intent.action.OP_AUTO_START --ei op_pkg_uid 0 --ez op_auto_start false --es op_pkg_name $packageName"
        )
        val allSuccess = results.all { it.success }
        return OperationResult(allSuccess, if (allSuccess) "已阻止 $packageName 自启动" else "部分接收器禁用失败")
    }

    fun allowAutoStart(packageName: String, receivers: List<String>): OperationResult {
        AppLogger.i(TAG, "允许自启动: $packageName")
        val results = receivers.map { receiver ->
            ShizukuHelper.executeCommand("pm enable --user 0 $packageName/$receiver")
        }
        val allSuccess = results.all { it.success }
        return OperationResult(allSuccess, if (allSuccess) "已允许 $packageName 自启动" else "部分接收器启用失败")
    }

    fun blockAutoStartBatch(items: List<AutoStartItem>): OperationResult {
        var success = 0; var fail = 0
        items.forEach { item ->
            val result = blockAutoStart(item.packageName, item.receivers)
            if (result.success) success++ else fail++
        }
        return OperationResult(fail == 0, "批量处理完成: 成功 $success, 失败 $fail")
    }
}
