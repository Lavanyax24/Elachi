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
import com.elachi.app.ui.theme.ElachiCream
import com.elachi.app.ui.theme.ElachiGreen
import com.elachi.app.ui.theme.ElachiGreenLight
import com.elachi.app.ui.theme.ElachiTextPrimary
import com.elachi.app.ui.theme.ElachiTextSecondary

/**
 * Visuals matched exactly to ElaichiDemo's ManualEntryScreen (header with
 * Close button, Manual/Camera/Screenshot tab bar, dropdown fields, allergen
 * chips, ingredient/step rows with delete buttons, dashed add buttons,
 * bottom action bar). Two functional additions the demo's static version
 * didn't have: the dropdowns actually open and pick a real value, and a
 * Recipe Book picker was added since every recipe must belong to one.
 */
@Composable
fun AddRecipeScreen(
    viewModel: AddRecipeViewModel,
    onClose: () -> Unit,
    onOpenCamera: () -> Unit,
    onSaved: (String) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) } // 0 = Manual, 1 = Camera, 2 = Screenshot

    LaunchedEffect(viewModel.savedRecipeId.value) {
        viewModel.savedRecipeId.value?.let { onSaved(it) }
    }

    Column(modifier = Modifier.fillMaxSize().background(ElachiCream).imePadding()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Add Recipe", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = ElachiGreen)
            Surface(
                modifier = Modifier.size(64.dp, 34.dp).clickable(onClick = onClose),
                color = Color(0xFFF0EDE9),
                shape = RoundedCornerShape(17.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(22.dp)) }
            }
        }
        HorizontalDivider(thickness = 2.4.dp, color = Color(0xFFF0EDE9))

        Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            ManualTabItem("Manual", tab == 0, Modifier.weight(1f).clickable { tab = 0 })
            ManualTabItem("Camera", tab == 1, Modifier.weight(1f).clickable { onOpenCamera() })
            ManualTabItem("Screenshot", tab == 2, Modifier.weight(1f).clickable { onOpenCamera() })
        }
        HorizontalDivider(thickness = 2.4.dp, color = Color(0xFFE5E2DD))

        if (tab == 0) {
            ManualForm(viewModel, modifier = Modifier.weight(1f))
            BottomActionBar(viewModel, onCancel = onClose)
        }
    }
}

