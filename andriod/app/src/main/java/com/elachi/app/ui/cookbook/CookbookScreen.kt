package com.elachi.app.ui.cookbook

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.data.remote.SupabaseStorageClient
import com.elachi.app.ui.onboarding.COLOUR_OPTIONS
import com.elachi.app.ui.onboarding.ColourSwatch
import com.elachi.app.ui.onboarding.ICON_OPTIONS
import com.elachi.app.ui.onboarding.IconChip
import com.elachi.app.ui.theme.ElachiCream
import com.elachi.app.ui.theme.ElachiGreen
import kotlinx.coroutines.launch

/**
 * Visuals matched to ElaichiDemo's CookbookScreen for the By Book grid
 * (colour header, emoji icon, edit/delete actions, 3-image collage). The
 * All Recipes tab keeps our own existing search-and-filter implementation
 * instead of the demo's placeholder, since ours is already fully working.
 */
@Composable
fun CookbookScreen(
    viewModel: CookbookViewModel,
    onOpenBook: (String) -> Unit,
    onOpenRecipe: (String) -> Unit,
    onAddRecipe: (String) -> Unit,
) {
    val books by viewModel.books.collectAsState()
    val allRecipes by viewModel.allRecipes.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var showNewBookDialog by remember { mutableStateOf(false) }
    var editingBook by remember { mutableStateOf<RecipeBookEntity?>(null) }
    var deletingBook by remember { mutableStateOf<RecipeBookEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            if (books.isNotEmpty()) {
                FloatingActionButton(onClick = { onAddRecipe(books.first().id) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Recipe")
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(ElachiCream)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("By Book") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("All Recipes") })
            }

            if (tab == 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(books) { book ->
                        val recipesInBook = allRecipes.filter { it.bookId == book.id }
                        BookCard(
                            book = book,
                            recipeCount = recipesInBook.size,
                            previewImages = recipesInBook.mapNotNull { it.imageUrl }.take(3),
                            onClick = { onOpenBook(book.id) },
                            onEdit = { editingBook = book },
                            onDelete = { deletingBook = book },
                        )
                    }
                    item {
                        NewBookCard(onClick = { showNewBookDialog = true })
                    }
                }
            } else {
                var search by remember { mutableStateOf("") }
                val filtered = allRecipes.filter { it.title.contains(search, ignoreCase = true) }
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = search, onValueChange = { search = it },
                            placeholder = { Text("Search recipes...") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    items(filtered) { recipe ->
                        Card(onClick = { onOpenRecipe(recipe.id) }, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(recipe.title, fontWeight = FontWeight.Bold)
                                    Text("${recipe.cuisine} \u00B7 ${recipe.cookTimeMinutes} min", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (recipe.isFavourite) Text("\u2605")
                            }
                        }
                    }
                }
            }
        }

        if (showNewBookDialog) {
            BookFormDialog(
                title = "New Recipe Book",
                initial = null,
                onDismiss = { showNewBookDialog = false },
                onSave = { name, desc, imageUrl, icon, colour ->
                    viewModel.createBook(name, desc, icon, colour, imageUrl)
                    showNewBookDialog = false
                },
            )
        }
        editingBook?.let { book ->
            BookFormDialog(
                title = "Edit Recipe Book",
                initial = book,
                onDismiss = { editingBook = null },
                onSave = { name, desc, imageUrl, icon, colour ->
                    viewModel.updateBook(book, name, desc, icon, colour, imageUrl)
                    editingBook = null
                },
            )
        }
        deletingBook?.let { book ->
            AlertDialog(
                onDismissRequest = { deletingBook = null },
                title = { Text("Delete \"${book.name}\"?") },
                text = { Text("This also deletes the recipes inside it.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteBook(book); deletingBook = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { deletingBook = null }) { Text("Cancel") } },
            )
        }
    }
}

@Composable
private fun BookCard(
    book: RecipeBookEntity,
    recipeCount: Int,
    previewImages: List<String>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val headerColor = runCatching {
        Color(android.graphics.Color.parseColor(book.colour))
    }.getOrDefault(ElachiGreen)

    val coverImage = book.coverImageUrl
        ?.takeIf { it.isNotBlank() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.2.dp,
            color = Color(0xFFE5E2DD),
        ),
    ) {
        Column {
            /*
             * Cookbook cover.
             *
             * The selected cookbook image is now displayed here.
             * The colour remains behind it as a fallback if loading fails.
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(headerColor),
            ) {
                if (coverImage != null) {
                    AsyncImage(
                        model = coverImage,
                        contentDescription = "${book.name} cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Dark overlay so the icon buttons remain visible.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.22f)),
                    )
                }

                Text(
                    text = book.icon.ifBlank { "📖" },
                    fontSize = 28.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onEdit),
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit ${book.name}",
                                modifier = Modifier.size(17.dp),
                                tint = Color.White,
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onDelete),
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete ${book.name}",
                                modifier = Modifier.size(17.dp),
                                tint = Color.White,
                            )
                        }
                    }
                }
            }

            /*
             * Recipe preview strip.
             * These are recipe images, separate from the cookbook cover.
             */
            if (previewImages.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                ) {
                    previewImages.forEach { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }

                    repeat(3 - previewImages.size) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFFF0EDE9)),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp,
                    ),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = book.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )

                    book.description
                        ?.takeIf { it.isNotBlank() }
                        ?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                            )
                        }
                }

                Text(
                    text = if (recipeCount == 1) {
                        "1 recipe"
                    } else {
                        "$recipeCount recipes"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = headerColor,
                )
            }
        }
    }
}

