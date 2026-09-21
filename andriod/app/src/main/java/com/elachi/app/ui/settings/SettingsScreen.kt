package com.elachi.app.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elachi.app.ui.common.ElachiTopBar
import com.elachi.app.ui.theme.ElachiCream
import com.elachi.app.ui.theme.ElachiGreen
import com.elachi.app.ui.theme.ElachiTextPrimary
import com.elachi.app.ui.theme.ElachiTextSecondary

/**
 * Redesigned Settings screen — grouped sections with icon-led rows
 * and a proper Account block at the bottom.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLoggedOut: () -> Unit,
    onBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = remember(context) { context.applicationContext as com.elachi.app.ElachiApp }
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { ElachiTopBar(title = "Settings", onBackClick = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // ---------- APPEARANCE ----------
            SettingsSectionHeader("Appearance")
            SettingsCard {
                RowSetting(
                    icon = Icons.Filled.DarkMode,
                    title = "Dark Theme",
                    subtitle = if (isDarkTheme) "Enabled" else "Disabled",
                    trailing = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                uncheckedBorderColor = Color.Transparent,
                                uncheckedIconColor = Color.Transparent
                            ),
                        )
                    },
                )
            }

            // ---------- LANGUAGE (disabled) ----------
            SettingsSectionHeader("Language")
            SettingsCard {
                RowSetting(
                    icon = Icons.Filled.Language,
                    title = "Display Language",
                    subtitle = "English — more coming in final version",
                    trailing = {
                        Text(
                            "Coming soon",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    },
                    enabled = false,
                )
            }

            // ---------- SECURITY (disabled) ----------
            SettingsSectionHeader("Security")
            SettingsCard {
                RowSetting(
                    icon = Icons.Filled.Lock,
                    title = "Biometric Lock",
                    subtitle = "Coming in final version",
                    trailing = {
                        Switch(
                            checked = false,
                            onCheckedChange = null,
                            enabled = false,
                        )
                    },
                    enabled = false,
                )
            }

            // ---------- NOTIFICATIONS (disabled) ----------
            SettingsSectionHeader("Notifications")
            SettingsCard {
                RowSetting(
                    icon = Icons.Filled.Notifications,
                    title = "Push Notifications",
                    subtitle = "Comment, friend and share alerts",
                    trailing = {
                        Switch(
                            checked = false,
                            onCheckedChange = null,
                            enabled = false,
                        )
                    },
                    enabled = false,
                )
            }

            // ---------- ACCOUNT ----------
            SettingsSectionHeader("Account")
            SettingsCard {
                RowSetting(
                    icon = Icons.Filled.Person,
                    title = "Edit Profile",
                    onClick = onNavigateToEditProfile,
                )
                CardDivider()
                RowSetting(
                    icon = Icons.Filled.Delete,
                    title = "Clear Cached Data",
                    subtitle = "Keeps your account, removes local cache",
                    onClick = { showClearDataDialog = true },
                )
                CardDivider()
                RowSetting(
                    icon = Icons.Filled.Delete,
                    title = "Delete Account",
                    subtitle = "Permanently removes your account and data",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteAccountDialog = true },
                )
                CardDivider()
                RowSetting(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { showLogoutDialog = true },
                )
            }

            // ---------- ABOUT ----------
            SettingsSectionHeader("About")
            SettingsCard {
                RowSetting(
                    icon = Icons.Filled.Info,
                    title = "App Version",
                    trailing = {
                        Text("v1.0.0-part2", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    },
                    enabled = false,
                )
                CardDivider()
                RowSetting(
                    icon = Icons.Filled.Description,
                    title = "Privacy Policy",
                    onClick = onNavigateToPrivacyPolicy,
                )
                CardDivider()
                RowSetting(
                    icon = Icons.Filled.Description,
                    title = "Terms of Service",
                    onClick = onNavigateToTerms,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ---------- Dialogs ----------
    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Logout?",
            message = "You'll need to sign in again to access your recipes.",
            confirmLabel = "Logout",
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout(onLoggedOut)
            },
            onDismiss = { showLogoutDialog = false },
        )
    }

    if (showClearDataDialog) {
        ConfirmDialog(
            title = "Clear cached data?",
            message = "Local cache will be cleared. Your account and cloud data are not affected.",
            confirmLabel = "Clear",
            onConfirm = { showClearDataDialog = false },
            onDismiss = { showClearDataDialog = false },
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmDialog(
            title = "Delete account?",
            message = "This permanently deletes your account and all your recipes. This cannot be undone.",
            confirmLabel = "Delete",
            confirmColor = Color(0xFFBA1A1A),
            onConfirm = {
                showDeleteAccountDialog = false
                viewModel.deleteAccount(app.api, onLoggedOut)
            },
            onDismiss = { showDeleteAccountDialog = false },
        )
    }
}

// ---------- Building blocks ----------

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 11.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column { content() }
    }
}

@Composable
private fun CardDivider() {
    Divider(
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(start = 56.dp),
    )
}

@Composable
private fun RowSetting(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)

    Row(
        modifier = rowModifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = confirmColor, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}
