package com.kiran.movie.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kiran.movie.R
import com.kiran.movie.core.ui.MainViewModel
import com.kiran.movie.ui.movies.MoviesScreen
import com.kiran.movie.ui.saved.SavedScreen
import com.kiran.movie.ui.tvshows.TvShowsScreen

sealed class Screen(val route: String, val titleRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Movies : Screen("movies", R.string.title_movies, Icons.Filled.Home)
    object TvShows : Screen("tv_shows", R.string.title_tv_shows, Icons.AutoMirrored.Filled.List)
    object Saved : Screen("saved", R.string.title_saved, Icons.Filled.Favorite)
}

val items = listOf(
    Screen.Movies,
    Screen.TvShows,
    Screen.Saved
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var isSearchVisible by remember { mutableStateOf(false) }
    val searchQuery by mainViewModel.searchQuery.collectAsState()

    // Hide search on destination change
    LaunchedEffect(currentDestination?.route) {
        isSearchVisible = false
        mainViewModel.updateSearchQuery("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Movie App") },
                actions = {
                    AnimatedVisibility(visible = isSearchVisible) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { mainViewModel.updateSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(0.6f),
                            placeholder = { Text("Search...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(28.dp)
                            .clickable {
                                isSearchVisible = !isSearchVisible
                                if (!isSearchVisible) {
                                    mainViewModel.updateSearchQuery("")
                                }
                            },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.titleRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = Screen.Movies.route) {
                composable(Screen.Movies.route) { MoviesScreen(mainViewModel) }
                composable(Screen.TvShows.route) { TvShowsScreen(mainViewModel) }
                composable(Screen.Saved.route) { SavedScreen(mainViewModel) }
            }
        }
    }
}
