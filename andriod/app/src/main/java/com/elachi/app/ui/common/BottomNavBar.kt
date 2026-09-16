package com.elachi.app.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.elachi.app.navigation.Screen
import com.elachi.app.ui.theme.ElachiCream
import com.elachi.app.ui.theme.ElachiGreen
import androidx.compose.ui.unit.dp

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

    NavigationBar(containerColor = ElachiCream) {
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
                icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp)) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElachiGreen,
                    selectedTextColor = ElachiGreen,
                    indicatorColor = ElachiGreen.copy(alpha = 0.1f),
                ),
            )
        }
    }
}