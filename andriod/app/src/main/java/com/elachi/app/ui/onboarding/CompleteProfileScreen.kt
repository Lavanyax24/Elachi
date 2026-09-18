package com.elachi.app.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.elachi.app.data.remote.SupabaseStorageClient
import com.elachi.app.data.repository.ProfileRepository
import com.elachi.app.ui.theme.ElachiGreen
import com.elachi.app.ui.theme.ElachiGreenLight
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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

    /** Loads existing values when opened for editing. */
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

/**
 * Used both for onboarding's "Complete Your Profile" step and for editing
 * from Profile later — isEditMode controls the small differences (top bar,
 * loading existing data first, button label).
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = { if (isEditMode) TopAppBar(title = { Text("Edit Profile") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isEditMode) {
                Text(
                    "Complete Your Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElachiGreen,
                )
                Text("Tell us a bit about yourself", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
            }

            // Avatar upload
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    viewModel.isUploadingPhoto.value -> CircularProgressIndicator()
                    viewModel.avatarUrl.value != null -> AsyncImage(
                        model = viewModel.avatarUrl.value,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> androidx.compose.foundation.Image(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Add photo",
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Text(
                "Add Photo",
                style = MaterialTheme.typography.labelLarge,
                color = ElachiGreen,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(20.dp))

            // Display name
            OutlinedTextField(
                value = viewModel.displayName.value,
                onValueChange = { viewModel.displayName.value = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(12.dp))

            // Bio
            OutlinedTextField(
                value = viewModel.bio.value,
                onValueChange = { viewModel.bio.value = it },
                label = { Text("Bio") },
                placeholder = { Text("Tell us about your cooking style...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(20.dp))

            // Cooking interests
            Text(
                "Cooking Interests",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(8.dp))

            // Preset chips
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
                        )
                    }
                }
            }

            // Custom interest input
            var customInterest by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = customInterest,
                    onValueChange = { customInterest = it },
                    placeholder = { Text("Add your own (e.g. Persian, BBQ...)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.addCustomInterest(customInterest)
                        customInterest = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElachiGreenLight),
                    modifier = Modifier.height(52.dp),
                ) {
                    androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }

            // Show any custom interests that were added
            if (viewModel.selectedInterests.any { it !in viewModel.cookingInterestOptions }) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    viewModel.selectedInterests
                        .filter { it !in viewModel.cookingInterestOptions }
                        .forEach { custom ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.toggleInterest(custom) },
                                label = { Text(custom) },
                            )
                        }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Dietary restrictions
            Text(
                "Dietary Restrictions & Allergies",
                style = MaterialTheme.typography.titleMedium,
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
                        )
                    }
                }
            }

            // Custom dietary input
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
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.addCustomDietary(customDietary)
                        customDietary = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElachiGreenLight),
                    modifier = Modifier.height(52.dp),
                ) {
                    androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }

            viewModel.errorMessage.value?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            // Save
            Button(
                onClick = { viewModel.save() },
                enabled = !viewModel.isSaving.value,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElachiGreen),
            ) {
                if (viewModel.isSaving.value) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        if (isEditMode) "Save" else "Continue",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (!isEditMode) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDone) { Text("Skip for now") }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}