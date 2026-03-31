package com.miakiller.app.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.miakiller.app.MiAdKillerApp
import com.miakiller.app.model.AppInfo
import com.miakiller.app.model.OperationResult
import com.miakiller.app.util.ShizukuHelper

/**
 * APP冻结/解冻服务
 *
 * 冻结原理: 使用 `pm disable-user` 命令禁用应用
 * 冻结后的应用不会在桌面显示，不会运行，不会占用资源
 * 但不会卸载应用，随时可以解冻恢复
 *
 * 通过 Shizuku (shell权限) 实现，无需Root
 */
object AppFreezeService {

    private val context: Context get() = MiAdKillerApp.instance

    /**
     * 获取已安装应用列表
     */
    fun getInstalledApps(includeSystem: Boolean = true): List<AppInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)

        return packages.mapNotNull { pkg ->
            val appInfo = pkg.applicationInfo ?: return@mapNotNull null
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            if (!includeSystem && isSystem) return@mapNotNull null

            // 排除自身
            if (pkg.packageName == context.packageName) return@mapNotNull null

            AppInfo(
                packageName = pkg.packageName,
                appName = appInfo.loadLabel(pm).toString(),
                icon = try { appInfo.loadIcon(pm) } catch (e: Exception) { null },
                isSystemApp = isSystem,
                isFrozen = !appInfo.enabled,
                isEnabled = appInfo.enabled,
                versionName = pkg.versionName ?: "",
                uid = appInfo.uid
            )
        }.sortedWith(
            compareByDescending<AppInfo> { it.isFrozen }
                .thenBy { it.isSystemApp }
                .thenBy { it.appName }
        )
    }

    /**
     * 获取已冻结的应用列表
     */
    fun getFrozenApps(): List<AppInfo> {
        return getInstalledApps().filter { it.isFrozen }
    }

    /**
     * 冻结应用 (禁用)
     */
    fun freezeApp(packageName: String): OperationResult {
        val result = ShizukuHelper.executeCommand(
            "pm disable-user --user 0 $packageName"
        )
        return if (result.success || result.output.contains("disabled")) {
            OperationResult(true, "已冻结: $packageName")
        } else {
            OperationResult(false, "冻结失败: ${result.error}")
        }
    }

    /**
     * 解冻应用 (启用)
     */
    fun unfreezeApp(packageName: String): OperationResult {
        val result = ShizukuHelper.executeCommand(
            "pm enable --user 0 $packageName"
        )
        return if (result.success || result.output.contains("enabled")) {
            OperationResult(true, "已解冻: $packageName")
        } else {
            OperationResult(false, "解冻失败: ${result.error}")
        }
    }

    /**
     * 批量冻结应用
     */
    fun freezeApps(packageNames: List<String>): OperationResult {
        var success = 0
        var fail = 0
        val details = mutableListOf<String>()

        packageNames.forEach { pkg ->
            val result = freezeApp(pkg)
            if (result.success) {
                success++
                details.add("[成功] $pkg")
            } else {
                fail++
                details.add("[失败] $pkg: ${result.message}")
            }
        }

        return OperationResult(
            success = fail == 0,
            message = "冻结完成: 成功 $success, 失败 $fail",
            details = details
        )
    }

    /**
     * 批量解冻应用
     */
    fun unfreezeApps(packageNames: List<String>): OperationResult {
        var success = 0
        var fail = 0
        val details = mutableListOf<String>()

        packageNames.forEach { pkg ->
            val result = unfreezeApp(pkg)
            if (result.success) {
                success++
                details.add("[成功] $pkg")
            } else {
                fail++
                details.add("[失败] $pkg: ${result.message}")
            }
        }

        return OperationResult(
            success = fail == 0,
            message = "解冻完成: 成功 $success, 失败 $fail",
            details = details
        )
    }

    /**
     * 强制停止应用
     */
    fun forceStopApp(packageName: String): OperationResult {
        val result = ShizukuHelper.executeCommand("am force-stop $packageName")
        return if (result.success) {
            OperationResult(true, "已停止: $packageName")
        } else {
            OperationResult(false, "停止失败: ${result.error}")
        }
    }

    /**
     * 清除应用数据
     */
    fun clearAppData(packageName: String): OperationResult {
        val result = ShizukuHelper.executeCommand("pm clear $packageName")
        return if (result.success) {
            OperationResult(true, "已清除数据: $packageName")
        } else {
            OperationResult(false, "清除失败: ${result.error}")
        }
    }

    /**
     * 获取小米预装应用列表(建议冻结)
     */
    fun getSuggestedFreezeApps(): List<String> {
        return listOf(
            "com.miui.systemAdSolution",    // MSA广告服务
            "com.miui.analytics",            // 小米分析
            "com.miui.hybrid",               // 快应用
            "com.miui.hybrid.accessory",     // 快应用关联
            "com.miui.msa.global",           // 全球MSA
            "com.miui.contentcatcher",       // 内容抓取
            "com.miui.yellowpage",           // 黄页
            "com.miui.newhome",              // 内容中心
            "com.miui.personalassistant",    // 智能助理(负一屏)
            "com.miui.virtualsim",           // 虚拟SIM
            "com.xiaomi.gamecenter.sdk.service", // 游戏中心SDK
            "com.xiaomi.payment",            // 小米支付
            "com.mipay.wallet",              // 小米钱包
            "com.miui.player",               // 小米音乐
            "com.miui.video",                // 小米视频
            "com.miui.bugreport",            // Bug报告
            "com.miui.mishare.connectivity",  // 小米互传
        )
    }
}
