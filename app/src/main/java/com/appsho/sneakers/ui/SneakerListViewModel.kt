package com.appsho.sneakers.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appsho.sneakers.data.PredefinedBrand
import com.appsho.sneakers.data.PredefinedSneakers
import com.appsho.sneakers.data.Sneaker
import com.appsho.sneakers.data.SneakerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SneakerListViewModel(
    private val repository: SneakerRepository
) : ViewModel() {

    val sneakers = repository.sneakers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val brands: List<PredefinedBrand> = repository.brands

    fun isVariantInCollection(sneakers: List<Sneaker>, variantKey: String): Boolean {
        val ref = PredefinedSneakers.iconRef(variantKey)
        return sneakers.any { it.iconPath == ref }
    }

    fun addPredefined(
        variantKey: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.addFromPredefined(variantKey)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erro ao adicionar tênis.")
            }
        }
    }

    fun addCustomSneaker(
        name: String,
        iconUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("Informe o nome do tênis.")
            return
        }
        viewModelScope.launch {
            try {
                repository.addCustom(name, iconUri)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Erro ao salvar tênis.")
            }
        }
    }

    fun deleteSneaker(sneaker: Sneaker) {
        viewModelScope.launch {
            repository.delete(sneaker)
        }
    }

    class Factory(private val repository: SneakerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SneakerListViewModel(repository) as T
        }
    }
}
