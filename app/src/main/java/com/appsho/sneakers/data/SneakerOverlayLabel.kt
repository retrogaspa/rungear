package com.appsho.sneakers.data

object SneakerOverlayLabel {
    /** Marca + modelo, sem cor. */
    fun resolve(iconPath: String, fallbackName: String): String {
        PredefinedSneakers.variantKeyFromRef(iconPath)?.let { key ->
            PredefinedSneakers.getVariantByKey(key)?.let { variant ->
                return "${variant.brandName} ${variant.modelName}"
            }
        }
        val withoutColor = fallbackName.substringBefore(" — ").trim()
        return withoutColor.ifEmpty { fallbackName.trim() }
    }

    fun resolve(sneaker: Sneaker): String = resolve(sneaker.iconPath, sneaker.name)
}
