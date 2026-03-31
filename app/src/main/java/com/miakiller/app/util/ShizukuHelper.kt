package com.miakiller.app.util

import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

/**
 * Shizuku 权限管理助手
 *
 * Shizuku 是一个免Root方案，通过ADB授权获取shell级别权限。
 * 用户只需要：
 * 1. 安装 Shizuku APP
 * 2. 通过电脑ADB执行: adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
 *    或者在开发者选项中开启无线调试后在Shizuku APP中直接启动
 * 3. 然后本APP就可以获得shell权限来执行系统级操作
 */
object ShizukuHelper {

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isGranted = MutableStateFlow(false)
    val isGranted: StateFlow<Boolean> = _isGranted.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        _isAvailable.value = true
        checkPermission()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _isAvailable.value = false
        _isGranted.value = false
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == PERMISSION_REQUEST_CODE) {
                _isGranted.value = grantResult == PackageManager.PERMISSION_GRANTED
            }
        }

    const val PERMISSION_REQUEST_CODE = 1001

    fun init() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }

    fun checkPermission() {
        try {
            if (Shizuku.isPreV11()) {
                _isGranted.value = false
                return
            }
            _isGranted.value = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            _isGranted.value = false
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.isPreV11()) return
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
            } else {
                _isGranted.value = true
            }
        } catch (e: Exception) {
            _isGranted.value = false
        }
    }

    /**
     * 通过Shizuku执行Shell命令
     * 这是核心方法 - 获取shell权限后可以执行各种系统级操作
     */
    fun executeCommand(command: String): CommandResult {
        return try {
            if (!_isGranted.value) {
                return CommandResult(false, "", "Shizuku权限未授予")
            }

            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", command),
                null,
                null
            )

            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            CommandResult(
                success = exitCode == 0,
                output = output.trim(),
                error = error.trim()
            )
        } catch (e: Exception) {
            CommandResult(false, "", e.message ?: "执行命令失败")
        }
    }

    /**
     * 批量执行Shell命令
     */
    fun executeCommands(commands: List<String>): List<CommandResult> {
        return commands.map { executeCommand(it) }
    }
}

data class CommandResult(
    val success: Boolean,
    val output: String,
    val error: String
)
