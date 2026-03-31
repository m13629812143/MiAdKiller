package com.miakiller.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miakiller.app.model.*
import com.miakiller.app.service.*
import com.miakiller.app.util.AppLogger
import com.miakiller.app.util.ShizukuHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ViewModel"

    val isShizukuAvailable = ShizukuHelper.isAvailable
    val isShizukuGranted = ShizukuHelper.isGranted

    private val _adSwitches = MutableStateFlow<List<MiuiAdSwitch>>(emptyList())
    val adSwitches: StateFlow<List<MiuiAdSwitch>> = _adSwitches.asStateFlow()

    private val _adOperationResult = MutableStateFlow<OperationResult?>(null)
    val adOperationResult: StateFlow<OperationResult?> = _adOperationResult.asStateFlow()

    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())
    val appList: StateFlow<List<AppInfo>> = _appList.asStateFlow()

    private val _frozenApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val frozenApps: StateFlow<List<AppInfo>> = _frozenApps.asStateFlow()

    private val _autoStartApps = MutableStateFlow<List<AutoStartItem>>(emptyList())
    val autoStartApps: StateFlow<List<AutoStartItem>> = _autoStartApps.asStateFlow()

    private val _permissionApps = MutableStateFlow<List<PermissionInfo>>(emptyList())
    val permissionApps: StateFlow<List<PermissionInfo>> = _permissionApps.asStateFlow()

    private val _hostsRules = MutableStateFlow<List<HostsRule>>(emptyList())
    val hostsRules: StateFlow<List<HostsRule>> = _hostsRules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    data class DashboardStats(
        val totalAdSwitches: Int = 0,
        val disabledAdSwitches: Int = 0,
        val frozenAppsCount: Int = 0,
        val blockedHostsCount: Int = 0
    )

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    init {
        AppLogger.i(TAG, "ViewModel 初始化")
        try {
            ShizukuHelper.init()
            AppLogger.i(TAG, "Shizuku 初始化完成")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Shizuku 初始化失败", e)
        }
        loadHostsRules()
    }

    override fun onCleared() {
        super.onCleared()
        try { ShizukuHelper.destroy() } catch (e: Exception) {
            AppLogger.e(TAG, "Shizuku 销毁失败", e)
        }
    }

    fun requestShizukuPermission() {
        AppLogger.i(TAG, "请求 Shizuku 权限")
        try {
            ShizukuHelper.requestPermission()
        } catch (e: Exception) {
            AppLogger.e(TAG, "请求 Shizuku 权限失败", e)
            _message.value = "请求权限失败: ${e.message}"
        }
    }

    fun clearMessage() { _message.value = null }

    // ===== 广告管理 =====

    fun loadAdSwitches() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                AppLogger.i(TAG, "加载广告开关列表")
                val switches = MiuiAdService.getAllAdSwitches()
                AppLogger.i(TAG, "共 ${switches.size} 个广告开关")

                if (ShizukuHelper.isGranted.value) {
                    val refreshed = MiuiAdService.refreshSwitchStates(switches)
                    _adSwitches.value = refreshed
                    val disabled = refreshed.count { it.isDisabled }
                    AppLogger.i(TAG, "已关闭 $disabled/${switches.size} 个")
                } else {
                    _adSwitches.value = switches
                    AppLogger.w(TAG, "Shizuku未授权，无法读取开关状态")
                }
                updateStats()
            } catch (e: Exception) {
                AppLogger.e(TAG, "加载广告开关失败", e)
                _message.value = "加载广告开关失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun oneClickDisableAds() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                AppLogger.i(TAG, "一键关闭广告开始")
                val result = MiuiAdService.disableAllAds()
                _adOperationResult.value = result
                _message.value = result.message
                AppLogger.i(TAG, "一键关闭完成: ${result.message}")
                loadAdSwitches()
            } catch (e: Exception) {
                AppLogger.e(TAG, "一键关闭失败", e)
                _message.value = "一键关闭失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAdSwitch(switch: MiuiAdSwitch) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (switch.isDisabled) MiuiAdService.enableAdSwitch(switch)
                else MiuiAdService.disableAdSwitch(switch)
                loadAdSwitches()
            } catch (e: Exception) {
                AppLogger.e(TAG, "切换广告开关失败: ${switch.name}", e)
                _message.value = "操作失败: ${e.message}"
            }
        }
    }

    // ===== 冻结管理 =====

    fun loadApps(includeSystem: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                AppLogger.i(TAG, "加载应用列表")
                _appList.value = AppFreezeService.getInstalledApps(includeSystem)
                _frozenApps.value = _appList.value.filter { it.isFrozen }
                AppLogger.i(TAG, "应用列表加载完成: ${_appList.value.size} 个, 冻结 ${_frozenApps.value.size} 个")
                updateStats()
            } catch (e: Exception) {
                AppLogger.e(TAG, "加载应用列表失败", e)
                _message.value = "加载应用列表失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun freezeApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = AppFreezeService.freezeApp(packageName)
                _message.value = result.message
                loadApps()
            } catch (e: Exception) {
                AppLogger.e(TAG, "冻结失败: $packageName", e)
                _message.value = "冻结失败: ${e.message}"
            }
        }
    }

    fun unfreezeApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = AppFreezeService.unfreezeApp(packageName)
                _message.value = result.message
                loadApps()
            } catch (e: Exception) {
                AppLogger.e(TAG, "解冻失败: $packageName", e)
                _message.value = "解冻失败: ${e.message}"
            }
        }
    }

    fun freezeSuggestedApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val suggested = AppFreezeService.getSuggestedFreezeApps()
                val result = AppFreezeService.freezeApps(suggested)
                _message.value = result.message
                loadApps()
            } catch (e: Exception) {
                AppLogger.e(TAG, "批量冻结失败", e)
                _message.value = "批量冻结失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ===== 自启动管理 =====

    fun loadAutoStartApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                AppLogger.i(TAG, "加载自启动列表")
                _autoStartApps.value = AutoStartService.getAutoStartAppsViaShell()
            } catch (e: Exception) {
                AppLogger.e(TAG, "加载自启动列表失败", e)
                _message.value = "加载自启动列表失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAutoStart(item: AutoStartItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (item.isBlocked) AutoStartService.allowAutoStart(item.packageName, item.receivers)
                else AutoStartService.blockAutoStart(item.packageName, item.receivers)
                loadAutoStartApps()
            } catch (e: Exception) {
                AppLogger.e(TAG, "切换自启动失败: ${item.packageName}", e)
                _message.value = "操作失败: ${e.message}"
            }
        }
    }

    // ===== 权限管理 =====

    fun loadPermissionApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                AppLogger.i(TAG, "扫描应用权限")
                _permissionApps.value = PermissionService.getAppsWithDangerousPermissions()
            } catch (e: Exception) {
                AppLogger.e(TAG, "权限扫描失败", e)
                _message.value = "加载权限信息失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun togglePermission(packageName: String, permission: AppPermission) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = if (permission.isGranted)
                    PermissionService.revokePermission(packageName, permission.name)
                else
                    PermissionService.grantPermission(packageName, permission.name)
                _message.value = result.message
                loadPermissionApps()
            } catch (e: Exception) {
                AppLogger.e(TAG, "权限操作失败", e)
                _message.value = "操作失败: ${e.message}"
            }
        }
    }

    // ===== Hosts管理 =====

    fun loadHostsRules() {
        _hostsRules.value = HostsService.getMiuiAdDomains()
    }

    fun toggleHostsRule(rule: HostsRule) {
        _hostsRules.value = _hostsRules.value.map {
            if (it.domain == rule.domain) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun applyHostsRules() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = HostsService.applyHostsRules(_hostsRules.value)
                _message.value = result.message
                if (result.details.isNotEmpty()) {
                    _adOperationResult.value = OperationResult(result.success, result.message, result.details)
                }
                updateStats()
            } catch (e: Exception) {
                AppLogger.e(TAG, "应用Hosts规则失败", e)
                _message.value = "应用Hosts规则失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreHosts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = HostsService.restoreHosts()
                _message.value = result.message
            } catch (e: Exception) {
                _message.value = "恢复失败: ${e.message}"
            }
        }
    }

    private fun updateStats() {
        _stats.value = DashboardStats(
            totalAdSwitches = _adSwitches.value.size,
            disabledAdSwitches = _adSwitches.value.count { it.isDisabled },
            frozenAppsCount = _frozenApps.value.size,
            blockedHostsCount = _hostsRules.value.count { it.isEnabled }
        )
    }
}
