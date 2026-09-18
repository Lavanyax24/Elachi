package com.elachi.app.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elachi.app.data.remote.SupabaseStorageClient
import com.elachi.app.ui.theme.ElachiGreen
import com.elachi.app.ui.theme.ElachiGreenLight
import kotlinx.coroutines.launch

internal val ICON_OPTIONS = listOf("📖", "🏠", "⚡", "🍜", "🌮", "🎂", "🥗", "🔍", "🎯", "🌍")
internal val COLOUR_OPTIONS = listOf(
    "#2F5233", "#8A4B2A", "#3B82F6", "#9333EA",
    "#DC2626", "#0F766E", "#EA580C",
)

/**
 * Onboarding step, "Let's Get Organised!" — a new user creates their first
 * Recipe Book. The icon + colour picker below is the same one used in the
 * in-app New Book dialog so both entry points look identical.
 */
@Composable
fun CreateFirstBookScreen(
    onCreated: (bookName: String, description: String?, coverImageUrl: String?, icon: String, colour: String) -> Unit,
    onSkip: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(ICON_OPTIONS.first()) }
    var selectedColour by remember { mutableStateOf(COLOUR_OPTIONS.first()) }
    var coverImageUrl by remember { mutableStateOf<String?>(null) }
    var localPreviewUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            localPreviewUri = uri
            isUploading = true
            scope.launch {
                SupabaseStorageClient.uploadImage(context, uri, folder = "book-covers")
                    .onSuccess { url -> coverImageUrl = url }
                isUploading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            "Let's Get Organised!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = ElachiGreen,
        )
        Text(
            "Create your first Recipe Book to start saving recipes",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF45483E),
        )

        Spacer(Modifier.height(24.dp))

        // Book Name
        Text(
            "BOOK NAME",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.88.sp),
            color = Color(0xFF45483E),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("My Family Recipes") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(Modifier.height(12.dp))

        // Description
        Text(
            "DESCRIPTION (OPTIONAL)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.88.sp),
            color = Color(0xFF45483E),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Add a description...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(Modifier.height(16.dp))

        // Icon picker
        Text(
            "ICON",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.88.sp),
            color = Color(0xFF45483E),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ICON_OPTIONS.take(5).forEach { icon ->
                IconChip(icon = icon, selected = selectedIcon == icon) { selectedIcon = icon }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ICON_OPTIONS.drop(5).forEach { icon ->
                IconChip(icon = icon, selected = selectedIcon == icon) { selectedIcon = icon }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Colour picker
        Text(
            "COLOUR",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.88.sp),
            color = Color(0xFF45483E),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            COLOUR_OPTIONS.forEach { hex ->
                ColourSwatch(hex = hex, selected = selectedColour == hex) { selectedColour = hex }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Cover photo
        Text(
            "COVER PHOTO (OPTIONAL)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.88.sp),
            color = Color(0xFF45483E),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0EDE9))
                .clickable {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                isUploading -> CircularProgressIndicator()
                localPreviewUri != null -> AsyncImage(
                    model = localPreviewUri,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = ElachiGreen)
                    Spacer(Modifier.height(6.dp))
                    Text("Tap to upload", style = MaterialTheme.typography.bodySmall, color = ElachiGreen)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Create button
        Button(
            onClick = {
                onCreated(
                    name.ifBlank { "My Cookbook" },
                    description.ifBlank { null },
                    coverImageUrl,
                    selectedIcon,
                    selectedColour,
                )
            },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElachiGreen),
        ) {
            Text("Create Book", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now", color = Color(0xFF75786D))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun IconChip(icon: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) ElachiGreen else Color(0xFFF0EDE9))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) ElachiGreen else Color(0xFFE5E2DD),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(icon, fontSize = 24.sp)
    }
}

@Composable
internal fun ColourSwatch(hex: String, selected: Boolean, onClick: () -> Unit) {
    val colour = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(ElachiGreen)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colour)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) ElachiGreenLight else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}