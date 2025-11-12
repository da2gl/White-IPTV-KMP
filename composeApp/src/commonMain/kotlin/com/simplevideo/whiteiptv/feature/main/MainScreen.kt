package com.simplevideo.whiteiptv.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.simplevideo.whiteiptv.feature.channels.ChannelsScreen
import com.simplevideo.whiteiptv.feature.favorites.FavoritesScreen
import com.simplevideo.whiteiptv.feature.home.HomeScreen
import com.simplevideo.whiteiptv.feature.settings.SettingsScreen
import com.simplevideo.whiteiptv.navigation.Route.MainTab

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
                },
            )
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Home,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable<MainTab.Home> { HomeScreen() }
            composable<MainTab.Favorites> { FavoritesScreen() }
            composable<MainTab.Channels> { ChannelsScreen() }
            composable<MainTab.Settings> { SettingsScreen() }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (MainTab) -> Unit,
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route::class.qualifiedName,
                onClick = { onItemClick(item.route) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
            )
        }
    }
}

private val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        icon = Icons.Default.Home,
        route = MainTab.Home,
    ),
    BottomNavItem(
        title = "Favorites",
        icon = Icons.Default.Favorite,
        route = MainTab.Favorites,
    ),
    BottomNavItem(
        title = "Channels",
        icon = Icons.Default.Tv,
        route = MainTab.Channels,
    ),
    BottomNavItem(
        title = "Settings",
        icon = Icons.Default.Settings,
        route = MainTab.Settings,
    ),
)

private data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: MainTab,
)
