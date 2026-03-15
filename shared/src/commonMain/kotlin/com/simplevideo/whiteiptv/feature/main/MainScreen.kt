package com.simplevideo.whiteiptv.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.simplevideo.whiteiptv.feature.channels.ChannelsScreen
import com.simplevideo.whiteiptv.feature.favorites.FavoritesScreen
import com.simplevideo.whiteiptv.feature.home.HomeScreen
import com.simplevideo.whiteiptv.feature.settings.SettingsScreen
import com.simplevideo.whiteiptv.navigation.Route.MainTab
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MainScreen(
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavItems,
                currentDestination = currentDestination,
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
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
            composable<MainTab.Home> {
                HomeScreen(
                    onNavigateToFavorites = {
                        navController.navigate(MainTab.Favorites) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToChannels = { groupId ->
                        navController.navigate(MainTab.Channels(groupId = groupId)) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onNavigateToPlayer = onNavigateToPlayer,
                    onNavigateToOnboarding = onNavigateToOnboarding,
                )
            }
            composable<MainTab.Favorites> {
                FavoritesScreen(
                    onNavigateToPlayer = onNavigateToPlayer,
                )
            }
            composable<MainTab.Channels> {
                ChannelsScreen(
                    onNavigateToPlayer = onNavigateToPlayer,
                )
            }
            composable<MainTab.Settings> { SettingsScreen() }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    items: ImmutableList<BottomNavItem>,
    currentDestination: androidx.navigation.NavDestination?,
    onItemClick: (MainTab) -> Unit,
) {
    NavigationBar {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(item.route::class)
            } == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onItemClick(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                    )
                },
                label = { Text(item.title) },
            )
        }
    }
}

private val bottomNavItems = persistentListOf(
    BottomNavItem(
        title = "Home",
        selectedIcon = Icons.Outlined.Home,
        unselectedIcon = Icons.Outlined.Home,
        route = MainTab.Home,
    ),
    BottomNavItem(
        title = "Favorites",
        selectedIcon = Icons.Outlined.FavoriteBorder,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
        route = MainTab.Favorites,
    ),
    BottomNavItem(
        title = "Channels",
        selectedIcon = Icons.Outlined.Tv,
        unselectedIcon = Icons.Outlined.Tv,
        route = MainTab.Channels(),
    ),
    BottomNavItem(
        title = "Settings",
        selectedIcon = Icons.Outlined.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        route = MainTab.Settings,
    ),
)

private data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: MainTab,
)
