package com.elachi.app.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.elachi.app.navigation.Screen

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val items = listOf(
    BottomItem(Screen.Home.route, "Home", Icons.Filled.Home),
    BottomItem(Screen.Cookbook.route, "Cookbook", Icons.Filled.MenuBook),
    BottomItem(Screen.Discover.route, "Discover", Icons.Filled.Explore),
    BottomItem(Screen.Pantry.route, "Pantry", Icons.Filled.Kitchen),
)

@Composable
fun ElachiBottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(72.dp),
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { 
                    Icon(
                        item.icon, 
                        contentDescription = item.label, 
                        modifier = Modifier.size(22.dp)
                    ) 
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
            )
        }
    }
}
