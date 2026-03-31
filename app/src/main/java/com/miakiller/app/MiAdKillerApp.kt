package com.miakiller.app

import android.app.Application
import com.miakiller.app.util.AppLogger
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MiAdKillerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 全局异常捕获
        setupCrashHandler()

        AppLogger.i("App", "MiAdKiller 启动")
        AppLogger.i("App", "设备: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        AppLogger.i("App", "系统: Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
        AppLogger.i("App", "MIUI/HyperOS: ${getSystemProperty("ro.miui.ui.version.name")} / ${getSystemProperty("ro.mi.os.version.name")}")

        // 绕过 Android 隐藏 API 限制
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            try {
                HiddenApiBypass.addHiddenApiExemptions("")
                AppLogger.i("App", "Hidden API bypass 成功")
            } catch (e: Exception) {
                AppLogger.e("App", "Hidden API bypass 失败", e)
            }
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e("CRASH", "未捕获异常 [${thread.name}]: ${throwable.message}", throwable)
            // 调用默认处理器
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            method.invoke(null, key, "unknown") as String
        } catch (e: Exception) {
            "unknown"
        }
    }

    companion object {
        lateinit var instance: MiAdKillerApp
            private set
    }
}
