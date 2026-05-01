package com.kiran.movie.ui.saved

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiran.movie.core.ui.R
import com.kiran.movie.core.ui.components.EmptyStateScreen
import com.kiran.movie.core.ui.components.ItemCard
import com.kiran.movie.core.ui.details.ItemDetailsBottomSheet
import com.kiran.movie.data.models.Item
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    searchQuery: String,
    onListEmptyStateChange: (Boolean) -> Unit,
    onUpdateSearchHint: (String) -> Unit,
    innerPadding: PaddingValues,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val isMovieTab by viewModel.isMovieTab.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedItemForDetails by remember { mutableStateOf<Item?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(searchQuery) {
        viewModel.onEvent(SavedContract.Event.Search(searchQuery))
    }

    LaunchedEffect(isMovieTab) {
        if (isMovieTab) {
            onUpdateSearchHint(context.getString(R.string.search_hint_saved_movies))
        } else {
            onUpdateSearchHint(context.getString(R.string.search_hint_saved_tv_shows))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SavedContract.Effect.ShowToast -> {
                    Toasty.error(context, effect.message, Toast.LENGTH_SHORT, true).show()
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        when (val currentState = state) {
            is SavedContract.State.Loading -> {
                LaunchedEffect(Unit) { onListEmptyStateChange(true) }
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            is SavedContract.State.Error -> {
                LaunchedEffect(Unit) { onListEmptyStateChange(true) }
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = currentState.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is SavedContract.State.Success -> {
                LaunchedEffect(currentState.items.size) {
                    onListEmptyStateChange(currentState.items.isEmpty())
                }

                // Tab selector (always visible above content)
                val moviesTabScale by animateFloatAsState(
                    targetValue = if (isMovieTab) 1.12f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "moviesTabScale",
                )
                val tvTabScale by animateFloatAsState(
                    targetValue = if (!isMovieTab) 1.12f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "tvTabScale",
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 16.dp)
                                .padding(top = innerPadding.calculateTopPadding()),
                    ) {
                        Text(
                            text = stringResource(id = R.string.movies),
                            color = if (isMovieTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier =
                                Modifier
                                    .padding(end = 16.dp)
                                    .scale(moviesTabScale)
                                    .clickable { viewModel.onEvent(SavedContract.Event.ChangeTab(true)) },
                        )
                        Text(
                            text = stringResource(id = R.string.tv_shows),
                            color = if (!isMovieTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier =
                                Modifier
                                    .scale(tvTabScale)
                                    .clickable { viewModel.onEvent(SavedContract.Event.ChangeTab(false)) },
                        )
                    }

                    // Content slides in from correct direction on tab switch
                    AnimatedContent(
                        targetState = isMovieTab to currentState,
                        transitionSpec = {
                            if (targetState.first) {
                                (slideInHorizontally(tween(350)) { -it } + fadeIn(tween(300)))
                                    .togetherWith(slideOutHorizontally(tween(350)) { it } + fadeOut(tween(200)))
                            } else {
                                (slideInHorizontally(tween(350)) { it } + fadeIn(tween(300)))
                                    .togetherWith(slideOutHorizontally(tween(350)) { -it } + fadeOut(tween(200)))
                            }
                        },
                        label = "savedTabContent",
                    ) { (movieTab, state) ->
                        if (state.items.isEmpty()) {
                            EmptyStateScreen(
                                icon = Icons.Default.Star,
                                message = if (searchQuery.isNotEmpty()) "No saved items found for '$searchQuery'" else "No bookmarks yet",
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding =
                                    PaddingValues(
                                        bottom = innerPadding.calculateBottomPadding(),
                                    ),
                                modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
                            ) {
                                items(state.items) { item ->
                                    ItemCard(
                                        item = item,
                                        onBookmarkClick = {
                                            viewModel.onEvent(SavedContract.Event.ToggleBookmark(it))
                                        },
                                        onItemClick = { selectedItemForDetails = it },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedItemForDetails?.let { item ->
        ItemDetailsBottomSheet(
            item = item,
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) selectedItemForDetails = null
                }
            },
        )
    }
}
