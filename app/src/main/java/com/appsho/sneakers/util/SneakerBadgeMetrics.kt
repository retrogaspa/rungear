package com.appsho.sneakers.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Métricas da barra lateral baseadas em ícones fonte de [SOURCE_ICON_PX]×[SOURCE_ICON_PX]
 * numa foto de referência de [REFERENCE_IMAGE_WIDTH_PX] px de largura.
 */
object SneakerBadgeMetrics {

    const val SOURCE_ICON_PX = 100f
    const val REFERENCE_IMAGE_WIDTH_PX = 1080f

    /** Margem interna ao redor do ícone no topo da barra (px na foto de referência). */
    const val ICON_PAD_H_REF = 16f
    const val ICON_PAD_TOP_REF = 14f
    const val ICON_PAD_BOTTOM_REF = 10f

    const val TEXT_SIZE_REF = 22f
    const val TEXT_SECTION_PAD_REF = 20f
    const val BODY_WIDTH_RATIO = 0.68f

    fun scale(imageWidth: Float, refPx: Float): Float =
        imageWidth * refPx / REFERENCE_IMAGE_WIDTH_PX

    fun iconSlotSize(imageWidth: Float): Int =
        scale(imageWidth, SOURCE_ICON_PX).roundToInt().coerceAtLeast(1)

    fun topSectionHeight(imageWidth: Float, iconSlot: Int): Float =
        iconSlot + scale(imageWidth, ICON_PAD_TOP_REF) + scale(imageWidth, ICON_PAD_BOTTOM_REF)

    fun topBulbWidth(imageWidth: Float, iconSlot: Int): Float =
        iconSlot + scale(imageWidth, ICON_PAD_H_REF) * 2f

    fun textSizePx(imageWidth: Float): Float = scale(imageWidth, TEXT_SIZE_REF)

    data class Layout(
        val iconSlot: Int,
        val topSectionHeight: Float,
        val textSectionHeight: Float,
        val barWidth: Float,
        val totalBarHeight: Float,
        val barLeft: Float,
        val barTop: Float,
        val iconDestRect: RectF
    )

    fun compute(
        imageWidth: Float,
        imageHeight: Float,
        textLength: Float,
        textSizePx: Float
    ): Layout {
        val innerPad = scale(imageWidth, 8f)
        val iconSlot = iconSlotSize(imageWidth)
        val topH = topSectionHeight(imageWidth, iconSlot)
        val textPad = scale(imageWidth, TEXT_SECTION_PAD_REF)
        val textH = textLength + textPad * 2f
        val totalH = topH + textH

        val topW = topBulbWidth(imageWidth, iconSlot)
        val textBarW = textSizePx * 1.25f + scale(imageWidth, 12f)
        val barW = max(topW, textBarW)

        val barLeft = imageWidth - barW
        val barTop = ((imageHeight - totalH) / 2f).coerceAtLeast(innerPad)

        val iconPadH = scale(imageWidth, ICON_PAD_H_REF)
        val iconPadTop = scale(imageWidth, ICON_PAD_TOP_REF)
        val iconDest = RectF(
            barLeft + iconPadH,
            barTop + iconPadTop,
            barLeft + iconPadH + iconSlot,
            barTop + iconPadTop + iconSlot
        )

        return Layout(
            iconSlot = iconSlot,
            topSectionHeight = topH,
            textSectionHeight = textH,
            barWidth = barW,
            totalBarHeight = totalH,
            barLeft = barLeft,
            barTop = barTop,
            iconDestRect = iconDest
        )
    }
}

object SneakerIconDraw {

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    /**
     * Desenha o bitmap inteiro dentro do retângulo destino (fit center, sem distorcer).
     */
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
