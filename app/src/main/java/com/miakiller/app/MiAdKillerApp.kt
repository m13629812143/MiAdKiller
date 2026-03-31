package com.miakiller.app

import android.app.Application
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MiAdKillerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 绕过 Android 隐藏 API 限制
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
    }

    companion object {
        lateinit var instance: MiAdKillerApp
            private set
    }
}
