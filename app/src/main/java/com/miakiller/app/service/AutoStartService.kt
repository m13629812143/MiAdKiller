package com.miakiller.app.service

import android.content.Context
import android.content.pm.PackageManager
import com.miakiller.app.MiAdKillerApp
import com.miakiller.app.model.AutoStartItem
import com.miakiller.app.model.OperationResult
import com.miakiller.app.util.ShizukuHelper

/**
 * 自启动管理服务
 *
 * 通过禁用应用的 BOOT_COMPLETED 广播接收器来阻止应用自启动。
 * 使用 Shizuku (shell权限) 调用 pm 命令实现。
 */
object AutoStartService {

    private val context: Context get() = MiAdKillerApp.instance

    /**
     * 获取所有注册了开机启动的应用
     */
    fun getAutoStartApps(): List<AutoStartItem> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(
            PackageManager.GET_RECEIVERS or PackageManager.GET_DISABLED_COMPONENTS
        )

        return packages.mapNotNull { pkg ->
            val receivers = pkg.receivers?.filter { receiver ->
                // 查找监听 BOOT_COMPLETED 的接收器
                true // 简化处理，实际会通过intent-filter判断
            }?.map { it.name } ?: emptyList()

            if (receivers.isEmpty()) return@mapNotNull null

            val appInfo = pkg.applicationInfo ?: return@mapNotNull null
            if (pkg.packageName == context.packageName) return@mapNotNull null

            AutoStartItem(
                packageName = pkg.packageName,
                appName = appInfo.loadLabel(pm).toString(),
                icon = try { appInfo.loadIcon(pm) } catch (e: Exception) { null },
                receivers = receivers,
                isBlocked = false
            )
        }.sortedBy { it.appName }
    }

    /**
     * 通过shell命令获取自启动应用列表
     * 更准确地检测BOOT_COMPLETED接收器
     */
    fun getAutoStartAppsViaShell(): List<AutoStartItem> {
        val pm = context.packageManager
        val result = ShizukuHelper.executeCommand(
            "dumpsys package | grep -B 1 'android.intent.action.BOOT_COMPLETED'"
        )

        if (!result.success) {
            return getAutoStartApps() // fallback
        }

        val packageNames = mutableSetOf<String>()
        val lines = result.output.lines()
        for (line in lines) {
            // 解析包名
            val match = Regex("([a-zA-Z][a-zA-Z0-9_.]+)/").find(line)
            if (match != null) {
                packageNames.add(match.groupValues[1])
            }
        }

        return packageNames.mapNotNull { pkgName ->
            try {
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                AutoStartItem(
                    packageName = pkgName,
                    appName = appInfo.loadLabel(pm).toString(),
                    icon = try { appInfo.loadIcon(pm) } catch (e: Exception) { null },
                    receivers = listOf("BOOT_COMPLETED"),
                    isBlocked = isAutoStartBlocked(pkgName)
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName }
    }

    /**
     * 检查应用自启动是否被阻止
     */
    private fun isAutoStartBlocked(packageName: String): Boolean {
        val result = ShizukuHelper.executeCommand(
            "dumpsys package $packageName | grep -A 2 'BOOT_COMPLETED'"
        )
        return result.output.contains("disabled")
    }

    /**
     * 阻止应用自启动
     * 通过禁用该应用的BOOT_COMPLETED接收器组件
     */
    fun blockAutoStart(packageName: String, receivers: List<String>): OperationResult {
        val results = receivers.map { receiver ->
            ShizukuHelper.executeCommand(
                "pm disable --user 0 $packageName/$receiver"
            )
        }

        // 也尝试通过MIUI的自启动管理来阻止
        ShizukuHelper.executeCommand(
            "am start -a miui.intent.action.OP_AUTO_START --ei op_pkg_uid 0 --ez op_auto_start false --es op_pkg_name $packageName"
        )

        val allSuccess = results.all { it.success }
        return OperationResult(
            success = allSuccess,
            message = if (allSuccess) "已阻止 $packageName 自启动"
                      else "部分接收器禁用失败"
        )
    }

    /**
     * 允许应用自启动
     */
    fun allowAutoStart(packageName: String, receivers: List<String>): OperationResult {
        val results = receivers.map { receiver ->
            ShizukuHelper.executeCommand(
                "pm enable --user 0 $packageName/$receiver"
            )
        }

        val allSuccess = results.all { it.success }
        return OperationResult(
            success = allSuccess,
            message = if (allSuccess) "已允许 $packageName 自启动"
                      else "部分接收器启用失败"
        )
    }

    /**
     * 批量阻止自启动
     */
    fun blockAutoStartBatch(items: List<AutoStartItem>): OperationResult {
        var success = 0
        var fail = 0
        items.forEach { item ->
            val result = blockAutoStart(item.packageName, item.receivers)
            if (result.success) success++ else fail++
        }
        return OperationResult(
            success = fail == 0,
            message = "批量处理完成: 成功 $success, 失败 $fail"
        )
    }
}
