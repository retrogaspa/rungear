package com.appsho.sneakers.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageOverlayHelper {

    private const val RIGHT_INSET_RATIO = 0f

    private val barColor = android.graphics.Color.rgb(0, 0, 0)
    private val textColor = android.graphics.Color.WHITE

    fun overlaySneakerBadgeOnImage(
        baseBitmap: Bitmap,
        iconBitmap: Bitmap,
        brandModelLabel: String
    ): Bitmap {
        val result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val imageWidth = baseBitmap.width.toFloat()
        val imageHeight = baseBitmap.height.toFloat()

        val textSizePx = SneakerBadgeMetrics.textSizePx(imageWidth)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            this.textSize = textSizePx
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val label = brandModelLabel.trim()
        val textLength = textPaint.measureText(label)

        val layout = SneakerBadgeMetrics.compute(
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            textLength = textLength,
            textSizePx = textSizePx
        )

        val barLeft = layout.barLeft - imageWidth * RIGHT_INSET_RATIO

        drawPillBar(
            canvas = canvas,
            left = barLeft,
            top = layout.barTop,
            width = layout.barWidth,
            totalHeight = layout.totalBarHeight,
            topSectionHeight = layout.topSectionHeight
        )

        SneakerIconDraw.drawContained(canvas, iconBitmap, layout.iconDestRect)

        val textCenterX = barLeft + layout.barWidth / 2f
        val textCenterY = layout.barTop + layout.topSectionHeight + layout.textSectionHeight / 2f

        canvas.save()
        canvas.translate(textCenterX, textCenterY)
        canvas.rotate(-90f)
        canvas.drawText(label, -textLength / 2f, textSizePx / 3f, textPaint)
        canvas.restore()

        return result
    }

    private fun drawPillBar(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        totalHeight: Float,
        topSectionHeight: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = barColor
            style = Paint.Style.FILL
        }

        val path = Path()
        val topRadius = width / 2f

        val topRect = RectF(left, top, left + width, top + topSectionHeight)
        path.addRoundRect(topRect, topRadius, topRadius, Path.Direction.CW)

        val bodyWidth = width * SneakerBadgeMetrics.BODY_WIDTH_RATIO
        val bodyLeft = left + (width - bodyWidth) / 2f
        val bodyTop = top + topSectionHeight - topRadius * 0.12f
        val bodyRect = RectF(bodyLeft, bodyTop, bodyLeft + bodyWidth, top + totalHeight)
        path.addRoundRect(bodyRect, bodyWidth / 2f, width / 2f, Path.Direction.CW)

        canvas.drawPath(path, paint)
    }

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }

    fun loadBitmapFromPath(path: String): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }

    fun loadBitmapFromIconRef(context: Context, iconRef: String): Bitmap? =
        SneakerIconLoader.loadBitmapFromIconRef(context, iconRef)

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val displayName = "${fileName}_${System.currentTimeMillis()}.jpg"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/RunGear"
                )
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return null

            context.contentResolver.openOutputStream(uri)?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
            }
            uri
        } else {
            @Suppress("DEPRECATION")
            val picturesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            val appDir = File(picturesDir, "RunGear").apply { mkdirs() }
            val file = File(appDir, displayName)
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
            }
            Uri.fromFile(file)
        }
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val cacheDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(cacheDir, "share_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
