package com.miakiller.app.util

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * 安全地将 Drawable 转为 Bitmap
 *
 * 各种 Drawable 类型（包括 AdaptiveIconDrawable）都能正确处理，
 * 不会因为某些系统应用的特殊图标而崩溃。
 */
object IconHelper {

    private const val ICON_SIZE = 48

    fun loadAppIcon(pm: PackageManager, packageName: String): Bitmap? {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val drawable = pm.getApplicationIcon(appInfo)
            drawableToBitmap(drawable)
        } catch (e: Exception) {
            AppLogger.w("IconHelper", "加载图标失败: $packageName", e)
            null
        }
    }

    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        return try {
            when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> {
                    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else ICON_SIZE
                    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else ICON_SIZE
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                }
            }
        } catch (e: Exception) {
            AppLogger.w("IconHelper", "Drawable转Bitmap失败", e)
            null
        }
    }
}
