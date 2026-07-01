package com.appsho.sneakers.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.min

object SneakerIconDraw {

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    fun drawContained(canvas: Canvas, bitmap: Bitmap, dest: RectF) {
        val srcW = bitmap.width.toFloat()
        val srcH = bitmap.height.toFloat()
        if (srcW <= 0f || srcH <= 0f) return

        val scale = min(dest.width() / srcW, dest.height() / srcH)
        val drawnW = srcW * scale
        val drawnH = srcH * scale
        val left = dest.left + (dest.width() - drawnW) / 2f
        val top = dest.top + (dest.height() - drawnH) / 2f
        val fitted = RectF(left, top, left + drawnW, top + drawnH)
        canvas.drawBitmap(bitmap, null, fitted, bitmapPaint)
    }
}
