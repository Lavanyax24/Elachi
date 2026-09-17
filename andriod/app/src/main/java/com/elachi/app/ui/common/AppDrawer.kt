package com.elachi.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elachi.app.ui.theme.ElachiGreen
import com.elachi.app.ui.theme.ElachiTextPrimary
import com.elachi.app.ui.theme.ElachiTextSecondary

private data class DrawerItem(val label: String, val icon: ImageVector, val route: String)

private val mainItems = listOf(
    DrawerItem("Home", Icons.Filled.Home, "home"),
    DrawerItem("My Profile", Icons.Filled.Person, "profile"),
    DrawerItem("My Cookbook", Icons.AutoMirrored.Filled.MenuBook, "cookbook"),
    DrawerItem("Discover", Icons.Filled.Explore, "discover"),
    DrawerItem("Pantry", Icons.Filled.Kitchen, "pantry"),
)

private val extraItems = listOf(
    DrawerItem("AI Chef Assistant", Icons.Filled.SmartToy, "ai_chef"),
    DrawerItem("Achievements", Icons.Filled.EmojiEvents, "achievements"),
    DrawerItem("Streak Calendar", Icons.Filled.CalendarMonth, "streak_calendar"),
)

private val systemItems = listOf(
    DrawerItem("Settings", Icons.Filled.Settings, "settings"),
    DrawerItem("Help & Support", Icons.AutoMirrored.Filled.Help, "help"),
)

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    userDisplayName: String,
    userEmail: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFFFCF9F4),
        drawerTonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Column {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                            .background(ElachiGreen.copy(alpha = 0.1f))
                            .border(1.dp, ElachiGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("\uD83D\uDC64", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        userDisplayName.ifBlank { "Chef" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ElachiTextPrimary,
                    )
                    Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = ElachiTextSecondary)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E2DD))
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                mainItems.forEach { DrawerRow(it, currentRoute, onNavigate) }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color(0xFFE5E2DD).copy(alpha = 0.5f),
                )
                extraItems.forEach { DrawerRow(it, currentRoute, onNavigate) }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color(0xFFE5E2DD).copy(alpha = 0.5f),
                )
                systemItems.forEach { DrawerRow(it, currentRoute, onNavigate) }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E2DD))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onLogout),
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        modifier = Modifier.size(22.dp),
                        tint = Color(0xFFBA1A1A),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Logout",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFBA1A1A),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DrawerRow(item: DrawerItem, currentRoute: String?, onNavigate: (String) -> Unit) {
    val isSelected = item.route == currentRoute
    val contentColor = if (isSelected) ElachiGreen else ElachiTextPrimary
    val backgroundColor = if (isSelected) ElachiGreen.copy(alpha = 0.1f) else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onNavigate(item.route) },
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp), tint = contentColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                item.label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = contentColor,
            )
        }
    }
}