package com.miakiller.app.service

import com.miakiller.app.model.MiuiAdSwitch
import com.miakiller.app.model.OperationResult
import com.miakiller.app.util.ShizukuHelper

/**
 * MIUI/HyperOS 广告开关管理服务
 *
 * 小米系统在各处内置了广告开关，这些开关存储在 Android Settings Provider 中。
 * 通过 Shizuku 执行 `settings put` 命令可以批量关闭这些广告。
 *
 * 适用于: MIUI 12/13/14, HyperOS 1.0/2.0 (小米15搭载HyperOS 2.0)
 */
object MiuiAdService {

    /**
     * 获取所有已知的MIUI广告开关
     * 这是根据MIUI/HyperOS系统源码和社区逆向整理的完整列表
     */
    fun getAllAdSwitches(): List<MiuiAdSwitch> {
        return listOf(
            // ===== 系统级广告 =====
            MiuiAdSwitch(
                name = "个性化广告推荐",
                description = "系统全局个性化广告推荐开关，关闭后不再根据用户行为推荐广告",
                settingsKey = "personalized_ad_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统广告"
            ),
            MiuiAdSwitch(
                name = "MSA广告服务",
                description = "小米系统广告服务(MIUI System Ads)，是大部分系统广告的来源",
                settingsKey = "msa_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统广告"
            ),
            MiuiAdSwitch(
                name = "智能服务推荐",
                description = "系统智能服务推荐，可能推送广告内容",
                settingsKey = "intelligent_service_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统广告"
            ),
            MiuiAdSwitch(
                name = "用户体验计划",
                description = "MIUI用户体验改进计划，收集使用数据用于广告投放",
                settingsKey = "upload_log_pref",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统广告"
            ),

            // ===== 应用商店 =====
            MiuiAdSwitch(
                name = "应用商店推荐",
                description = "应用商店中的个性化推荐和广告",
                settingsKey = "market_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "应用商店"
            ),
            MiuiAdSwitch(
                name = "应用商店热榜推广",
                description = "应用商店热榜中的推广内容",
                settingsKey = "market_hot_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "应用商店"
            ),

            // ===== 桌面/负一屏 =====
            MiuiAdSwitch(
                name = "负一屏信息流广告",
                description = "桌面左滑负一屏(智能助理)中的广告和推荐",
                settingsKey = "pref_ai_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "桌面"
            ),
            MiuiAdSwitch(
                name = "桌面推荐内容",
                description = "桌面上的推荐内容卡片",
                settingsKey = "launcher_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "桌面"
            ),

            // ===== 浏览器 =====
            MiuiAdSwitch(
                name = "浏览器推荐",
                description = "小米浏览器中的新闻推荐和广告",
                settingsKey = "browser_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "浏览器"
            ),
            MiuiAdSwitch(
                name = "浏览器信息流",
                description = "浏览器首页信息流广告",
                settingsKey = "browser_feed_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "浏览器"
            ),

            // ===== 安全中心/手机管家 =====
            MiuiAdSwitch(
                name = "安全中心推荐",
                description = "安全中心/手机管家中的推荐内容",
                settingsKey = "security_center_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "安全中心"
            ),
            MiuiAdSwitch(
                name = "垃圾清理推荐",
                description = "清理时显示的推广内容",
                settingsKey = "cleaner_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "安全中心"
            ),

            // ===== 设置 =====
            MiuiAdSwitch(
                name = "设置页推荐",
                description = "系统设置页面中的推荐内容",
                settingsKey = "settings_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "设置"
            ),

            // ===== 音乐 =====
            MiuiAdSwitch(
                name = "音乐推荐广告",
                description = "小米音乐APP中的广告",
                settingsKey = "music_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "音乐"
            ),

            // ===== 视频 =====
            MiuiAdSwitch(
                name = "视频推荐广告",
                description = "小米视频APP中的广告推荐",
                settingsKey = "video_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "视频"
            ),

            // ===== 天气 =====
            MiuiAdSwitch(
                name = "天气推荐内容",
                description = "天气APP中的广告和推荐内容",
                settingsKey = "weather_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "天气"
            ),

            // ===== 日历 =====
            MiuiAdSwitch(
                name = "日历推荐内容",
                description = "日历APP中的广告和推荐内容",
                settingsKey = "calendar_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "日历"
            ),

            // ===== 锁屏/主题 =====
            MiuiAdSwitch(
                name = "锁屏画报",
                description = "锁屏自动更换的壁纸画报(含广告)",
                settingsKey = "lockscreen_magazine",
                settingsTable = "system",
                disableValue = "0",
                category = "锁屏"
            ),
            MiuiAdSwitch(
                name = "主题商店推荐",
                description = "主题商店中的推荐和推广",
                settingsKey = "theme_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "主题"
            ),

            // ===== 文件管理器 =====
            MiuiAdSwitch(
                name = "文件管理器推荐",
                description = "文件管理器中的广告和推荐",
                settingsKey = "filemanager_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "文件管理"
            ),

            // ===== 下载管理 =====
            MiuiAdSwitch(
                name = "下载管理推荐",
                description = "下载管理器中的推荐内容",
                settingsKey = "download_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "下载管理"
            ),

            // ===== 通知栏 =====
            MiuiAdSwitch(
                name = "通知栏推送广告",
                description = "通过通知栏推送的广告内容",
                settingsKey = "notification_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "通知"
            ),

            // ===== 搜索 =====
            MiuiAdSwitch(
                name = "全局搜索推荐",
                description = "全局搜索中的热搜和推荐广告",
                settingsKey = "search_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "搜索"
            ),
            MiuiAdSwitch(
                name = "搜索热词",
                description = "搜索框中的热词推荐(可能含推广)",
                settingsKey = "search_hot_word_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "搜索"
            ),

            // ===== 短信 =====
            MiuiAdSwitch(
                name = "短信推荐",
                description = "短信应用中的推荐和通知广告",
                settingsKey = "sms_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "短信"
            ),

            // ===== 笔记 =====
            MiuiAdSwitch(
                name = "笔记推荐",
                description = "笔记应用中的推荐内容",
                settingsKey = "notes_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "笔记"
            )
        )
    }

