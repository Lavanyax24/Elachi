package com.elachi.app.ui.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// This is the addRecipeScreen which allows the user to add a new recipe

@Composable
fun AddRecipeScreen(
    viewModel: AddRecipeViewModel,
    onClose: () -> Unit,
    onOpenCamera: () -> Unit,
    onSaved: (String) -> Unit,
) {
    LaunchedEffect(viewModel.savedRecipeId.value) {
        viewModel.savedRecipeId.value?.let { onSaved(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomActionBar(viewModel, onCancel = onClose) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(if (viewModel.isEditMode) "Edit Recipe" else "Add Recipe", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text("Fields marked * are required", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface) }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                ManualTabItem("Manual", true, Modifier.weight(1f))
                ManualTabItem("Camera", false, Modifier.weight(1f).clickable { onOpenCamera() })
                ManualTabItem("Screenshot", false, Modifier.weight(1f).clickable { onOpenCamera() })
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            if (viewModel.isLoading.value) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                ManualForm(viewModel, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ManualForm(vm: AddRecipeViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var optionalExpanded by rememberSaveable { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) vm.uploadPhoto(context, uri) }

    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("What you need to save", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Choose a recipe book, enter a title, then add at least one ingredient and step.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Recipe details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                ManualLabel("Recipe Book", required = true)
                RecipeBookDropdown(vm)
                ManualLabel("Title", required = true)
                ManualTextField(vm.title.value, "e.g. Grandma's Apple Pie") { vm.title.value = it }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Kitchen, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("Ingredients *", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                Text("Add at least one ingredient.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                vm.ingredients.forEachIndexed { index, ingredient ->
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ManualTextField(value = ingredient.name, placeholder = "Ingredient name") { vm.updateIngredient(index, ingredient.copy(name = it)) }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            ManualTextField(value = ingredient.quantity, placeholder = "Qty", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal) { vm.updateIngredient(index, ingredient.copy(quantity = it)) }
                            IngredientUnitDropdown(selectedUnit = ingredient.unit, unitOptions = vm.ingredientUnitOptions, modifier = Modifier.weight(1f), onUnitSelected = { vm.updateIngredient(index, ingredient.copy(unit = if (it == "No unit") "" else it)) })
                            IconButton(onClick = { vm.removeIngredientRow(index) }, enabled = vm.ingredients.size > 1) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = if (vm.ingredients.size > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
                DashedButtonManual("Add Ingredient") { vm.addIngredientRow() }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.FormatListNumbered, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("Cooking steps *", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                Text("Add at least one instruction.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                vm.steps.forEachIndexed { index, step ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(modifier = Modifier.padding(top = 10.dp).size(28.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape) {
                            Box(contentAlignment = Alignment.Center) { Text("${index + 1}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        }
                        ManualTextField(step, "Describe this step...", modifier = Modifier.weight(1f), height = 74.dp) { vm.steps[index] = it }
                        IconButton(onClick = { vm.removeStepRow(index) }, enabled = vm.steps.size > 1, modifier = Modifier.padding(top = 6.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = if (vm.steps.size > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        }
                    }
                }
                DashedButtonManual("Add Step") { vm.addStepRow() }
            }
        }

        Surface(onClick = { optionalExpanded = !optionalExpanded }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Optional details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Photo, cuisine, servants, allergens...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(if (optionalExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
            }
        }
        if (optionalExpanded) { OptionalDetails(vm = vm, onPickPhoto = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) }
        vm.errorMessage.value?.let { Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) { Text(it, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer) } }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OptionalDetails(vm: AddRecipeViewModel, onPickPhoto: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ManualLabel("Recipe Photo")
            Surface(onClick = onPickPhoto, modifier = Modifier.fillMaxWidth().height(110.dp), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        vm.isUploadingPhoto.value -> CircularProgressIndicator()
                        vm.photoUri.value != null -> AsyncImage(model = vm.photoUri.value, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        else -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.AddAPhoto, contentDescription = null); Text("Tap to add a photo") }
                    }
                }
            }
            ManualLabel("Category")
            PresetDropdown(vm.category.value, vm.categoryOptions) { vm.category.value = it }
            ManualLabel("Cuisine")
            PresetDropdown(vm.cuisine.value, vm.cuisineOptions) { vm.cuisine.value = it }
            ManualLabel("Food Type")
            PresetDropdown(vm.foodType.value, vm.foodTypeOptions) { vm.foodType.value = it }
            ManualLabel("Difficulty")
            PresetDropdown(vm.difficulty.value, vm.difficultyOptions) { vm.difficulty.value = it }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) { ManualLabel("Servings"); ManualTextField(vm.servings.value, "4", keyboardType = KeyboardType.Number) { vm.servings.value = it } }
                Column(modifier = Modifier.weight(1f)) { ManualLabel("Cook Time"); ManualTextField(vm.cookTimeMinutes.value, "30", keyboardType = KeyboardType.Number) { vm.cookTimeMinutes.value = it } }
            }
            ManualLabel("Method")
            PresetDropdown(vm.method.value, vm.methodOptions) { vm.method.value = it }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Private recipe", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Only you can see it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = vm.isPrivate.value,
                    onCheckedChange = { vm.isPrivate.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
            ManualLabel("Allergens Present")
            AllergenFlow(vm)
        }
    }
}

@Composable
private fun BottomActionBar(vm: AddRecipeViewModel, onCancel: () -> Unit) {
    val canSave = vm.selectedBookId.value.isNotBlank() && vm.title.value.isNotBlank() && vm.ingredients.any { it.name.isNotBlank() } && vm.steps.any { it.isNotBlank() }
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(modifier = Modifier.navigationBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(text = if (canSave) "Ready to save" else "Complete all fields marked *", style = MaterialTheme.typography.labelMedium, color = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W600) }
                Button(onClick = { vm.save() }, enabled = canSave && !vm.isLoading.value && !vm.isSaving.value && !vm.isUploadingPhoto.value, modifier = Modifier.weight(2f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                    if (vm.isSaving.value) { CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) }
                    else { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp)); Text(if (vm.isEditMode) "Update Recipe" else "Save Recipe", fontWeight = FontWeight.W600) } }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeBookDropdown(vm: AddRecipeViewModel) {
    val books by vm.availableBooks.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val selectedBook = books.find { it.id == vm.selectedBookId.value }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(modifier = Modifier.fillMaxWidth().height(51.dp).menuAnchor(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
            Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(selectedBook?.name ?: "Choose a book", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = MaterialTheme.colorScheme.surface) {
            if (books.isEmpty()) DropdownMenuItem(text = { Text("No books yet") }, onClick = {}, enabled = false)
            books.forEach { book -> DropdownMenuItem(text = { Text(book.name) }, onClick = { vm.selectedBookId.value = book.id; expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientUnitDropdown(selectedUnit: String, unitOptions: List<String>, modifier: Modifier = Modifier, onUnitSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(value = selectedUnit.ifBlank { "Unit" }, onValueChange = {}, readOnly = true, singleLine = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().menuAnchor())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = MaterialTheme.colorScheme.surface) {
            unitOptions.forEach { unit -> DropdownMenuItem(text = { Text(unit) }, onClick = { onUnitSelected(unit); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetDropdown(value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(modifier = Modifier.fillMaxWidth().height(51.dp).menuAnchor(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
            Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = MaterialTheme.colorScheme.surface) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

@Composable
private fun ManualTabItem(label: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        if (isSelected) { Spacer(Modifier.height(10.dp)); Box(modifier = Modifier.fillMaxWidth().height(2.4.dp).background(MaterialTheme.colorScheme.primary)) }
    }
}

@Composable
private fun ManualLabel(text: String, required: Boolean = false) {
    Text(text = if (required) "${text.uppercase()} *" else text.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.88.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun ManualTextField(value: String, placeholder: String, modifier: Modifier = Modifier, height: Dp = 51.dp, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = modifier.fillMaxWidth().height(height), placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 15.sp) }, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AllergenFlow(vm: AddRecipeViewModel) {
    var showAddAllergenDialog by rememberSaveable { mutableStateOf(false) }
    var customAllergenName by rememberSaveable { mutableStateOf("") }
    var customAllergenError by rememberSaveable { mutableStateOf<String?>(null) }
    Column(modifier = Modifier.fillMaxWidth()) {
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vm.allergenOptions.forEach { allergen ->
                val isSelected = vm.selectedAllergens.contains(allergen)
                Surface(modifier = Modifier.clickable { vm.toggleAllergen(allergen) }, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(999.dp), border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        if (isSelected) { Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White) }
                        Text(text = allergen, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Surface(modifier = Modifier.clickable { customAllergenName = ""; customAllergenError = null; showAddAllergenDialog = true }, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(999.dp), border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary)) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(text = "Add allergy", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
    if (showAddAllergenDialog) {
        AlertDialog(
            onDismissRequest = { showAddAllergenDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Create Allergy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Enter the name of the allergen.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(value = customAllergenName, onValueChange = { customAllergenName = it; customAllergenError = null }, label = { Text("Allergy name") }, isError = customAllergenError != null, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { Button(onClick = { if (customAllergenName.trim().isBlank()) customAllergenError = "Enter a name" else { vm.addCustomAllergen(customAllergenName.trim()); showAddAllergenDialog = false } }) { Text("Create") } },
            dismissButton = { TextButton(onClick = { showAddAllergenDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DashedButtonManual(label: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(46.dp).clickable(onClick = onClick)) {
        val color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        Canvas(modifier = Modifier.fillMaxSize()) { drawRoundRect(color = color, style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())) }
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
