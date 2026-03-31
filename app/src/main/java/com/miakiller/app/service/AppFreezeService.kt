package com.miakiller.app.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.miakiller.app.MiAdKillerApp
import com.miakiller.app.model.AppInfo
import com.miakiller.app.model.OperationResult
import com.miakiller.app.util.AppLogger
import com.miakiller.app.util.IconHelper
import com.miakiller.app.util.ShizukuHelper

/**
 * APP冻结/解冻服务
 */
object AppFreezeService {

    private const val TAG = "FreezeService"
    private val context: Context get() = MiAdKillerApp.instance

    fun getInstalledApps(includeSystem: Boolean = true): List<AppInfo> {
        AppLogger.i(TAG, "开始加载应用列表, includeSystem=$includeSystem")
        val pm = context.packageManager
        return try {
            val packages = pm.getInstalledPackages(0)
            AppLogger.i(TAG, "获取到 ${packages.size} 个已安装包")

            packages.mapNotNull { pkg ->
                try {
                    val appInfo = pkg.applicationInfo ?: return@mapNotNull null
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    if (!includeSystem && isSystem) return@mapNotNull null
                    if (pkg.packageName == context.packageName) return@mapNotNull null

                    val info = AppInfo(
                        packageName = pkg.packageName,
                        appName = try { appInfo.loadLabel(pm).toString() } catch (e: Exception) { pkg.packageName },
                        isSystemApp = isSystem,
                        isFrozen = !appInfo.enabled,
                        isEnabled = appInfo.enabled,
                        versionName = pkg.versionName ?: ""
                    )
                    info.iconBitmap = IconHelper.loadAppIcon(pm, pkg.packageName)
                    info
                } catch (e: Exception) {
                    AppLogger.w(TAG, "解析应用失败: ${pkg.packageName}", e)
                    null
                }
            }.sortedWith(
                compareByDescending<AppInfo> { it.isFrozen }
                    .thenBy { it.isSystemApp }
                    .thenBy { it.appName }
            ).also {
                AppLogger.i(TAG, "应用列表加载完成, 共 ${it.size} 个")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "加载应用列表失败", e)
            emptyList()
        }
    }

    fun getFrozenApps(): List<AppInfo> {
        return getInstalledApps().filter { it.isFrozen }
    }

    fun freezeApp(packageName: String): OperationResult {
        AppLogger.i(TAG, "冻结应用: $packageName")
        val result = ShizukuHelper.executeCommand("pm disable-user --user 0 $packageName")
        return if (result.success || result.output.contains("disabled")) {
            AppLogger.i(TAG, "冻结成功: $packageName")
            OperationResult(true, "已冻结: $packageName")
        } else {
            AppLogger.e(TAG, "冻结失败: $packageName - ${result.error}")
            OperationResult(false, "冻结失败: ${result.error}")
        }
    }

    fun unfreezeApp(packageName: String): OperationResult {
        AppLogger.i(TAG, "解冻应用: $packageName")
        val result = ShizukuHelper.executeCommand("pm enable --user 0 $packageName")
        return if (result.success || result.output.contains("enabled")) {
            AppLogger.i(TAG, "解冻成功: $packageName")
            OperationResult(true, "已解冻: $packageName")
        } else {
            AppLogger.e(TAG, "解冻失败: $packageName - ${result.error}")
            OperationResult(false, "解冻失败: ${result.error}")
        }
    }

    fun freezeApps(packageNames: List<String>): OperationResult {
        var success = 0; var fail = 0
        val details = mutableListOf<String>()
        packageNames.forEach { pkg ->
            val result = freezeApp(pkg)
            if (result.success) { success++; details.add("[OK] $pkg") }
            else { fail++; details.add("[FAIL] $pkg: ${result.message}") }
        }
        return OperationResult(fail == 0, "冻结完成: 成功 $success, 失败 $fail", details)
    }

    fun unfreezeApps(packageNames: List<String>): OperationResult {
        var success = 0; var fail = 0
        val details = mutableListOf<String>()
        packageNames.forEach { pkg ->
            val result = unfreezeApp(pkg)
            if (result.success) { success++; details.add("[OK] $pkg") }
            else { fail++; details.add("[FAIL] $pkg: ${result.message}") }
        }
        return OperationResult(fail == 0, "解冻完成: 成功 $success, 失败 $fail", details)
    }

    fun forceStopApp(packageName: String): OperationResult {
        val result = ShizukuHelper.executeCommand("am force-stop $packageName")
        return if (result.success) OperationResult(true, "已停止: $packageName")
        else OperationResult(false, "停止失败: ${result.error}")
    }

    fun clearAppData(packageName: String): OperationResult {
        val result = ShizukuHelper.executeCommand("pm clear $packageName")
        return if (result.success) OperationResult(true, "已清除数据: $packageName")
        else OperationResult(false, "清除失败: ${result.error}")
    }

    fun getSuggestedFreezeApps(): List<String> {
        return listOf(
            "com.miui.systemAdSolution", "com.miui.analytics", "com.xiaomi.ab",
            "com.miui.contentcatcher", "com.miui.msa.global",
            "com.miui.hybrid", "com.miui.hybrid.accessory", "com.miui.newhome",
            "com.miui.yellowpage", "com.miui.personalassistant", "com.miui.bugreport",
            "com.xiaomi.gamecenter.sdk.service", "com.xiaomi.gamecenter", "com.miui.virtualsim",
            "com.miui.cleanmaster", "com.xiaomi.payment", "com.mipay.wallet",
            "com.miui.player", "com.miui.video", "com.miui.compass", "com.miui.fm",
            "com.miui.notes", "com.miui.voiceassist", "com.miui.mishare.connectivity",
            "com.xiaomi.mi_connect_service", "com.miui.huanji", "com.xiaomi.shop"
        )
    }
}
