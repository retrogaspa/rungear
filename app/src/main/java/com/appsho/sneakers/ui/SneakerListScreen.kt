package com.appsho.sneakers.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appsho.sneakers.data.PredefinedBrand
import com.appsho.sneakers.data.PredefinedColorVariant
import com.appsho.sneakers.data.PredefinedModel
import com.appsho.sneakers.data.PredefinedSneakers
import com.appsho.sneakers.data.Sneaker
import com.appsho.sneakers.ui.components.CatalogIconPreviewSize
import com.appsho.sneakers.ui.components.EmptyState
import com.appsho.sneakers.ui.components.PrimaryButton
import com.appsho.sneakers.ui.components.ScreenHeader
import com.appsho.sneakers.ui.components.SecondaryButton
import com.appsho.sneakers.ui.components.SneakerIcon
import com.appsho.sneakers.ui.components.StepIndicator
import com.appsho.sneakers.ui.theme.ButtonShape
import com.appsho.sneakers.ui.theme.CardShape
import com.appsho.sneakers.ui.theme.ImageShape
import kotlinx.coroutines.launch

private enum class AddSneakerTab { CATALOG, CUSTOM }

private enum class CatalogStep { BRAND, MODEL, COLOR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SneakerListScreen(
    viewModel: SneakerListViewModel,
    snackbarHostState: SnackbarHostState,
    gridColumns: Int = 2
) {
    val sneakers by viewModel.sneakers.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var sneakerToDelete by remember { mutableStateOf<Sneaker?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        if (sneakers.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScreenHeader(
                    title = "Coleções",
                    subtitle = "Marca → Modelo → Cor"
                )
                EmptyState(
                    icon = Icons.Outlined.SportsSoccer,
                    title = "Nenhum tênis ainda",
                    description = "Escolha marca, modelo e cor do catálogo, ou cadastre um personalizado.",
                    actionLabel = "Adicionar tênis",
                    onAction = { showAddSheet = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ScreenHeader(
                        title = "Coleções",
                        subtitle = "${sneakers.size} tênis na coleção"
                    )
                    val columns = gridColumns.coerceIn(1, 4)
                    val gridSpacing = when (columns) {
                        1 -> 0.dp
                        2 -> 12.dp
                        else -> 8.dp
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                        verticalArrangement = Arrangement.spacedBy(gridSpacing)
                    ) {
                        items(sneakers, key = { it.id }) { sneaker ->
                            SneakerGridCard(
                                sneaker = sneaker,
                                gridColumns = columns,
                                onDelete = { sneakerToDelete = sneaker }
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    PrimaryButton(
                        text = "Adicionar tênis",
                        onClick = { showAddSheet = true },
                        icon = Icons.Outlined.SportsSoccer
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddSneakerSheet(
            brands = viewModel.brands,
            isVariantInCollection = { variantKey ->
                viewModel.isVariantInCollection(sneakers, variantKey)
            },
            onDismiss = { showAddSheet = false },
            onAddPredefined = { variantKey ->
                viewModel.addPredefined(
                    variantKey = variantKey,
                    onSuccess = { showAddSheet = false },
                    onError = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
            },
            onAddCustom = { name, uri ->
                viewModel.addCustomSneaker(
                    name = name,
                    iconUri = uri,
                    onSuccess = { showAddSheet = false },
                    onError = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
            }
        )
    }

    sneakerToDelete?.let { sneaker ->
        AlertDialog(
            onDismissRequest = { sneakerToDelete = null },
            title = { Text("Excluir tênis?") },
            text = { Text("\"${sneaker.name}\" será removido da sua coleção.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSneaker(sneaker)
                        sneakerToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { sneakerToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SneakerGridCard(
    sneaker: Sneaker,
    gridColumns: Int,
    onDelete: () -> Unit
) {
    val cardPadding = when (gridColumns) {
        1 -> 12.dp
        2 -> 10.dp
        3 -> 6.dp
        else -> 4.dp
    }
    val iconInnerPadding = when (gridColumns) {
        1, 2 -> 6.dp
        3 -> 4.dp
        else -> 2.dp
    }
    val nameMaxLines = if (gridColumns >= 4) 1 else 2
    val nameStyle = when (gridColumns) {
        1, 2 -> MaterialTheme.typography.titleMedium
        3 -> MaterialTheme.typography.titleSmall
        else -> MaterialTheme.typography.labelMedium
    }
    val deleteIconSize = if (gridColumns >= 4) 18.dp else 24.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(cardPadding)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(ImageShape)
                    .background(Color.Black)
                    .padding(iconInnerPadding),
                contentAlignment = Alignment.Center
            ) {
                SneakerIcon(
                    iconPath = sneaker.iconPath,
                    contentDescription = sneaker.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .then(if (gridColumns >= 4) Modifier.size(32.dp) else Modifier)
            ) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(deleteIconSize)
                )
            }
        }
        Spacer(modifier = Modifier.height(if (gridColumns >= 4) 4.dp else 8.dp))
        Text(
            text = sneaker.name,
            style = nameStyle,
            maxLines = nameMaxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSneakerSheet(
    brands: List<PredefinedBrand>,
    isVariantInCollection: (String) -> Boolean,
    onDismiss: () -> Unit,
    onAddPredefined: (String) -> Unit,
    onAddCustom: (String, Uri) -> Unit
) {
    var selectedTab by remember { mutableStateOf(AddSneakerTab.CATALOG) }
    var catalogStep by remember { mutableStateOf(CatalogStep.BRAND) }
    var selectedBrandKey by remember { mutableStateOf<String?>(null) }
    var selectedModelKey by remember { mutableStateOf<String?>(null) }
    var selectedColorKey by remember { mutableStateOf<String?>(null) }
    var customName by remember { mutableStateOf("") }
    var customUri by remember { mutableStateOf<Uri?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        customUri = uri
    }

    val selectedBrand = selectedBrandKey?.let { PredefinedSneakers.getBrand(it) }
    val selectedModel = selectedBrandKey?.let { bk ->
        selectedModelKey?.let { mk -> PredefinedSneakers.getModel(bk, mk) }
    }
    val selectedVariant = selectedBrandKey?.let { bk ->
        selectedModelKey?.let { mk ->
            selectedColorKey?.let { ck -> PredefinedSneakers.getVariant(bk, mk, ck) }
        }
    }

    val catalogStepNumber = when (catalogStep) {
        CatalogStep.BRAND -> 1
        CatalogStep.MODEL -> 2
        CatalogStep.COLOR -> 3
    }

    fun goBack() {
        when (catalogStep) {
            CatalogStep.BRAND -> onDismiss()
            CatalogStep.MODEL -> {
                catalogStep = CatalogStep.BRAND
                selectedModelKey = null
                selectedColorKey = null
            }
            CatalogStep.COLOR -> {
                catalogStep = CatalogStep.MODEL
                selectedColorKey = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = CardShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedTab == AddSneakerTab.CATALOG && catalogStep != CatalogStep.BRAND) {
                    IconButton(onClick = { goBack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Adicionar tênis", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        if (selectedTab == AddSneakerTab.CATALOG) "Escolha marca, modelo e cor"
                        else "Cadastro personalizado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTab == AddSneakerTab.CATALOG,
                    onClick = {
                        selectedTab = AddSneakerTab.CATALOG
                        catalogStep = CatalogStep.BRAND
                        selectedBrandKey = null
                        selectedModelKey = null
                        selectedColorKey = null
                    },
                    label = { Text("Catálogo") },
                    leadingIcon = if (selectedTab == AddSneakerTab.CATALOG) {
                        { Icon(Icons.Outlined.Check, null, Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                FilterChip(
                    selected = selectedTab == AddSneakerTab.CUSTOM,
                    onClick = { selectedTab = AddSneakerTab.CUSTOM },
                    label = { Text("Personalizado") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Add, null, Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                AddSneakerTab.CATALOG -> {
                    StepIndicator(
                        currentStep = catalogStepNumber,
                        totalSteps = 3,
                        labels = listOf("Marca", "Modelo", "Cor")
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    when (catalogStep) {
                        CatalogStep.BRAND -> {
                            Text(
                                "Qual a marca?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyColumn(
                                modifier = Modifier.height(280.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(brands, key = { it.key }) { brand ->
                                    SelectionRow(
                                        title = brand.name,
                                        subtitle = "${brand.models.size} modelo(s)",
                                        onClick = {
                                            selectedBrandKey = brand.key
                                            catalogStep = CatalogStep.MODEL
                                        }
                                    )
                                }
                            }
                        }

                        CatalogStep.MODEL -> {
                            Text(
                                selectedBrand?.name ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Qual o modelo?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyColumn(
                                modifier = Modifier.height(280.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedBrand?.models.orEmpty(), key = { it.key }) { model ->
                                    SelectionRow(
                                        title = model.name,
                                        subtitle = "${model.colors.size} cor(es)",
                                        onClick = {
                                            selectedModelKey = model.key
                                            catalogStep = CatalogStep.COLOR
                                        }
                                    )
                                }
                            }
                        }

                        CatalogStep.COLOR -> {
                            Text(
                                "${selectedBrand?.name} ${selectedModel?.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Qual a cor?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyColumn(
                                modifier = Modifier.height(220.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedModel?.colors.orEmpty(), key = { it.key }) { color ->
                                    val variantKey = "${selectedBrandKey}:${selectedModelKey}:${color.key}"
                                    val alreadyAdded = isVariantInCollection(variantKey)
                                    ColorSelectionRow(
                                        brandKey = selectedBrandKey!!,
                                        modelKey = selectedModelKey!!,
                                        color = color,
                                        alreadyAdded = alreadyAdded,
                                        onClick = {
                                            if (!alreadyAdded) {
                                                selectedColorKey = color.key
                                            }
                                        },
                                        selected = selectedColorKey == color.key
                                    )
                                }
                            }

                            selectedVariant?.let { variant ->
                                val alreadyAdded = isVariantInCollection(variant.variantKey)
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(ImageShape)
                                        .background(Color.Black)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SneakerIcon(
                                        iconPath = PredefinedSneakers.iconRef(variant.variantKey),
                                        contentDescription = variant.displayName,
                                        modifier = Modifier.size(CatalogIconPreviewSize),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                PrimaryButton(
                                    text = if (alreadyAdded) "Já na coleção" else "Adicionar à coleção",
                                    onClick = { onAddPredefined(variant.variantKey) },
                                    enabled = !alreadyAdded && selectedColorKey != null
                                )
                            }
                        }
                    }
                }

                AddSneakerTab.CUSTOM -> {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Não encontrou no catálogo? Envie a foto do seu tênis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Nome completo") },
                            placeholder = { Text("Ex: Fila Racer Carbon 2 Verde") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = ImageShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        if (customUri != null) {
                            AsyncImage(
                                model = customUri,
                                contentDescription = "Prévia",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(ImageShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(ImageShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        ImageShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Outlined.PhotoCamera,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Faça upload do ícone",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        SecondaryButton(
                            text = if (customUri == null) "Escolher foto" else "Trocar foto",
                            onClick = { picker.launch("image/*") },
                            icon = Icons.Outlined.PhotoCamera
                        )
                        PrimaryButton(
                            text = "Salvar personalizado",
                            onClick = { customUri?.let { onAddCustom(customName, it) } },
                            enabled = customName.isNotBlank() && customUri != null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ButtonShape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ColorSelectionRow(
    brandKey: String,
    modelKey: String,
    color: PredefinedColorVariant,
    alreadyAdded: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        alreadyAdded -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (alreadyAdded) 0.5f else 1f)
            .clip(ButtonShape)
            .border(1.dp, borderColor, ButtonShape)
            .clickable(enabled = !alreadyAdded, onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(ImageShape)
                .background(Color.Black)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            SneakerIcon(
                iconPath = PredefinedSneakers.iconRef("$brandKey:$modelKey:${color.key}"),
                contentDescription = color.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(color.name, style = MaterialTheme.typography.titleMedium)
            if (alreadyAdded) {
                Text(
                    "Já na coleção",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (selected && !alreadyAdded) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
