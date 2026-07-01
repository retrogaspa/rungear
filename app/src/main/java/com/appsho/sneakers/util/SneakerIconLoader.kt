package com.appsho.sneakers.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.appsho.sneakers.data.PredefinedSneakers
import java.io.IOException

object SneakerIconLoader {

    private const val ASSETS_ROOT = "icons"
    private val SUPPORTED_EXTENSIONS = listOf("png", "webp", "jpg", "jpeg")

    /** Ícones do catálogo são gerados em 100×100 px. */
    const val SOURCE_ICON_PX = SneakerBadgeMetrics.SOURCE_ICON_PX

    fun assetRelativePath(brandKey: String, modelKey: String, colorKey: String): String =
        "$ASSETS_ROOT/$brandKey/$modelKey/$colorKey"

    fun assetRelativePath(variantKey: String): String? {
        val parts = variantKey.split(":")
        if (parts.size != 3) return null
        return assetRelativePath(parts[0], parts[1], parts[2])
    }

    fun loadBitmapFromIconRef(context: Context, iconRef: String): Bitmap? {
        if (PredefinedSneakers.isPredefinedRef(iconRef)) {
            val variantKey = PredefinedSneakers.variantKeyFromRef(iconRef) ?: return null
            loadBitmapFromAssets(context, variantKey)?.let { return it }
            val resId = PredefinedSneakers.resolveIconResId(iconRef) ?: return null
            val drawable = ContextCompat.getDrawable(context, resId) ?: return null
            return drawableToBitmap(drawable)
        }
        return BitmapFactory.decodeFile(iconRef)?.let { normalizeIconBitmap(it) }
    }
    private fun normalizeIconBitmap(bitmap: Bitmap): Bitmap {
        val normalized = if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }
        return normalized
    }

    fun loadBitmapFromAssets(context: Context, variantKey: String): Bitmap? {
        for (key in PredefinedSneakers.assetVariantKeysToTry(variantKey)) {
            val basePath = assetRelativePath(key) ?: continue
            for (extension in SUPPORTED_EXTENSIONS) {
                val path = "$basePath.$extension"
                try {
                    context.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)?.let { bitmap ->
                            return normalizeIconBitmap(bitmap)
                        }
                    }
                } catch (_: IOException) {
                    // tenta próxima extensão / caminho legado
                }
            }
        }
        return null
    }

    fun assetUriForVariant(variantKey: String): String? {
        val basePath = assetRelativePath(variantKey) ?: return null
        return "file:///android_asset/$basePath.png"
    }

    fun resolveAssetModel(iconPath: String): Any? {
        if (!PredefinedSneakers.isPredefinedRef(iconPath)) {
            return java.io.File(iconPath)
        }
        val variantKey = PredefinedSneakers.variantKeyFromRef(iconPath) ?: return null
        val basePath = assetRelativePath(variantKey) ?: return null
        return "file:///android_asset/$basePath.png"
    }

    @DrawableRes
    fun fallbackDrawableResId(iconRef: String): Int? =
        PredefinedSneakers.resolveIconResId(iconRef)

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 512
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 512
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
