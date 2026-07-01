package com.appsho.sneakers.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appsho.sneakers.data.PredefinedSneakers
import com.appsho.sneakers.util.SneakerIconLoader
import java.io.File

@Composable
fun SneakerIcon(
    iconPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current

    if (PredefinedSneakers.isPredefinedRef(iconPath)) {
        val bitmap = remember(iconPath) {
            SneakerIconLoader.loadBitmapFromIconRef(context, iconPath)
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
            return
        }

        val resId = SneakerIconLoader.fallbackDrawableResId(iconPath)
        if (resId != null) {
            Image(
                painter = painterResource(resId),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
            return
        }
    }

    AsyncImage(
        model = File(iconPath),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/** Slot quadrado proporcional ao ícone 100×100 na barra (útil em prévias da UI). */
val CatalogIconPreviewSize: Dp = 100.dp
