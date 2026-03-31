package com.miakiller.app.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * 应用日志系统
 *
 * 记录所有操作日志，支持在APP内查看。
 * 方便定位闪退和功能异常问题。
 */
object AppLogger {

    private const val TAG = "MiAdKiller"
    private const val MAX_LOGS = 500

    data class LogEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val level: Level,
        val tag: String,
        val message: String,
        val throwable: String? = null
    ) {
        enum class Level { DEBUG, INFO, WARN, ERROR }

        val timeString: String
            get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))

        val fullTimeString: String
            get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
    }

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun d(tag: String, message: String) {
        Log.d(TAG, "[$tag] $message")
        addLog(LogEntry.Level.DEBUG, tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(TAG, "[$tag] $message")
        addLog(LogEntry.Level.INFO, tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(TAG, "[$tag] $message", throwable)
        addLog(LogEntry.Level.WARN, tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(TAG, "[$tag] $message", throwable)
        addLog(LogEntry.Level.ERROR, tag, message, throwable)
    }

    private fun addLog(level: LogEntry.Level, tag: String, message: String, throwable: Throwable? = null) {
        val entry = LogEntry(
            level = level,
            tag = tag,
            message = message,
            throwable = throwable?.let { "${it.javaClass.simpleName}: ${it.message}\n${it.stackTraceToString().take(500)}" }
        )
        synchronized(this) {
            val current = _logs.value.toMutableList()
            current.add(0, entry) // 最新的在前面
            if (current.size > MAX_LOGS) {
                _logs.value = current.take(MAX_LOGS)
            } else {
                _logs.value = current
            }
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }

    /**
     * 导出所有日志为文本
     */
    fun exportLogs(): String {
        return _logs.value.reversed().joinToString("\n") { entry ->
            "${entry.fullTimeString} [${entry.level}] [${entry.tag}] ${entry.message}" +
                    (entry.throwable?.let { "\n  $it" } ?: "")
        }
    }
}
