package com.miakiller.app.service

import com.miakiller.app.model.HostsRule
import com.miakiller.app.model.OperationResult
import com.miakiller.app.util.ShizukuHelper

/**
 * Hosts文件广告屏蔽服务
 *
 * 通过修改 /etc/hosts 文件，将广告域名指向 127.0.0.1 来屏蔽广告。
 * 注意: 修改 /etc/hosts 需要root权限或者通过Shizuku挂载可写分区。
 *
 * 在非Root设备上，需要 Shizuku + 特殊挂载技巧。
 * 如果无法直接修改hosts，会提供VPN方案作为fallback。
 */
object HostsService {

    /**
     * 小米/MIUI 相关广告域名列表
     */
    fun getMiuiAdDomains(): List<HostsRule> {
        return listOf(
            // 小米广告核心域名
            HostsRule("ad.xiaomi.com", "小米广告主域名", "小米广告"),
            HostsRule("ad1.xiaomi.com", "小米广告服务器1", "小米广告"),
            HostsRule("ad.mi.com", "小米广告", "小米广告"),
            HostsRule("adi.xiaomi.com", "小米广告接口", "小米广告"),
            HostsRule("adv.sec.miui.com", "安全中心广告", "小米广告"),

            // MSA (MIUI System Ads)
            HostsRule("sdkconfig.ad.xiaomi.com", "MSA广告SDK配置", "MSA"),
            HostsRule("globalapi.ad.xiaomi.com", "MSA全局广告API", "MSA"),
            HostsRule("cnbj-maacs.ad.xiaomi.com", "MSA广告投放", "MSA"),

            // 小米数据追踪
            HostsRule("tracking.miui.com", "MIUI追踪", "追踪"),
            HostsRule("tracking.intl.miui.com", "MIUI国际追踪", "追踪"),
            HostsRule("t.browser.miui.com", "浏览器追踪", "追踪"),
            HostsRule("sa.api.intl.miui.com", "数据分析API", "追踪"),
            HostsRule("data.mistat.xiaomi.com", "小米统计数据", "追踪"),
            HostsRule("data.mistat.intl.xiaomi.com", "国际统计数据", "追踪"),
            HostsRule("o2o.api.xiaomi.com", "O2O广告", "追踪"),

            // 小米推送广告
            HostsRule("api.ad.xiaomi.com", "广告API", "推送广告"),
            HostsRule("mis.token.xiaomi.com", "广告Token", "推送广告"),
            HostsRule("api.zhuti.xiaomi.com", "主题商店广告", "推送广告"),
            HostsRule("cdn.ad.xiaomi.com", "广告CDN", "推送广告"),

            // 小米内容/信息流
            HostsRule("api.market.xiaomi.com", "应用商店推荐API", "内容推荐"),
            HostsRule("huiyan.xiaomi.com", "慧眼数据收集", "内容推荐"),
            HostsRule("resolver.msg.xiaomi.net", "消息推送", "内容推荐"),

            // 通用广告域名
            HostsRule("e.ad.xiaomi.com", "广告事件上报", "通用广告"),
            HostsRule("migc.xiaomi.com", "游戏中心广告", "通用广告"),
            HostsRule("dvr.mi.com", "设备注册追踪", "通用广告"),

            // 第三方广告SDK (常见于小米预装应用)
            HostsRule("sdk.open.talk.getui.com", "个推SDK", "第三方SDK"),
            HostsRule("s.union.mi.com", "小米联盟广告", "第三方SDK"),
            HostsRule("ad.doubleclick.net", "Google广告", "第三方SDK"),
            HostsRule("pagead2.googlesyndication.com", "Google广告联盟", "第三方SDK"),
        )
    }

    /**
     * 获取当前hosts文件内容
     */
    fun getCurrentHosts(): String {
        val result = ShizukuHelper.executeCommand("cat /etc/hosts")
        return if (result.success) result.output else ""
    }

    /**
     * 检查hosts文件是否可写
     */
    fun isHostsWritable(): Boolean {
        // 尝试检查/system或/etc挂载状态
        val result = ShizukuHelper.executeCommand("mount | grep ' /system '")
        return result.output.contains("rw")
    }

