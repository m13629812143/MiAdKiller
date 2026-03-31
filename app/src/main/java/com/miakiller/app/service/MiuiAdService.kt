package com.miakiller.app.service

import com.miakiller.app.model.MiuiAdSwitch
import com.miakiller.app.model.OperationResult
import com.miakiller.app.util.ShizukuHelper

/**
 * 澎湃OS3 / HyperOS 广告开关管理服务
 *
 * 适配: 小米15 - 澎湃OS3 (3.0.7.0.WOCCNXM) 基于 Android 16
 *
 * 澎湃OS3 相比 MIUI/HyperOS 1.x/2.x 的变化:
 * - 广告体系从 MSA 迁移到 MiAdServices
 * - 部分 settings key 命名空间变为 hyperos_ 前缀
 * - 新增"智能推荐引擎"统一管理各应用的推荐内容
 * - 安全与隐私设置路径重构
 * - 应用商店更名为小米应用市场，包名不变
 */
object MiuiAdService {

    /**
     * 获取所有已知的澎湃OS3广告开关
     *
     * 数据来源:
     * - Settings Provider 数据库分析
     * - 澎湃OS3 系统设置 Activity 逆向
     * - 社区用户反馈验证
     */
    fun getAllAdSwitches(): List<MiuiAdSwitch> {
        return listOf(
            // ========================================
            // 系统级广告/隐私 (最重要，优先关闭)
            // ========================================
            MiuiAdSwitch(
                name = "个性化广告推荐",
                description = "设置 > 隐私保护 > 广告服务 > 个性化广告推荐。关闭后系统不再根据用户画像投放定向广告",
                settingsKey = "personalized_ad_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统核心"
            ),
            MiuiAdSwitch(
                name = "个性化推荐",
                description = "设置 > 隐私保护 > 个性化推荐总开关。控制系统全局的推荐内容",
                settingsKey = "personalized_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统核心"
            ),
            MiuiAdSwitch(
                name = "广告服务 (MiAd)",
                description = "小米广告服务核心开关，控制所有小米系统应用内的广告展示",
                settingsKey = "mi_ad_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统核心"
            ),
            MiuiAdSwitch(
                name = "MSA广告服务",
                description = "MIUI System Ads 广告投放引擎，澎湃OS3中仍保留此兼容开关",
                settingsKey = "msa_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统核心"
            ),
            MiuiAdSwitch(
                name = "智能推荐引擎",
                description = "澎湃OS3新增的统一推荐引擎，为各应用提供个性化推荐内容",
                settingsKey = "hyperos_smart_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统核心"
            ),
            MiuiAdSwitch(
                name = "智能服务",
                description = "系统智能服务推荐，会分析使用习惯推送建议和广告",
                settingsKey = "intelligent_service_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "系统核心"
            ),

            // ========================================
            // 隐私与数据收集
            // ========================================
            MiuiAdSwitch(
                name = "用户体验改进计划",
                description = "设置 > 隐私保护 > 加入用户体验改进计划。收集使用数据用于改进和广告",
                settingsKey = "upload_log_pref",
                settingsTable = "secure",
                disableValue = "0",
                category = "隐私与数据"
            ),
            MiuiAdSwitch(
                name = "自动发送诊断数据",
                description = "自动收集并发送系统诊断数据到小米服务器",
                settingsKey = "mi_diagnostic_data",
                settingsTable = "secure",
                disableValue = "0",
                category = "隐私与数据"
            ),
            MiuiAdSwitch(
                name = "使用情况统计",
                description = "收集应用使用时长、频率等数据，用于个性化推荐",
                settingsKey = "usage_stats_collection",
                settingsTable = "secure",
                disableValue = "0",
                category = "隐私与数据"
            ),

            // ========================================
            // 应用商店 (小米应用市场)
            // ========================================
            MiuiAdSwitch(
                name = "应用商店个性化推荐",
                description = "小米应用市场中的个性化应用推荐和广告",
                settingsKey = "market_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "应用商店"
            ),
            MiuiAdSwitch(
                name = "应用商店热榜推广",
                description = "应用市场热榜、排行榜中的推广位",
                settingsKey = "market_hot_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "应用商店"
            ),
            MiuiAdSwitch(
                name = "应用安装完成页推荐",
                description = "通过应用商店安装应用后，完成页面的相关应用推荐",
                settingsKey = "market_install_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "应用商店"
            ),
            MiuiAdSwitch(
                name = "应用更新提醒推广",
                description = "应用更新提醒中夹带的推广内容",
                settingsKey = "market_update_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "应用商店"
            ),

            // ========================================
            // 桌面/负一屏/小部件
            // ========================================
            MiuiAdSwitch(
                name = "负一屏信息流",
                description = "桌面左滑进入的智能助理(负一屏)中的信息流广告和推荐卡片",
                settingsKey = "pref_ai_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "桌面"
            ),
            MiuiAdSwitch(
                name = "桌面推荐内容",
                description = "桌面上的内容推荐卡片/快捷方式推荐",
                settingsKey = "launcher_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "桌面"
            ),
            MiuiAdSwitch(
                name = "桌面文件夹推荐",
                description = "打开桌面文件夹时底部的应用推荐",
                settingsKey = "launcher_folder_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "桌面"
            ),
            MiuiAdSwitch(
                name = "小部件推荐",
                description = "小部件中的推荐内容和广告",
                settingsKey = "widget_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "桌面"
            ),

            // ========================================
            // 浏览器
            // ========================================
            MiuiAdSwitch(
                name = "浏览器推荐",
                description = "小米浏览器中的新闻推荐和广告",
                settingsKey = "browser_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "浏览器"
            ),
            MiuiAdSwitch(
                name = "浏览器首页信息流",
                description = "浏览器首页的信息流广告和推荐文章",
                settingsKey = "browser_feed_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "浏览器"
            ),
            MiuiAdSwitch(
                name = "浏览器热搜",
                description = "浏览器搜索框中的热搜推荐(含推广)",
                settingsKey = "browser_hot_search",
                settingsTable = "secure",
                disableValue = "0",
                category = "浏览器"
            ),

            // ========================================
            // 手机管家 (安全中心)
            // ========================================
            MiuiAdSwitch(
                name = "手机管家推荐",
                description = "手机管家/安全中心中的推荐内容和广告",
                settingsKey = "security_center_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "手机管家"
            ),
            MiuiAdSwitch(
                name = "垃圾清理推荐",
                description = "清理垃圾时显示的推广和应用推荐",
                settingsKey = "cleaner_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "手机管家"
            ),
            MiuiAdSwitch(
                name = "病毒扫描推荐",
                description = "病毒扫描结果页的推荐内容",
                settingsKey = "security_scan_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "手机管家"
            ),

            // ========================================
            // 设置
            // ========================================
            MiuiAdSwitch(
                name = "设置页推荐",
                description = "系统设置页面顶部的推荐内容和小米服务推广",
                settingsKey = "settings_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "设置"
            ),

            // ========================================
            // 锁屏/主题/壁纸
            // ========================================
            MiuiAdSwitch(
                name = "锁屏画报",
                description = "锁屏自动更换壁纸画报，含品牌合作广告内容",
                settingsKey = "lockscreen_magazine",
                settingsTable = "system",
                disableValue = "0",
                category = "锁屏与主题"
            ),
            MiuiAdSwitch(
                name = "锁屏通知推广",
                description = "锁屏界面上的推广通知和卡片",
                settingsKey = "lockscreen_promote_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "锁屏与主题"
            ),
            MiuiAdSwitch(
                name = "主题商店推荐",
                description = "主题商店中的付费主题推荐和广告位",
                settingsKey = "theme_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "锁屏与主题"
            ),
            MiuiAdSwitch(
                name = "壁纸轮播推荐",
                description = "壁纸轮播功能中的推荐壁纸(可能含品牌合作)",
                settingsKey = "wallpaper_carousel_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "锁屏与主题"
            ),

            // ========================================
            // 通知/搜索
            // ========================================
            MiuiAdSwitch(
                name = "通知栏推送广告",
                description = "系统通过通知栏推送的推广和活动通知",
                settingsKey = "notification_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "通知与搜索"
            ),
            MiuiAdSwitch(
                name = "全局搜索推荐",
                description = "下拉全局搜索中的热搜词和推荐内容",
                settingsKey = "search_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "通知与搜索"
            ),
            MiuiAdSwitch(
                name = "搜索热词",
                description = "搜索框中的热门搜索词推荐(含推广)",
                settingsKey = "search_hot_word_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "通知与搜索"
            ),

            // ========================================
            // 内置应用 (天气/日历/音乐/视频/文件/短信/笔记/时钟)
            // ========================================
            MiuiAdSwitch(
                name = "天气推荐内容",
                description = "天气APP底部的推荐信息和广告",
                settingsKey = "weather_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "日历推荐内容",
                description = "日历APP中的服务推荐和活动广告",
                settingsKey = "calendar_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "音乐推荐广告",
                description = "小米音乐APP中的VIP推广和广告",
                settingsKey = "music_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "视频推荐广告",
                description = "小米视频APP中的广告推荐和信息流",
                settingsKey = "video_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "文件管理器推荐",
                description = "文件管理器中的清理推荐和广告",
                settingsKey = "filemanager_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "下载管理推荐",
                description = "下载管理器中的应用推荐",
                settingsKey = "download_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "短信推荐",
                description = "短信应用中的卡片推荐和通知广告",
                settingsKey = "sms_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "笔记推荐",
                description = "笔记应用中的模板推荐",
                settingsKey = "notes_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "时钟推荐",
                description = "时钟/闹钟应用中的推荐内容",
                settingsKey = "clock_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),
            MiuiAdSwitch(
                name = "录音机推荐",
                description = "录音机应用中的增值服务推荐",
                settingsKey = "recorder_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "内置应用"
            ),

            // ========================================
            // 小米账号/云服务
            // ========================================
            MiuiAdSwitch(
                name = "小米账号推荐",
                description = "小米账号相关的会员推荐和推广",
                settingsKey = "mi_account_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "小米服务"
            ),
            MiuiAdSwitch(
                name = "云服务推荐",
                description = "小米云服务的扩容和VIP推广",
                settingsKey = "cloud_service_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "小米服务"
            ),
            MiuiAdSwitch(
                name = "小米钱包推荐",
                description = "小米钱包/支付中的金融推广",
                settingsKey = "wallet_recommend_enabled",
                settingsTable = "secure",
                disableValue = "0",
                category = "小米服务"
            ),
            MiuiAdSwitch(
                name = "游戏加速推荐",
                description = "游戏加速器中的游戏推荐和广告",
                settingsKey = "game_booster_recommend",
                settingsTable = "secure",
                disableValue = "0",
                category = "小米服务"
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
            // "null" 表示从未设置过，按系统默认(开启广告)处理
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
                results.add("[OK] ${switch.name}")
            } else {
                failCount++
                results.add("[FAIL] ${switch.name}: ${result.error}")
            }
        }

        // 禁用广告相关系统组件
        disableAdComponents()

        // 额外: 通过 content provider 关闭各应用内部的推荐开关
        disableAppInternalAds()

        return OperationResult(
            success = failCount == 0,
            message = "已关闭 $successCount/${switches.size} 个广告开关" +
                    if (failCount > 0) "，$failCount 个失败" else " + 广告组件已禁用",
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
     * 禁用广告相关系统组件
     *
     * 澎湃OS3 中广告相关的核心包名:
     * - com.miui.systemAdSolution   (MSA广告引擎，澎湃OS3仍保留)
     * - com.miui.analytics           (小米数据分析SDK)
     * - com.xiaomi.ab                (A/B测试框架，用于广告实验)
     * - com.miui.contentcatcher      (内容抓取，用于推荐)
     * - com.miui.msa.global          (海外MSA，国行可能存在)
     */
    private fun disableAdComponents() {
        val adPackages = listOf(
            "com.miui.systemAdSolution",
            "com.miui.analytics",
            "com.xiaomi.ab",
            "com.miui.contentcatcher",
            "com.miui.msa.global"
        )
        for (pkg in adPackages) {
            ShizukuHelper.executeCommand("pm disable-user --user 0 $pkg")
        }

        // 禁用MSA的特定Activity和Service组件
        val msaComponents = listOf(
            "com.miui.systemAdSolution/com.xiaomi.ad.AdManagerService",
            "com.miui.systemAdSolution/com.xiaomi.ad.splash.SplashAdActivity"
        )
        for (component in msaComponents) {
            ShizukuHelper.executeCommand("pm disable --user 0 $component")
        }
    }

    /**
     * 通过各应用自身的 SharedPreferences / ContentProvider 关闭内部广告
     *
     * 澎湃OS3 部分应用的广告开关不走 Settings Provider，
     * 而是存在应用自身的配置中，需要通过 content provider 或 am 命令修改
     */
    private fun disableAppInternalAds() {
        // 小米浏览器: 关闭首页推荐
        ShizukuHelper.executeCommand(
            "am broadcast -a com.android.browser.SETTINGS_CHANGE " +
            "--es key browser_show_recommend --ez value false " +
            "-p com.android.browser"
        )

        // 小米音乐: 关闭广告
        ShizukuHelper.executeCommand(
            "am broadcast -a com.miui.player.SETTINGS_CHANGE " +
            "--es key show_ad --ez value false " +
            "-p com.miui.player"
        )

        // 小米视频: 关闭广告
        ShizukuHelper.executeCommand(
            "am broadcast -a com.miui.video.SETTINGS_CHANGE " +
            "--es key show_recommend --ez value false " +
            "-p com.miui.video"
        )

        // 手机管家: 关闭推荐
        ShizukuHelper.executeCommand(
            "am broadcast -a com.miui.securitycenter.SETTINGS_CHANGE " +
            "--es key recommend_switch --ez value false " +
            "-p com.miui.securitycenter"
        )
    }

    /**
     * 获取按分类分组的广告开关
     */
    fun getAdSwitchesByCategory(): Map<String, List<MiuiAdSwitch>> {
        return getAllAdSwitches().groupBy { it.category }
    }
}