@Composable
private fun ManualForm(vm: AddRecipeViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.uploadPhoto(context, uri)
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Recipe photo
        Text("Recipe Photo (optional)", style = MaterialTheme.typography.titleMedium)
        Box(
            modifier = Modifier.fillMaxWidth().height(140.dp)
                .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            contentAlignment = Alignment.Center,
        ) {
            when {
                vm.isUploadingPhoto.value -> CircularProgressIndicator()
                vm.photoUri.value != null -> AsyncImage(model = vm.photoUri.value, contentDescription = "Recipe photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                    Text("Tap to add a photo", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Main info card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.4.dp, Color(0xFFE5E2DD)),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ManualLabel("Recipe Book")
                RecipeBookDropdown(vm)

                ManualLabel("Title")
                ManualTextField(vm.title.value, "e.g. Grandma's Apple Pie") { vm.title.value = it }

                ManualLabel("Category")
                PresetDropdown(vm.category.value, vm.categoryOptions) { vm.category.value = it }

                ManualLabel("Cuisine")
                PresetDropdown(vm.cuisine.value, vm.cuisineOptions) { vm.cuisine.value = it }

                ManualLabel("Food Type")
                PresetDropdown(vm.foodType.value, vm.foodTypeOptions) { vm.foodType.value = it }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        ManualLabel("Difficulty")
                        PresetDropdown(vm.difficulty.value, vm.difficultyOptions) { vm.difficulty.value = it }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ManualLabel("Servings")
                        ManualTextField(vm.servings.value, "4", keyboardType = KeyboardType.Number) { vm.servings.value = it }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                    Column(modifier = Modifier.weight(1f)) {
                        ManualLabel("Cook Time (min)")
                        ManualTextField(vm.cookTimeMinutes.value, "45", keyboardType = KeyboardType.Number) { vm.cookTimeMinutes.value = it }
                    }
                    Surface(modifier = Modifier.weight(1f).height(42.dp), color = Color(0xFFF0EDE9), shape = RoundedCornerShape(10.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("\uD83D\uDD12 Private", style = MaterialTheme.typography.bodySmall, color = Color(0xFF45483E))
                            Switch(
                                checked = vm.isPrivate.value,
                                onCheckedChange = { vm.isPrivate.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElachiGreen, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFFC5C8BA)),
                            )
                        }
                    }
                }

                ManualLabel("Method")
                PresetDropdown(vm.method.value, vm.methodOptions) { vm.method.value = it }
            }
        }

        // Allergens
        Column {
            ManualLabel("Allergens Present")
            AllergenFlow(vm)
        }

        // Ingredients card
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(2.4.dp, Color(0xFFE5E2DD))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Kitchen, contentDescription = null, tint = ElachiGreen, modifier = Modifier.size(18.dp))
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ElachiGreen)
                }
                Spacer(Modifier.height(12.dp))
                vm.ingredients.forEachIndexed { index, ingredient ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        ManualTextField(ingredient.name, "e.g. Flour", modifier = Modifier.weight(2f)) { ingredient.name = it }
                        ManualTextField(ingredient.quantity, "2", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal) { ingredient.quantity = it }
                        ManualTextField(ingredient.unit, "cups", modifier = Modifier.weight(1f)) { ingredient.unit = it }
                        Surface(modifier = Modifier.size(44.dp, 51.dp).clickable { vm.removeIngredientRow(index) }, color = Color(0xFFFFDAD6), shape = RoundedCornerShape(8.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color(0xFFBA1A1A), modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
                DashedButtonManual("Add Ingredient") { vm.addIngredientRow() }
            }
        }

        // Steps card
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(2.4.dp, Color(0xFFE5E2DD))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.FormatListNumbered, contentDescription = null, tint = ElachiGreen, modifier = Modifier.size(18.dp))
                    Text("Steps", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ElachiGreen)
                }
                Spacer(Modifier.height(12.dp))
                vm.steps.forEachIndexed { index, step ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(modifier = Modifier.padding(top = 10.dp).size(28.dp), color = ElachiGreenLight, shape = CircleShape) {
                            Box(contentAlignment = Alignment.Center) { Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        }
                        ManualTextField(step, "Describe this step...", modifier = Modifier.weight(1f), height = 74.dp) { vm.steps[index] = it }
                        Surface(modifier = Modifier.padding(top = 6.dp).size(44.dp, 51.dp).clickable { vm.removeStepRow(index) }, color = Color(0xFFFFDAD6), shape = RoundedCornerShape(8.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color(0xFFBA1A1A), modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
                DashedButtonManual("Add Step") { vm.addStepRow() }
            }
        }

        vm.errorMessage.value?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BottomActionBar(vm: AddRecipeViewModel, onCancel: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0EDE9)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Cancel", color = Color(0xFF45483E), fontWeight = FontWeight.W600)
                }
                Button(
                    onClick = { vm.save() },
                    enabled = !vm.isSaving.value && !vm.isUploadingPhoto.value,
                    modifier = Modifier.weight(2f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElachiGreenLight),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (vm.isSaving.value) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Save Recipe", fontWeight = FontWeight.W600)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
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
        Surface(
            modifier = Modifier.fillMaxWidth().height(51.dp).menuAnchor(),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(2.4.dp, Color(0xFFC5C8BA)),
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(selectedBook?.name ?: "Choose a book", style = MaterialTheme.typography.bodyMedium, color = ElachiTextPrimary)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF75786D))
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (books.isEmpty()) {
                DropdownMenuItem(text = { Text("No Recipe Books yet, create one from My Cookbook first") }, onClick = {}, enabled = false)
            }
            books.forEach { book ->
                DropdownMenuItem(text = { Text(book.name) }, onClick = { vm.selectedBookId.value = book.id; expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetDropdown(value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(51.dp).menuAnchor(),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(2.4.dp, Color(0xFFC5C8BA)),
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = ElachiTextPrimary)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF75786D))
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
    var customValue by remember { mutableStateOf("") }
    CustomAddRow(customValue, "Add custom value...") {
        customValue = it
    }
    LaunchedEffect(customValue) {
        if (customValue.isNotBlank()) onSelect(customValue)
    }
}

@Composable
private fun ManualTabItem(label: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), color = if (isSelected) ElachiGreen else ElachiTextSecondary)
        if (isSelected) {
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(2.4.dp).background(ElachiGreen))
        }
    }
}

@Composable
private fun ManualLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.88.sp),
        color = Color(0xFF45483E),
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun ManualTextField(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 51.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().height(height),
        placeholder = { Text(placeholder, color = ElachiTextSecondary, fontSize = 15.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFC5C8BA), focusedBorderColor = ElachiGreen, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
    )
}

@Composable
private fun CustomAddRow(value: String, placeholder: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    var local by remember { mutableStateOf("") }
    Row(modifier = modifier.fillMaxWidth().height(44.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        ManualTextField(local, placeholder, modifier = Modifier.weight(1f).height(38.dp)) { local = it }
        Box(
            modifier = Modifier.size(32.dp, 38.dp).background(ElachiGreenLight, RoundedCornerShape(8.dp))
                .clickable { if (local.isNotBlank()) { onValueChange(local); local = "" } },
            contentAlignment = Alignment.Center,
        ) {
            Text("+", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AllergenFlow(vm: AddRecipeViewModel) {
    vm.allergenOptions.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            row.forEach { allergen ->
                val isSelected = vm.selectedAllergens.contains(allergen)
                Surface(
                    modifier = Modifier.clickable { vm.toggleAllergen(allergen) },
                    color = if (isSelected) ElachiGreenLight else Color.White,
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(2.4.dp, if (isSelected) ElachiGreenLight else Color(0xFFC5C8BA)),
                ) {
                    Text(allergen, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else ElachiTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun DashedButtonManual(label: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(46.dp).clickable(onClick = onClick)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color(0xFFC5C8BA),
                style = Stroke(width = 2.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
            )
        }
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = ElachiGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = ElachiGreen)
        }
    }
}