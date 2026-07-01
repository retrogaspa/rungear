package com.appsho.sneakers.util

import android.graphics.Path
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Badge estilo corrida (referência Strava) — gota no topo + retângulo vertical.
 * Colado à direita; pequena margem no topo.
 */
object SneakerBadgeMetrics {

    const val SOURCE_ICON_PX = 100f
    const val REFERENCE_IMAGE_WIDTH_PX = 1080f

    /** Margem do topo (~14 px numa foto de 1080 px de largura). */
    const val MARGIN_TOP_REF = 14f
    const val MARGIN_RIGHT_REF = 0f

    const val BODY_WIDTH_MIN_REF = 70f
    const val BULB_DIAMETER_REF = 92f

    /**
     * Quanto o retângulo sobe dentro do círculo (0–1 do diâmetro).
     * Cria a “gota” contínua em vez de bola separada.
     */
    const val BULB_STEM_OVERLAP_RATIO = 0.40f

    const val TEXT_SIZE_REF = 30f
    /** Padding do nome (+60% em relação aos valores anteriores). */
    const val TEXT_PAD_HORIZONTAL_REF = 19f
    const val TEXT_PAD_VERTICAL_REF = 35f
    /** Espaço extra entre a zona do ícone e o início do texto. */
    const val TEXT_TOP_GAP_REF = 30f
    const val ICON_PAD_IN_BULB_REF = 4f
    /** Ícone 15% maior dentro da gota. */
    const val ICON_SCALE_IN_BULB = 1.15f

    fun scale(imageWidth: Float, refPx: Float): Float =
        imageWidth * refPx / REFERENCE_IMAGE_WIDTH_PX

    fun iconSlotSize(imageWidth: Float): Int =
        scale(imageWidth, SOURCE_ICON_PX).roundToInt().coerceAtLeast(1)

    fun textSizePx(imageWidth: Float): Float = scale(imageWidth, TEXT_SIZE_REF)

    data class Layout(
        val bodyLeft: Float,
        val bodyRight: Float,
        val bodyTop: Float,
        val bodyBottom: Float,
        val bulbCenterX: Float,
        val bulbCenterY: Float,
        val bulbRadius: Float,
        val bulbTop: Float,
        val iconDestRect: RectF,
        val textClipRect: RectF
    )

    fun compute(imageWidth: Float, textLength: Float, textSizePx: Float): Layout {
        val marginTop = scale(imageWidth, MARGIN_TOP_REF)
        val marginRight = scale(imageWidth, MARGIN_RIGHT_REF)

        val bodyRight = imageWidth - marginRight

        val textHorizPad = scale(imageWidth, TEXT_PAD_HORIZONTAL_REF)
        val textVertPad = scale(imageWidth, TEXT_PAD_VERTICAL_REF)
        val textTopGap = scale(imageWidth, TEXT_TOP_GAP_REF)
        val bodyWidth = max(
            scale(imageWidth, BODY_WIDTH_MIN_REF),
            textSizePx + textHorizPad * 2f
        )
        val bodyLeft = bodyRight - bodyWidth

        val bulbDiameter = scale(imageWidth, BULB_DIAMETER_REF)
        val bulbRadius = bulbDiameter / 2f
        val bulbTop = marginTop

        // Retângulo sobe dentro do círculo → forma de gota contínua
        val stemOverlap = bulbDiameter * BULB_STEM_OVERLAP_RATIO
        val bodyTop = bulbTop + bulbDiameter - stemOverlap
        val bodyBottom = bodyTop + textTopGap + textLength + textVertPad * 2f

        val bulbCenterX = bodyRight - bulbRadius
        val bulbCenterY = bulbTop + bulbRadius

        val iconPad = scale(imageWidth, ICON_PAD_IN_BULB_REF)
        val iconDest = RectF(
            bulbCenterX - bulbRadius + iconPad,
            bulbTop + iconPad,
            bodyRight - iconPad,
            bulbTop + bulbDiameter - iconPad
        )

        val textClipRect = RectF(
            bodyLeft + textHorizPad,
            bodyTop + textTopGap + textVertPad,
            bodyRight - textHorizPad,
            bodyBottom - textVertPad
        )

        return Layout(
            bodyLeft = bodyLeft,
            bodyRight = bodyRight,
            bodyTop = bodyTop,
            bodyBottom = bodyBottom,
            bulbCenterX = bulbCenterX,
            bulbCenterY = bulbCenterY,
            bulbRadius = bulbRadius,
            bulbTop = bulbTop,
            iconDestRect = iconDest,
            textClipRect = textClipRect
        )
    }
}

object SneakerBadgePath {

    /**
     * Gota contínua: retângulo funde com círculo (retângulo entra no círculo).
     */
    fun build(layout: SneakerBadgeMetrics.Layout): Path {
        val bodyPath = Path().apply {
            addRect(
                RectF(layout.bodyLeft, layout.bodyTop, layout.bodyRight, layout.bodyBottom),
                Path.Direction.CW
            )
        }
        val bulbPath = Path().apply {
            addCircle(layout.bulbCenterX, layout.bulbCenterY, layout.bulbRadius, Path.Direction.CW)
        }
        bodyPath.op(bulbPath, Path.Op.UNION)
        return bodyPath
    }
}
