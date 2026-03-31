# MiAdKiller - 小米广告管理工具

免Root的小米/MIUI/HyperOS广告管理工具，基于Shizuku实现shell级权限。

## 功能

### 1. 一键关闭广告
- 自动关闭 MIUI/HyperOS 系统中 25+ 个已知广告开关
- 覆盖：系统广告、应用商店、浏览器、安全中心、桌面、锁屏、天气、日历等
- 禁用 MSA (MIUI System Ads) 广告服务组件

### 2. 应用冻结
- 冻结不需要的预装应用（不会卸载，随时可解冻）
- 智能冻结：一键冻结建议的广告/臃肿应用
- 支持搜索、筛选系统应用

### 3. 自启动管理
- 检测所有注册了开机自启动的应用
- 一键阻止/允许应用自启动
- 加快开机速度，减少内存占用

### 4. 权限管理
- 查看所有应用的危险权限（相机、位置、联系人等）
- 单独撤销/授予权限
- 按已授权权限数排序，快速发现权限滥用

### 5. Hosts广告屏蔽
- 30+ 条小米广告域名屏蔽规则
- 覆盖 MSA、追踪、推送、第三方SDK等
- 支持导出规则供其他广告屏蔽工具使用

## 技术架构

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **权限方案**: Shizuku (免Root，通过ADB授权)
- **最低版本**: Android 9 (API 28)
- **目标版本**: Android 14 (API 34)

## 使用前提

### 安装Shizuku
1. 从 [Shizuku官网](https://shizuku.rikka.app/) 或应用商店安装 Shizuku
2. 启动方式（二选一）：
   - **无线调试（推荐）**: 在开发者选项中开启无线调试，在Shizuku中点击"通过无线调试启动"
   - **ADB连接**: 连接电脑，执行：
     ```
     adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
     ```

### 使用步骤
1. 确保Shizuku正在运行（通知栏会有Shizuku图标）
2. 打开MiAdKiller
3. 授予Shizuku权限
4. 使用各项功能

## 项目结构

```
app/src/main/java/com/miakiller/app/
├── MiAdKillerApp.kt          # Application类
├── MainActivity.kt            # 主Activity + 导航
├── model/
│   └── Models.kt              # 数据模型
├── service/
│   ├── MiuiAdService.kt       # MIUI广告开关管理
│   ├── AppFreezeService.kt    # 应用冻结/解冻
│   ├── AutoStartService.kt    # 自启动管理
│   ├── PermissionService.kt   # 权限管理
│   └── HostsService.kt        # Hosts域名屏蔽
├── util/
│   └── ShizukuHelper.kt       # Shizuku权限管理和命令执行
├── viewmodel/
│   └── MainViewModel.kt       # 主ViewModel
└── ui/
    ├── theme/
    │   └── Theme.kt            # Material 3 主题
    └── screens/
        ├── HomeScreen.kt       # 首页/仪表盘
        ├── AdSwitchScreen.kt   # 广告开关管理页
        ├── FreezeScreen.kt     # 冻结管理页
        ├── AutoStartScreen.kt  # 自启动管理页
        ├── PermissionScreen.kt # 权限管理页
        └── HostsScreen.kt     # Hosts屏蔽页
```

## 构建

```bash
# Debug构建
./gradlew assembleDebug

# Release构建
./gradlew assembleRelease
```

## 适用设备

- 小米15 / 小米15 Pro / 小米15 Ultra (HyperOS 2.0)
- 其他运行 MIUI 12-14 或 HyperOS 的小米设备
- 部分功能（冻结、权限管理）适用于所有Android设备

## 注意事项

- 冻结系统核心应用可能导致系统不稳定，请谨慎操作
- Hosts屏蔽在非Root设备上可能需要借助VPN类广告屏蔽工具
- Shizuku在每次重启后需要重新启动（通过无线调试启动可自动恢复）
- 广告开关的key可能随MIUI/HyperOS版本更新而变化

## License

MIT
