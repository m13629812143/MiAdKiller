package com.miakiller.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miakiller.app.model.*
import com.miakiller.app.service.*
import com.miakiller.app.util.ShizukuHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // ===== Shizuku 状态 =====
    val isShizukuAvailable = ShizukuHelper.isAvailable
    val isShizukuGranted = ShizukuHelper.isGranted

    // ===== 广告开关 =====
    private val _adSwitches = MutableStateFlow<List<MiuiAdSwitch>>(emptyList())
    val adSwitches: StateFlow<List<MiuiAdSwitch>> = _adSwitches.asStateFlow()

    private val _adOperationResult = MutableStateFlow<OperationResult?>(null)
    val adOperationResult: StateFlow<OperationResult?> = _adOperationResult.asStateFlow()

    // ===== 应用列表 =====
    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())
    val appList: StateFlow<List<AppInfo>> = _appList.asStateFlow()

    private val _frozenApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val frozenApps: StateFlow<List<AppInfo>> = _frozenApps.asStateFlow()

    // ===== 自启动 =====
    private val _autoStartApps = MutableStateFlow<List<AutoStartItem>>(emptyList())
    val autoStartApps: StateFlow<List<AutoStartItem>> = _autoStartApps.asStateFlow()

    // ===== 权限 =====
    private val _permissionApps = MutableStateFlow<List<PermissionInfo>>(emptyList())
    val permissionApps: StateFlow<List<PermissionInfo>> = _permissionApps.asStateFlow()

    // ===== Hosts =====
    private val _hostsRules = MutableStateFlow<List<HostsRule>>(emptyList())
    val hostsRules: StateFlow<List<HostsRule>> = _hostsRules.asStateFlow()

    // ===== 通用状态 =====
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // ===== 统计数据 =====
    data class DashboardStats(
        val totalAdSwitches: Int = 0,
        val disabledAdSwitches: Int = 0,
        val frozenAppsCount: Int = 0,
        val blockedHostsCount: Int = 0
    )

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    init {
        ShizukuHelper.init()
        loadHostsRules()
    }

    override fun onCleared() {
        super.onCleared()
        ShizukuHelper.destroy()
    }

    fun requestShizukuPermission() {
        ShizukuHelper.requestPermission()
    }

    fun clearMessage() {
        _message.value = null
    }

    // ===== 广告管理 =====

    fun loadAdSwitches() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val switches = MiuiAdService.getAllAdSwitches()
                val refreshed = MiuiAdService.refreshSwitchStates(switches)
                _adSwitches.value = refreshed
                updateStats()
            } catch (e: Exception) {
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
                val result = MiuiAdService.disableAllAds()
                _adOperationResult.value = result
                _message.value = result.message
                loadAdSwitches() // 刷新状态
            } catch (e: Exception) {
                _message.value = "一键关闭失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAdSwitch(switch: MiuiAdSwitch) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (switch.isDisabled) {
                    MiuiAdService.enableAdSwitch(switch)
                } else {
                    MiuiAdService.disableAdSwitch(switch)
                }
                loadAdSwitches() // 刷新
            } catch (e: Exception) {
                _message.value = "操作失败: ${e.message}"
            }
        }
    }

    // ===== 冻结管理 =====

    fun loadApps(includeSystem: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                _appList.value = AppFreezeService.getInstalledApps(includeSystem)
                _frozenApps.value = AppFreezeService.getFrozenApps()
                updateStats()
            } catch (e: Exception) {
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
                _autoStartApps.value = AutoStartService.getAutoStartAppsViaShell()
            } catch (e: Exception) {
                _message.value = "加载自启动列表失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAutoStart(item: AutoStartItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (item.isBlocked) {
                    AutoStartService.allowAutoStart(item.packageName, item.receivers)
                } else {
                    AutoStartService.blockAutoStart(item.packageName, item.receivers)
                }
                loadAutoStartApps()
            } catch (e: Exception) {
                _message.value = "操作失败: ${e.message}"
            }
        }
    }

    // ===== 权限管理 =====

    fun loadPermissionApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                _permissionApps.value = PermissionService.getAppsWithDangerousPermissions()
            } catch (e: Exception) {
                _message.value = "加载权限信息失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun togglePermission(packageName: String, permission: AppPermission) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = if (permission.isGranted) {
                    PermissionService.revokePermission(packageName, permission.name)
                } else {
                    PermissionService.grantPermission(packageName, permission.name)
                }
                _message.value = result.message
                loadPermissionApps()
            } catch (e: Exception) {
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
                    _adOperationResult.value = OperationResult(
                        result.success, result.message, result.details
                    )
                }
                updateStats()
            } catch (e: Exception) {
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

    // ===== 统计 =====

    private fun updateStats() {
        _stats.value = DashboardStats(
            totalAdSwitches = _adSwitches.value.size,
            disabledAdSwitches = _adSwitches.value.count { it.isDisabled },
            frozenAppsCount = _frozenApps.value.size,
            blockedHostsCount = _hostsRules.value.count { it.isEnabled }
        )
    }
}
