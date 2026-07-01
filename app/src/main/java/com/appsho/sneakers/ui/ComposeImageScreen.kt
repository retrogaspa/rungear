package com.appsho.sneakers.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appsho.sneakers.data.Sneaker
import com.appsho.sneakers.ui.components.ActionRow
import com.appsho.sneakers.ui.components.EmptyState
import com.appsho.sneakers.ui.components.PreviewFrame
import com.appsho.sneakers.ui.components.PrimaryButton
import com.appsho.sneakers.ui.components.ScreenHeader
import com.appsho.sneakers.ui.components.SecondaryButton
import com.appsho.sneakers.ui.components.SectionCard
import com.appsho.sneakers.ui.components.StepIndicator
import com.appsho.sneakers.ui.components.SneakerIcon
import com.appsho.sneakers.ui.theme.ImageShape

private const val SNEAKER_GRID_COLUMNS = 3
private const val SEARCH_MIN_SNEAKERS = 4

@Composable
fun ComposeImageScreen(
    viewModel: ComposeImageViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToCollection: () -> Unit = {}
) {
    val context = LocalContext.current
    val sneakers by viewModel.sneakers.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showResult by rememberSaveable { mutableStateOf(false) }
    var dismissedResult by rememberSaveable { mutableStateOf(false) }
    var sneakerSearchQuery by rememberSaveable { mutableStateOf("") }

    val hasSelection = uiState.baseImageUri != null && uiState.selectedSneakerId != null
    val resultReady = uiState.previewBitmap != null && !uiState.isProcessing

    val filteredSneakers = remember(sneakers, sneakerSearchQuery) {
        val query = sneakerSearchQuery.trim()
        if (query.isEmpty()) sneakers
        else sneakers.filter { it.name.contains(query, ignoreCase = true) }
    }

    LaunchedEffect(uiState.baseImageUri, uiState.selectedSneakerId) {
        dismissedResult = false
        showResult = false
    }

    LaunchedEffect(resultReady) {
        if (resultReady && !dismissedResult) showResult = true
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.shareIntent.collect { intent ->
            context.startActivity(
                android.content.Intent.createChooser(intent, "Compartilhar via")
            )
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.setBaseImage(it) } }

    if (sneakers.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Criar",
                subtitle = "Componha sua foto com o ícone do tênis"
            )
            EmptyState(
                icon = Icons.Outlined.SportsSoccer,
                title = "Nenhum tênis ainda",
                description = "Vá em Coleções e adicione pelo menos um tênis para começar a criar.",
                actionLabel = "Ir para Coleções",
                onAction = onNavigateToCollection,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    if (showResult && resultReady) {
        ResultScreen(
            previewBitmap = uiState.previewBitmap!!,
            isProcessing = uiState.isProcessing,
            onSave = { viewModel.saveComposedImage() },
            onShare = { viewModel.shareComposedImage() },
            onEdit = {
                dismissedResult = true
                showResult = false
            }
        )
        return
    }

    val currentStep = when {
        hasSelection && uiState.isProcessing -> 2
        uiState.selectedSneakerId != null -> 2
        uiState.baseImageUri != null -> 1
        else -> 1
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ScreenHeader(
                title = "Criar",
                subtitle = "Escolha a foto, depois o tênis — o resultado aparece em seguida"
            )

            StepIndicator(
                currentStep = currentStep,
                totalSteps = 3,
                labels = listOf("Foto", "Tênis", "Resultado"),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                ActionRow(
                    icon = Icons.Outlined.Image,
                    title = "Escolher foto",
                    subtitle = if (uiState.baseImageUri != null) {
                        "Foto selecionada — toque para trocar"
                    } else {
                        "Toque para abrir a galeria"
                    },
                    onClick = { imagePicker.launch("image/*") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreviewFrame(
                modifier = Modifier.padding(horizontal = 20.dp),
                isLoading = false
            ) {
                when {
                    hasSelection && uiState.isProcessing -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Gerando resultado…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    uiState.baseImageUri != null -> {
                        AsyncImage(
                            model = uiState.baseImageUri,
                            contentDescription = "Imagem base",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(ImageShape),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        EmptyPreviewHint()
                    }
                }
            }

            if (hasSelection && uiState.isProcessing) {
                Text(
                    text = "Aguarde — montando a imagem com a barra do tênis…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Escolher tênis", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))

                if (sneakers.size >= SEARCH_MIN_SNEAKERS) {
                    OutlinedTextField(
                        value = sneakerSearchQuery,
                        onValueChange = { sneakerSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Buscar tênis…") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (sneakerSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { sneakerSearchQuery = "" }) {
                                    Icon(Icons.Outlined.Clear, contentDescription = "Limpar busca")
                                }
                            }
                        },
                        shape = ImageShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (filteredSneakers.isEmpty()) {
                    Text(
                        text = "Nenhum tênis encontrado para \"$sneakerSearchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    SneakerChipGrid(
                        sneakers = filteredSneakers,
                        selectedSneakerId = uiState.selectedSneakerId,
                        onSelect = { viewModel.selectSneaker(it) }
                    )
                }

                if (hasSelection && !uiState.isProcessing && !resultReady) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Pronto! Gerando o resultado…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SneakerChipGrid(
    sneakers: List<Sneaker>,
    selectedSneakerId: Long?,
    onSelect: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sneakers.chunked(SNEAKER_GRID_COLUMNS).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { sneaker ->
                    SneakerChip(
                        sneaker = sneaker,
                        selected = selectedSneakerId == sneaker.id,
                        onClick = { onSelect(sneaker.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(SNEAKER_GRID_COLUMNS - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ResultScreen(
    previewBitmap: android.graphics.Bitmap,
    isProcessing: Boolean,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ScreenHeader(
                title = "Resultado",
                subtitle = "Confira a imagem antes de salvar ou compartilhar"
            )

            StepIndicator(
                currentStep = 3,
                totalSteps = 3,
                labels = listOf("Foto", "Tênis", "Resultado"),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            PreviewFrame(
                modifier = Modifier.padding(horizontal = 20.dp),
                isLoading = isProcessing
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Resultado final",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(ImageShape),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gostou do resultado?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                text = "Salve na galeria ou compartilhe direto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            TextButton(
                onClick = onEdit,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(" Trocar foto ou tênis", modifier = Modifier.padding(start = 4.dp))
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "Compartilhar",
                    onClick = onShare,
                    enabled = !isProcessing,
                    icon = Icons.Outlined.Share,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Salvar",
                    onClick = onSave,
                    enabled = !isProcessing,
                    icon = Icons.Outlined.SaveAlt,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyPreviewHint() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(ImageShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Prévia da foto", style = MaterialTheme.typography.titleMedium)
        Text(
            "Escolha uma foto acima",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SneakerChip(
    sneaker: Sneaker,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ImageShape)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = ImageShape
            )
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(8.dp)
    ) {
        SneakerIcon(
            iconPath = sneaker.iconPath,
            contentDescription = sneaker.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(ImageShape)
                .background(Color.Black)
                .padding(4.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = sneaker.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