    /**
     * 读取当前所有广告开关的状态
     */
    fun refreshSwitchStates(switches: List<MiuiAdSwitch>): List<MiuiAdSwitch> {
        return switches.map { switch ->
            val result = ShizukuHelper.executeCommand(
                "settings get ${switch.settingsTable} ${switch.settingsKey}"
            )
            val currentValue = result.output.trim()
            switch.copy(
                isDisabled = currentValue == switch.disableValue
            )
        }
    }

    /**
     * 一键关闭所有广告
     */
    fun disableAllAds(): OperationResult {
        val switches = getAllAdSwitches()
        val results = mutableListOf<String>()
        var successCount = 0
        var failCount = 0

        for (switch in switches) {
            val result = ShizukuHelper.executeCommand(
                "settings put ${switch.settingsTable} ${switch.settingsKey} ${switch.disableValue}"
            )
            if (result.success) {
                successCount++
                results.add("[成功] ${switch.name}")
            } else {
                failCount++
                results.add("[失败] ${switch.name}: ${result.error}")
            }
        }

        // 额外: 禁用MSA广告组件
        disableMsaComponent()

        return OperationResult(
            success = failCount == 0,
            message = "已关闭 $successCount/${switches.size} 个广告开关" +
                    if (failCount > 0) "，$failCount 个失败" else "",
            details = results
        )
    }

    /**
     * 关闭单个广告开关
     */
    fun disableAdSwitch(switch: MiuiAdSwitch): Boolean {
        val result = ShizukuHelper.executeCommand(
            "settings put ${switch.settingsTable} ${switch.settingsKey} ${switch.disableValue}"
        )
        return result.success
    }

    /**
     * 开启单个广告开关 (恢复)
     */
    fun enableAdSwitch(switch: MiuiAdSwitch): Boolean {
        val result = ShizukuHelper.executeCommand(
            "settings put ${switch.settingsTable} ${switch.settingsKey} ${switch.enableValue}"
        )
        return result.success
    }

    /**
     * 禁用MSA(小米系统广告服务)组件
     * MSA是小米系统广告的核心服务
     */
    private fun disableMsaComponent() {
        // 尝试禁用MSA的主要组件
        val msaPackages = listOf(
            "com.miui.systemAdSolution",
            "com.miui.analytics"
        )
        for (pkg in msaPackages) {
            ShizukuHelper.executeCommand("pm disable-user --user 0 $pkg")
        }
    }

    /**
     * 获取按分类分组的广告开关
     */
    fun getAdSwitchesByCategory(): Map<String, List<MiuiAdSwitch>> {
        return getAllAdSwitches().groupBy { it.category }
    }
}