    /**
     * 应用hosts广告屏蔽规则
     *
     * 方案说明:
     * 1. 首先尝试直接修改 /etc/hosts (需要可写的system分区)
     * 2. 如果失败，尝试通过 mount --bind 技巧
     * 3. 最终方案：生成hosts文件供用户手动导入或使用VPN方式
     */
    fun applyHostsRules(rules: List<HostsRule>): OperationResult {
        val enabledRules = rules.filter { it.isEnabled }
        if (enabledRules.isEmpty()) {
            return OperationResult(false, "没有启用的规则")
        }

        // 构建hosts内容
        val hostsContent = buildString {
            appendLine("# === MiAdKiller Hosts Rules ===")
            appendLine("# Generated at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
            appendLine("# Total rules: ${enabledRules.size}")
            appendLine()
            appendLine("127.0.0.1 localhost")
            appendLine("::1 localhost")
            appendLine()

            enabledRules.groupBy { it.category }.forEach { (category, categoryRules) ->
                appendLine("# --- $category ---")
                categoryRules.forEach { rule ->
                    appendLine("127.0.0.1 ${rule.domain}")
                }
                appendLine()
            }

            appendLine("# === End MiAdKiller Rules ===")
        }

        // 方案1: 尝试直接写入
        val writeResult = tryDirectWrite(hostsContent)
        if (writeResult.success) return writeResult

        // 方案2: 使用 mount --bind 技巧
        val mountResult = tryMountBind(hostsContent)
        if (mountResult.success) return mountResult

        // 方案3: 保存到可访问位置，提示用户
        return saveHostsToAccessibleLocation(hostsContent)
    }

    private fun tryDirectWrite(content: String): OperationResult {
        // 备份原始hosts
        ShizukuHelper.executeCommand("cp /etc/hosts /etc/hosts.bak")

        // 尝试重新挂载system为可写
        ShizukuHelper.executeCommand("mount -o rw,remount /system")

        // 写入新hosts
        val escapedContent = content.replace("\"", "\\\"")
        val result = ShizukuHelper.executeCommand(
            "echo \"$escapedContent\" > /etc/hosts"
        )

        return if (result.success) {
            // 恢复只读
            ShizukuHelper.executeCommand("mount -o ro,remount /system")
            OperationResult(true, "Hosts规则已应用 (直接写入)")
        } else {
            OperationResult(false, "直接写入失败: ${result.error}")
        }
    }

    private fun tryMountBind(content: String): OperationResult {
        // 将hosts写入app私有目录，然后mount --bind
        val tempPath = "/data/local/tmp/miakiller_hosts"
        val escapedContent = content.replace("\"", "\\\"")

        val writeResult = ShizukuHelper.executeCommand(
            "echo \"$escapedContent\" > $tempPath"
        )
        if (!writeResult.success) {
            return OperationResult(false, "写入临时文件失败")
        }

        ShizukuHelper.executeCommand("chmod 644 $tempPath")
        val mountResult = ShizukuHelper.executeCommand(
            "mount --bind $tempPath /etc/hosts"
        )

        return if (mountResult.success) {
            OperationResult(true, "Hosts规则已应用 (mount bind方式，重启后失效)")
        } else {
            OperationResult(false, "Mount bind失败: ${mountResult.error}")
        }
    }

    private fun saveHostsToAccessibleLocation(content: String): OperationResult {
        val savePath = "/sdcard/Download/miakiller_hosts.txt"
        val escapedContent = content.replace("\"", "\\\"")

        val result = ShizukuHelper.executeCommand(
            "echo \"$escapedContent\" > $savePath"
        )

        return if (result.success) {
            OperationResult(
                success = true,
                message = "Hosts规则已保存到: $savePath",
                details = listOf(
                    "无法直接修改系统hosts文件 (非Root设备限制)",
                    "规则已保存到 $savePath",
                    "建议方案:",
                    "1. 使用 AdGuard/personalDNSfilter 等VPN方式广告屏蔽APP导入此规则",
                    "2. 或通过私有DNS设置使用 dns.adguard.com 等广告过滤DNS"
                )
            )
        } else {
            OperationResult(false, "保存hosts文件失败")
        }
    }

    /**
     * 恢复原始hosts文件
     */
    fun restoreHosts(): OperationResult {
        val result = ShizukuHelper.executeCommand(
            "if [ -f /etc/hosts.bak ]; then cp /etc/hosts.bak /etc/hosts; echo 'restored'; else echo 'no backup'; fi"
        )
        return if (result.output.contains("restored")) {
            OperationResult(true, "已恢复原始hosts文件")
        } else {
            OperationResult(false, "没有找到备份文件")
        }
    }

    /**
     * 获取按分类分组的规则
     */
    fun getRulesByCategory(): Map<String, List<HostsRule>> {
        return getMiuiAdDomains().groupBy { it.category }
    }
}
