package com.elachi.app.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.elachi.app.data.remote.SupabaseStorageClient
import com.elachi.app.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class CompleteProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {
    var displayName = mutableStateOf("")
    var bio = mutableStateOf("")
    var avatarUrl = mutableStateOf<String?>(null)
    var isUploadingPhoto = mutableStateOf(false)
    var isSaving = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var saveComplete = mutableStateOf(false)

    val cookingInterestOptions = listOf(
        "Vegan", "Keto", "Gluten-Free", "Baking", "Grilling", "Italian",
        "Mexican", "Asian", "Quick Meals", "Healthy", "Comfort Food", "Vegetarian",
    )
    val selectedInterests = mutableStateListOf<String>()

    val dietaryOptions = listOf(
        "Nuts", "Dairy", "Gluten", "Soy", "Eggs", "Shellfish", "Fish", "Peanuts", "Sesame",
    )
    val selectedDietary = mutableStateListOf<String>()

    fun toggleInterest(v: String) {
        if (selectedInterests.contains(v)) selectedInterests.remove(v) else selectedInterests.add(v)
    }

    fun addCustomInterest(v: String) {
        val trimmed = v.trim()
        if (trimmed.isNotBlank() && !selectedInterests.contains(trimmed)) {
            selectedInterests.add(trimmed)
        }
    }

    fun toggleDietary(v: String) {
        if (selectedDietary.contains(v)) selectedDietary.remove(v) else selectedDietary.add(v)
    }

    fun addCustomDietary(v: String) {
        val trimmed = v.trim()
        if (trimmed.isNotBlank() && !selectedDietary.contains(trimmed)) {
            selectedDietary.add(trimmed)
        }
    }

    fun uploadPhoto(context: android.content.Context, uri: android.net.Uri) {
        isUploadingPhoto.value = true
        viewModelScope.launch {
            SupabaseStorageClient.uploadImage(context, uri, folder = "avatars")
                .onSuccess { url -> avatarUrl.value = url }
                .onFailure { e -> errorMessage.value = e.message }
            isUploadingPhoto.value = false
        }
    }

    fun loadExisting() {
        viewModelScope.launch {
            profileRepository.getMyProfile().onSuccess { profile ->
                displayName.value = profile.displayName
                bio.value = profile.bio ?: ""
                avatarUrl.value = profile.avatarUrl
                selectedInterests.clear(); selectedInterests.addAll(profile.cookingInterests)
                selectedDietary.clear(); selectedDietary.addAll(profile.dietaryRestrictions)
            }
        }
    }

    fun save() {
        isSaving.value = true
        errorMessage.value = null
        viewModelScope.launch {
            profileRepository.updateProfile(
                displayName = displayName.value.ifBlank { null },
                bio = bio.value.ifBlank { null },
                avatarUrl = avatarUrl.value,
                cookingInterests = selectedInterests.toList(),
                dietaryRestrictions = selectedDietary.toList(),
            ).onSuccess {
                isSaving.value = false
                saveComplete.value = true
            }.onFailure { e ->
                isSaving.value = false
                errorMessage.value = e.message
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CompleteProfileScreen(
    viewModel: CompleteProfileViewModel,
    isEditMode: Boolean = false,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.uploadPhoto(context, uri)
    }

    LaunchedEffect(Unit) { if (isEditMode) viewModel.loadExisting() }
    LaunchedEffect(viewModel.saveComplete.value) {
        if (viewModel.saveComplete.value) onDone()
    }

    Scaffold(
        topBar = {
            if (isEditMode) {
                TopAppBar(
                    title = { Text("Edit Profile") },
                    navigationIcon = {
                        IconButton(onClick = onDone) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isEditMode) {
                Text(
                    "Complete Your Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Tell us a bit about yourself",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
            }

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    viewModel.isUploadingPhoto.value -> CircularProgressIndicator()
                    viewModel.avatarUrl.value != null -> AsyncImage(model = viewModel.avatarUrl.value, contentDescription = "Avatar", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else -> Icon(imageVector = Icons.Filled.Person, contentDescription = "Add photo", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "Add Photo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = viewModel.displayName.value,
                onValueChange = { viewModel.displayName.value = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.bio.value,
                onValueChange = { viewModel.bio.value = it },
                label = { Text("Bio") },
                placeholder = { Text("Tell us about your cooking style...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.height(20.dp))

            Text(
                "Cooking Interests",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(8.dp))

            viewModel.cookingInterestOptions.chunked(3).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                ) {
                    row.forEach { option ->
                        FilterChip(
                            selected = viewModel.selectedInterests.contains(option),
                            onClick = { viewModel.toggleInterest(option) },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            var customInterest by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = customInterest,
                    onValueChange = { customInterest = it },
                    placeholder = { Text("Add your own...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.addCustomInterest(customInterest); customInterest = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
                }
            }

            if (viewModel.selectedInterests.any { it !in viewModel.cookingInterestOptions }) {
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    viewModel.selectedInterests.filter { it !in viewModel.cookingInterestOptions }.forEach { custom ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.toggleInterest(custom) },
                            label = { Text(custom) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Dietary Restrictions & Allergies",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(8.dp))

            viewModel.dietaryOptions.chunked(3).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                ) {
                    row.forEach { option ->
                        FilterChip(
                            selected = viewModel.selectedDietary.contains(option),
                            onClick = { viewModel.toggleDietary(option) },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            var customDietary by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = customDietary,
                    onValueChange = { customDietary = it },
                    placeholder = { Text("Add your own...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.addCustomDietary(customDietary); customDietary = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
                }
            }

            if (viewModel.selectedDietary.any { it !in viewModel.dietaryOptions }) {
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    viewModel.selectedDietary.filter { it !in viewModel.dietaryOptions }.forEach { custom ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.toggleDietary(custom) },
                            label = { Text(custom) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            viewModel.errorMessage.value?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.save() },
                enabled = !viewModel.isSaving.value,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                if (viewModel.isSaving.value) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(if (isEditMode) "Save Changes" else "Continue", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (isEditMode) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            if (!isEditMode) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDone) {
                    Text("Skip for now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}