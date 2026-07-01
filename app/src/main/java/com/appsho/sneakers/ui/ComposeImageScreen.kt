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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun ComposeImageScreen(
    viewModel: ComposeImageViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToCollection: () -> Unit = {}
) {
    val context = LocalContext.current
    val sneakers by viewModel.sneakers.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

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

    val currentStep = when {
        uiState.previewBitmap != null -> 2
        uiState.selectedSneakerId != null -> 2
        uiState.baseImageUri != null -> 1
        else -> 1
    }

    val canSave = uiState.previewBitmap != null && !uiState.isProcessing

    if (sneakers.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Criar",
                subtitle = "Componha sua foto com o ícone do tênis"
            )
            EmptyState(
                icon = Icons.Outlined.SportsSoccer,
                title = "Cadastre um tênis primeiro",
                description = "Vá em Coleção e adicione pelo menos um tênis para começar a criar.",
                actionLabel = "Ir para Coleção",
                onAction = onNavigateToCollection,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ScreenHeader(
                title = "Criar",
                subtitle = "Foto + tênis — barra automática na direita"
            )

            StepIndicator(
                currentStep = currentStep,
                totalSteps = 2,
                labels = listOf("Foto", "Tênis"),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            PreviewFrame(
                modifier = Modifier.padding(horizontal = 20.dp),
                isLoading = uiState.isProcessing && uiState.baseImageUri != null
            ) {
                when {
                    uiState.previewBitmap != null -> {
                        Image(
                            bitmap = uiState.previewBitmap!!.asImageBitmap(),
                            contentDescription = "Pré-visualização",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(ImageShape),
                            contentScale = ContentScale.Fit
                        )
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
                                androidx.compose.material3.Icon(
                                    Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Sua composição aparece aqui",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Comece escolhendo uma foto abaixo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                ActionRow(
                    icon = Icons.Outlined.Image,
                    title = "1. Escolher foto",
                    subtitle = if (uiState.baseImageUri != null) "Foto selecionada ✓" else "Toque para abrir a galeria",
                    onClick = { imagePicker.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "2. Escolher tênis",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(sneakers, key = { it.id }) { sneaker ->
                        SneakerChip(
                            sneaker = sneaker,
                            selected = uiState.selectedSneakerId == sneaker.id,
                            onClick = { viewModel.selectSneaker(sneaker.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
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
                    onClick = { viewModel.shareComposedImage() },
                    enabled = canSave,
                    icon = Icons.Outlined.Share,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Salvar",
                    onClick = { viewModel.saveComposedImage() },
                    enabled = canSave,
                    icon = Icons.Outlined.SaveAlt,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SneakerChip(
    sneaker: Sneaker,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
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
                .size(64.dp)
                .clip(ImageShape)
                .background(Color.Black)
                .padding(5.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            sneaker.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.size(width = 72.dp, height = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}
