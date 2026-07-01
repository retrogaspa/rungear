package com.appsho.sneakers.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

class SneakerRepository(
    private val dao: SneakerDao,
    private val context: Context
) {
    val sneakers: Flow<List<Sneaker>> = dao.getAll()

    val brands: List<PredefinedBrand> = PredefinedSneakers.brands

    suspend fun getById(id: Long): Sneaker? = dao.getById(id)

    suspend fun isVariantInCollection(variantKey: String): Boolean {
        return dao.existsByIconPath(PredefinedSneakers.iconRef(variantKey))
    }

    suspend fun addFromPredefined(variantKey: String): Long {
        val variant = PredefinedSneakers.getVariantByKey(variantKey)
            ?: error("Combinação não encontrada no catálogo.")

        if (isVariantInCollection(variantKey)) {
            error("\"${variant.displayName}\" já está na sua coleção.")
        }

        return dao.insert(
            Sneaker(
                name = variant.displayName,
                iconPath = PredefinedSneakers.iconRef(variantKey)
            )
        )
    }

    suspend fun addCustom(name: String, iconUri: Uri): Long {
        val iconPath = copyIconToInternalStorage(iconUri)
        return dao.insert(Sneaker(name = name.trim(), iconPath = iconPath))
    }

    suspend fun delete(sneaker: Sneaker) {
        if (!PredefinedSneakers.isPredefinedRef(sneaker.iconPath)) {
            File(sneaker.iconPath).delete()
        }
        dao.delete(sneaker)
    }

    private fun copyIconToInternalStorage(sourceUri: Uri): String {
        val iconsDir = File(context.filesDir, "sneaker_icons").apply { mkdirs() }
        val destination = File(iconsDir, "${UUID.randomUUID()}.png")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Não foi possível ler a imagem selecionada.")

        return destination.absolutePath
    }
}
