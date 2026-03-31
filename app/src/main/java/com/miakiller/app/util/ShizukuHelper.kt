package com.miakiller.app.util

import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Shizuku 权限管理助手
 */
object ShizukuHelper {

    private const val TAG = "Shizuku"

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isGranted = MutableStateFlow(false)
    val isGranted: StateFlow<Boolean> = _isGranted.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        _isAvailable.value = true
        AppLogger.i(TAG, "Binder received, Shizuku可用")
        checkPermission()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _isAvailable.value = false
        _isGranted.value = false
        AppLogger.w(TAG, "Binder dead, Shizuku不可用")
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == PERMISSION_REQUEST_CODE) {
                _isGranted.value = grantResult == PackageManager.PERMISSION_GRANTED
                AppLogger.i(TAG, "权限回调: ${if (_isGranted.value) "已授予" else "被拒绝"}")
            }
        }

    const val PERMISSION_REQUEST_CODE = 1001

    fun init() {
        AppLogger.i(TAG, "初始化 Shizuku 监听器")
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            AppLogger.e(TAG, "初始化失败", e)
        }
    }

    fun destroy() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            AppLogger.e(TAG, "销毁失败", e)
        }
    }

    fun checkPermission() {
        try {
            if (Shizuku.isPreV11()) {
                _isGranted.value = false
                AppLogger.w(TAG, "Shizuku版本过低 (pre-v11)")
                return
            }
            _isGranted.value = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            AppLogger.i(TAG, "权限检查: ${if (_isGranted.value) "已授予" else "未授予"}")
        } catch (e: Exception) {
            _isGranted.value = false
            AppLogger.e(TAG, "权限检查异常", e)
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.isPreV11()) {
                AppLogger.w(TAG, "无法请求权限: Shizuku版本过低")
                return
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                AppLogger.i(TAG, "请求 Shizuku 权限...")
                Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
            } else {
                _isGranted.value = true
                AppLogger.i(TAG, "权限已存在，无需请求")
            }
        } catch (e: Exception) {
            _isGranted.value = false
            AppLogger.e(TAG, "请求权限失败", e)
        }
    }

    /**
     * 通过Shizuku执行Shell命令
     */
    fun executeCommand(command: String): CommandResult {
        if (!_isGranted.value) {
            AppLogger.w(TAG, "命令跳过(未授权): $command")
            return CommandResult(false, "", "Shizuku权限未授予")
        }

        return try {
            // 方案1: 通过反射调用 Shizuku.newProcess
            val shizukuClass = Shizuku::class.java
            val method = shizukuClass.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true

            val process = method.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val exitCode = process.waitFor()

            val result = CommandResult(exitCode == 0, output.trim(), error.trim())
            if (!result.success && result.error.isNotEmpty()) {
                AppLogger.d(TAG, "CMD[$exitCode]: $command -> ${result.error.take(100)}")
            }
            result
        } catch (e: Exception) {
            AppLogger.d(TAG, "Shizuku.newProcess失败，尝试fallback: ${e.message}")
            executeCommandFallback(command)
        }
    }

    private fun executeCommandFallback(command: String): CommandResult {
        return try {
            val processBuilder = ProcessBuilder("sh", "-c", command)
            processBuilder.redirectErrorStream(false)
            val process = processBuilder.start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val exitCode = process.waitFor()

            CommandResult(exitCode == 0, output.trim(), error.trim())
        } catch (e: Exception) {
            AppLogger.e(TAG, "Fallback也失败: $command", e)
            CommandResult(false, "", e.message ?: "执行命令失败")
        }
    }

    fun executeCommands(commands: List<String>): List<CommandResult> {
        return commands.map { executeCommand(it) }
    }
}

data class CommandResult(
    val success: Boolean,
    val output: String,
    val error: String
)
