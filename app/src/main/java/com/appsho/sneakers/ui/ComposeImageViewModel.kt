package com.appsho.sneakers.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appsho.sneakers.data.SneakerOverlayLabel
import com.appsho.sneakers.data.SneakerRepository
import com.appsho.sneakers.util.ImageOverlayHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ComposeUiState(
    val baseImageUri: Uri? = null,
    val selectedSneakerId: Long? = null,
    val previewBitmap: Bitmap? = null,
    val isProcessing: Boolean = false,
    val message: String? = null
)

class ComposeImageViewModel(
    private val repository: SneakerRepository,
    private val appContext: Context
) : ViewModel() {

    val sneakers = repository.sneakers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(ComposeUiState())
    val uiState: StateFlow<ComposeUiState> = _uiState.asStateFlow()

    private val _shareIntent = MutableSharedFlow<Intent>()
    val shareIntent: SharedFlow<Intent> = _shareIntent.asSharedFlow()

    private var previewJob: Job? = null

    fun setBaseImage(uri: Uri) {
        _uiState.update { it.copy(baseImageUri = uri, message = null) }
        schedulePreviewUpdate()
    }

    fun selectSneaker(sneakerId: Long) {
        _uiState.update { it.copy(selectedSneakerId = sneakerId, message = null) }
        schedulePreviewUpdate()
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun schedulePreviewUpdate() {
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            delay(200)
            updatePreview()
        }
    }

    private fun updatePreview() {
        val state = _uiState.value
        val baseUri = state.baseImageUri
        val sneakerId = state.selectedSneakerId

        if (baseUri == null || sneakerId == null) {
            recyclePreviewBitmap()
            _uiState.update { it.copy(previewBitmap = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                val preview = withContext(Dispatchers.Default) {
                    buildComposedBitmap(baseUri, sneakerId)
                }
                recyclePreviewBitmap()
                _uiState.update {
                    it.copy(previewBitmap = preview, isProcessing = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = e.message ?: "Erro ao gerar pré-visualização."
                    )
                }
            }
        }
    }

    fun saveComposedImage() {
        val preview = _uiState.value.previewBitmap ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                withContext(Dispatchers.IO) {
                    ImageOverlayHelper.saveBitmapToGallery(appContext, preview, "rungear")
                }
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = "Imagem salva em Fotos/RunGear."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = e.message ?: "Erro ao salvar imagem."
                    )
                }
            }
        }
    }

    fun shareComposedImage() {
        val preview = _uiState.value.previewBitmap ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                val uri = withContext(Dispatchers.IO) {
                    ImageOverlayHelper.saveBitmapToCache(appContext, preview)
                }
                _shareIntent.emit(ImageOverlayHelper.createShareIntent(uri))
                _uiState.update { it.copy(isProcessing = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = e.message ?: "Erro ao compartilhar imagem."
                    )
                }
            }
        }
    }

    private suspend fun buildComposedBitmap(baseUri: Uri, sneakerId: Long): Bitmap {
        val sneaker = repository.getById(sneakerId)
            ?: error("Tênis não encontrado.")

        val baseBitmap = withContext(Dispatchers.IO) {
            ImageOverlayHelper.loadBitmapFromUri(appContext, baseUri)
        } ?: error("Não foi possível carregar a imagem base.")

        val iconBitmap = withContext(Dispatchers.IO) {
            ImageOverlayHelper.loadBitmapFromIconRef(appContext, sneaker.iconPath)
        } ?: error("Não foi possível carregar o ícone do tênis.")

        val label = SneakerOverlayLabel.resolve(sneaker)

        return ImageOverlayHelper.overlaySneakerBadgeOnImage(
            baseBitmap = baseBitmap,
            iconBitmap = iconBitmap,
            brandModelLabel = label
        )
    }

    private fun recyclePreviewBitmap() {
        _uiState.value.previewBitmap?.recycle()
    }

    override fun onCleared() {
        recyclePreviewBitmap()
        super.onCleared()
    }

    class Factory(
        private val repository: SneakerRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ComposeImageViewModel(repository, appContext) as T
        }
    }
}