@Composable
private fun NewBookCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(207.dp).clickable(onClick = onClick),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFC5C8BA)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(40.dp), color = Color(0xFFF0EDE9), shape = RoundedCornerShape(12.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, contentDescription = null) }
            }
            Spacer(Modifier.height(8.dp))
            Text("New Book", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600), color = ElachiGreen)
        }
    }
}

@Composable
private fun BookFormDialog(
    title: String,
    initial: RecipeBookEntity?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        description: String?,
        coverImageUrl: String?,
        icon: String,
        colour: String,
    ) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember(initial) {
        mutableStateOf(initial?.name ?: "")
    }

    var description by remember(initial) {
        mutableStateOf(initial?.description ?: "")
    }

    var coverImageUrl by remember(initial) {
        mutableStateOf(initial?.coverImageUrl)
    }

    var icon by remember(initial) {
        mutableStateOf(
            initial?.icon?.ifBlank { null }
                ?: ICON_OPTIONS.first(),
        )
    }

    var colour by remember(initial) {
        mutableStateOf(
            initial?.colour?.ifBlank { null }
                ?: COLOUR_OPTIONS.first(),
        )
    }

    var localPreviewUri by remember(initial) {
        mutableStateOf<Uri?>(null)
    }

    var isUploading by remember(initial) {
        mutableStateOf(false)
    }

    var uploadError by remember(initial) {
        mutableStateOf<String?>(null)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            localPreviewUri = uri
            uploadError = null
            isUploading = true

            scope.launch {
                SupabaseStorageClient.uploadImage(
                    context = context,
                    uri = uri,
                    folder = "book-covers",
                ).onSuccess { uploadedUrl ->
                    coverImageUrl = uploadedUrl
                    uploadError = null
                }.onFailure { exception ->
                    localPreviewUri = null

                    uploadError = exception.message
                        ?: "The cover image could not be uploaded."
                }

                isUploading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isUploading) {
                onDismiss()
            }
        },
        title = {
            Text(title)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(
                    rememberScrollState(),
                ),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("Book Name")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = {
                        Text("Description (optional)")
                    },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ICON_OPTIONS.forEach { option ->
                        IconChip(
                            icon = option,
                            selected = icon == option,
                            onClick = {
                                icon = option
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Colour",
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    COLOUR_OPTIONS.forEach { option ->
                        ColourSwatch(
                            hex = option,
                            selected = colour == option,
                            onClick = {
                                colour = option
                            },
                        )
                    }
                }

                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cover Photo",
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable(enabled = !isUploading) {
                            photoPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts
                                        .PickVisualMedia
                                        .ImageOnly,
                                ),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isUploading -> {
                            CircularProgressIndicator()
                        }

                        localPreviewUri != null -> {
                            AsyncImage(
                                model = localPreviewUri,
                                contentDescription =
                                    "Selected cookbook cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        !coverImageUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = coverImageUrl,
                                contentDescription =
                                    "Current cookbook cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        else -> {
                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddAPhoto,
                                    contentDescription = null,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Add Cover Photo",
                                    style =
                                        MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                when {
                    isUploading -> {
                        Text(
                            text = "Uploading cookbook cover...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    uploadError != null -> {
                        Text(
                            text = uploadError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )

                        Text(
                            text = "Tap the image area to try again.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                        )
                    }

                    localPreviewUri != null &&
                            coverImageUrl != null -> {
                        Text(
                            text = "Cover image uploaded successfully.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    !coverImageUrl.isNullOrBlank() -> {
                        Text(
                            text = "A cover image is currently saved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        name.trim(),
                        description.trim().ifBlank { null },
                        coverImageUrl,
                        icon,
                        colour,
                    )
                },
                enabled = name.isNotBlank() && !isUploading,
            ) {
                Text(
                    text = if (isUploading) {
                        "Uploading..."
                    } else {
                        "Save"
                    },
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading,
            ) {
                Text("Cancel")
            }
        },
    )
}