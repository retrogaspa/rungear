package com.appsho.sneakers.data

import androidx.annotation.DrawableRes
import com.appsho.sneakers.R

data class PredefinedColorVariant(
    val key: String,
    val name: String,
    @DrawableRes val iconResId: Int
)

data class PredefinedModel(
    val key: String,
    val name: String,
    val colors: List<PredefinedColorVariant>
)

data class PredefinedBrand(
    val key: String,
    val name: String,
    val models: List<PredefinedModel>
)

data class PredefinedVariant(
    val brandKey: String,
    val brandName: String,
    val modelKey: String,
    val modelName: String,
    val colorKey: String,
    val colorName: String,
    @DrawableRes val iconResId: Int
) {
    val variantKey: String get() = "$brandKey:$modelKey:$colorKey"
    val displayName: String get() = "$brandName $modelName — $colorName"
}

object PredefinedSneakers {
    const val ICON_PREFIX = "predefined:"

    val brands: List<PredefinedBrand> = listOf(
        PredefinedBrand(
            key = "fila",
            name = "Fila",
            models = listOf(
                PredefinedModel(
                    key = "racer_carbon_2",
                    name = "Racer Carbon 2",
                    colors = listOf(
                        PredefinedColorVariant(
                            key = "verde",
                            name = "Verde",
                            iconResId = R.drawable.sneaker_fila_racer_carbon_2_verde
                        )
                    )
                )
            )
        ),
        PredefinedBrand(
            key = "olympikus",
            name = "Olympikus",
            models = listOf(
                PredefinedModel(
                    key = "corre_5",
                    name = "Corre 5",
                    colors = listOf(
                        PredefinedColorVariant(
                            key = "preto_amarelo",
                            name = "Preto e Amarelo",
                            iconResId = R.drawable.sneaker_olympikus_corre_5_preto_amarelo
                        )
                    )
                )
            )
        ),
        PredefinedBrand(
            key = "chunta",
            name = "Chunta",
            models = listOf(
                PredefinedModel(
                    key = "sn",
                    name = "SN",
                    colors = listOf(
                        PredefinedColorVariant(
                            key = "beige",
                            name = "Beige",
                            iconResId = R.drawable.sneaker_chunta_sn_beige
                        )
                    )
                )
            )
        ),
        PredefinedBrand(
            key = "asics",
            name = "Asics",
            models = listOf(
                PredefinedModel(
                    key = "dynablast_5",
                    name = "Dynablast 5",
                    colors = listOf(
                        PredefinedColorVariant(
                            key = "azul_claro",
                            name = "Azul Claro",
                            iconResId = R.drawable.sneaker_asics_dynablast_5_azul_claro
                        )
                    )
                )
            )
        )
    )

    private val allVariants: List<PredefinedVariant> by lazy {
        brands.flatMap { brand ->
            brand.models.flatMap { model ->
                model.colors.map { color ->
                    PredefinedVariant(
                        brandKey = brand.key,
                        brandName = brand.name,
                        modelKey = model.key,
                        modelName = model.name,
                        colorKey = color.key,
                        colorName = color.name,
                        iconResId = color.iconResId
                    )
                }
            }
        }
    }

    fun getBrand(brandKey: String): PredefinedBrand? =
        brands.find { it.key == brandKey }

    fun getModel(brandKey: String, modelKey: String): PredefinedModel? =
        getBrand(brandKey)?.models?.find { it.key == modelKey }

    fun getVariant(brandKey: String, modelKey: String, colorKey: String): PredefinedVariant? =
        allVariants.find {
            it.brandKey == brandKey && it.modelKey == modelKey && it.colorKey == colorKey
        }

    fun getVariantByKey(variantKey: String): PredefinedVariant? =
        allVariants.find { it.variantKey == variantKey }

    fun iconRef(variantKey: String): String = "$ICON_PREFIX$variantKey"

    fun isPredefinedRef(iconPath: String): Boolean = iconPath.startsWith(ICON_PREFIX)

    fun variantKeyFromRef(iconPath: String): String? =
        if (isPredefinedRef(iconPath)) iconPath.removePrefix(ICON_PREFIX) else null

    fun resolveIconResId(iconPath: String): Int? {
        val variantKey = variantKeyFromRef(iconPath) ?: return null
        return getVariantByKey(variantKey)?.iconResId
    }
}
