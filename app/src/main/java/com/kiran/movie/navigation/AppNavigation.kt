package com.kiran.movie.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kiran.movie.core.ui.MainViewModel
import com.kiran.movie.core.ui.R
import com.kiran.movie.ui.movies.MoviesScreen
import com.kiran.movie.ui.saved.SavedScreen
import com.kiran.movie.ui.tvshows.TvShowsScreen
import kotlin.math.roundToInt

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

    var bottomBarHeightPx by remember { mutableFloatStateOf(0f) }
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val searchQuery by mainViewModel.searchQuery.collectAsState()
    val searchHint by mainViewModel.searchHint.collectAsState()
    val isListEmpty by mainViewModel.isListEmpty.collectAsState()
    val context = LocalContext.current

    // Clear search on destination change and reset scroll offsets
    LaunchedEffect(currentDestination?.route) {
        mainViewModel.updateSearchQuery("")
        when (currentDestination?.route) {
            Screen.Movies.route -> mainViewModel.updateSearchHint(context.getString(R.string.search_hint_movies))
            Screen.TvShows.route -> mainViewModel.updateSearchHint(context.getString(R.string.search_hint_tv_shows))
        }
        bottomBarOffsetHeightPx = 0f
    }

    LaunchedEffect(isListEmpty) {
        if (isListEmpty) {
            bottomBarOffsetHeightPx = 0f
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isListEmpty) return Offset.Zero
                
                val delta = available.y

                val newBottomOffset = bottomBarOffsetHeightPx - delta
                bottomBarOffsetHeightPx = newBottomOffset.coerceIn(0f, bottomBarHeightPx)

                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                androidx.compose.material3.SearchBar(
                    query = searchQuery,
                    onQueryChange = { mainViewModel.updateSearchQuery(it) },
                    onSearch = { },
                    active = false,
                    onActiveChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(searchHint) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") }
                ) {}
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .onSizeChanged { bottomBarHeightPx = it.height.toFloat() }
                    .offset { IntOffset(x = 0, y = bottomBarOffsetHeightPx.roundToInt()) },
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
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(navController, startDestination = Screen.Movies.route) {
                composable(Screen.Movies.route) {
                    MoviesScreen(
                        searchQuery = searchQuery,
                        onListEmptyStateChange = { mainViewModel.updateIsListEmpty(it) },
                        innerPadding = innerPadding
                    )
                }
                composable(Screen.TvShows.route) {
                    TvShowsScreen(
                        searchQuery = searchQuery,
                        onListEmptyStateChange = { mainViewModel.updateIsListEmpty(it) },
                        innerPadding = innerPadding
                    )
                }
                composable(Screen.Saved.route) {
                    SavedScreen(
                        searchQuery = searchQuery,
                        onListEmptyStateChange = { mainViewModel.updateIsListEmpty(it) },
                        onUpdateSearchHint = { mainViewModel.updateSearchHint(it) },
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}
