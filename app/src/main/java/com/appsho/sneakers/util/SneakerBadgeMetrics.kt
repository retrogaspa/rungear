package com.appsho.sneakers.util

import android.graphics.Path
import android.graphics.RectF
import kotlin.math.roundToInt

/**
 * Badge estilo corrida (referência Strava) — retângulo vertical + círculo no topo.
 * Ancorado no canto superior direito da foto.
 */
object SneakerBadgeMetrics {

    const val SOURCE_ICON_PX = 100f
    const val REFERENCE_IMAGE_WIDTH_PX = 1080f

    /** Margens do badge em relação ao topo/direita da foto (px na ref. 1080). */
    const val MARGIN_TOP_REF = 12f
    const val MARGIN_RIGHT_REF = 12f

    /** Faixa retangular do nome (estreita, vertical). */
    const val BODY_WIDTH_REF = 38f

    /** Círculo do ícone — maior que a faixa de texto. */
    const val BULB_DIAMETER_REF = 74f

    /** Próximo ao tamanho da badge “Amazfit T-Rex 3” na referência Strava. */
    const val TEXT_SIZE_REF = 30f
    const val TEXT_PAD_VERTICAL_REF = 14f
    const val ICON_PAD_IN_BULB_REF = 8f

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
        val bodyWidth = scale(imageWidth, BODY_WIDTH_REF)
        val bodyLeft = bodyRight - bodyWidth

        val bulbDiameter = scale(imageWidth, BULB_DIAMETER_REF)
        val bulbRadius = bulbDiameter / 2f

        // Círculo no topo; retângulo do nome começa na linha de tangência inferior
        val bulbTop = marginTop
        val bodyTop = bulbTop + bulbDiameter
        val textPad = scale(imageWidth, TEXT_PAD_VERTICAL_REF)
        val bodyBottom = bodyTop + textLength + textPad * 2f

        val bulbCenterX = bodyRight - bulbRadius
        val bulbCenterY = bulbTop + bulbRadius

        val iconPad = scale(imageWidth, ICON_PAD_IN_BULB_REF)
        val iconDest = RectF(
            bulbCenterX - bulbRadius + iconPad,
            bulbTop + iconPad,
            bodyRight - iconPad,
            bodyTop - iconPad
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
            textClipRect = RectF(bodyLeft, bodyTop, bodyRight, bodyBottom)
        )
    }
}

object SneakerBadgePath {

    /**
     * Retângulo (cantos inferiores quadrados) + círculo saindo do topo, colado à direita.
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
