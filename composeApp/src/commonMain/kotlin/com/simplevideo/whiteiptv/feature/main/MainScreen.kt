package com.simplevideo.whiteiptv.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.simplevideo.whiteiptv.feature.categories.CategoriesScreen
import com.simplevideo.whiteiptv.feature.favorites.FavoritesScreen
import com.simplevideo.whiteiptv.feature.home.HomeScreen
import com.simplevideo.whiteiptv.feature.settings.SettingsScreen
import com.simplevideo.whiteiptv.navigation.Route

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavItems,
                currentRoute = currentRoute,
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Route.Main.Home,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Route.Main.Home> { HomeScreen() }
            composable<Route.Main.Favorites> { FavoritesScreen() }
            composable<Route.Main.Categories> { CategoriesScreen() }
            composable<Route.Main.Settings> { SettingsScreen() }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (Route.Main) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route::class.qualifiedName,
                onClick = { onItemClick(item.route) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}

private val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        icon = Icons.Default.Home,
        route = Route.Main.Home
    ),
    BottomNavItem(
        title = "Favorites",
        icon = Icons.Default.Favorite,
        route = Route.Main.Favorites
    ),
    BottomNavItem(
        title = "Categories",
        icon = Icons.Default.Category,
        route = Route.Main.Categories
    ),
    BottomNavItem(
        title = "Settings",
        icon = Icons.Default.Settings,
        route = Route.Main.Settings
    )
)

private data class BottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: Route.Main
)
