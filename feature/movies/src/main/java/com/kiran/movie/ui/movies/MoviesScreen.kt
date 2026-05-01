package com.kiran.movie.ui.movies

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.kiran.movie.core.ui.R
import com.kiran.movie.core.ui.components.EmptyStateScreen
import com.kiran.movie.core.ui.components.ItemCard
import com.kiran.movie.core.ui.details.ItemDetailsBottomSheet
import com.kiran.movie.core.ui.models.MovieLanguage
import com.kiran.movie.core.ui.models.MovieSortOrder
import com.kiran.movie.data.models.Item
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MoviesScreen(
    searchQuery: String,
    onListEmptyStateChange: (Boolean) -> Unit,
    innerPadding: PaddingValues,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedSortOrder by viewModel.selectedSortOrder.collectAsState()
    val carouselItemsList by viewModel.carouselItems.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var selectedItemForDetails by remember { mutableStateOf<Item?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(searchQuery) {
        viewModel.onEvent(MoviesContract.Event.Search(searchQuery))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MoviesContract.Effect.ShowToast -> {
                    Toasty.error(context, effect.message, Toast.LENGTH_SHORT, true).show()
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.onEvent(MoviesContract.Event.RefreshBookmarks)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        when (val currentState = state) {
            is MoviesContract.State.Loading -> {
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

            is MoviesContract.State.Error -> {
                LaunchedEffect(Unit) { onListEmptyStateChange(true) }
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = currentState.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is MoviesContract.State.Success -> {
                val lazyPagingItems = currentState.pagingDataFlow.collectAsLazyPagingItems()

                LaunchedEffect(lazyPagingItems.itemCount) {
                    onListEmptyStateChange(lazyPagingItems.itemCount == 0)
                }

                if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh !is LoadState.Loading) {
                    val errorMsg = if (lazyPagingItems.loadState.refresh is LoadState.Error) {
                        "Error: " + (lazyPagingItems.loadState.refresh as LoadState.Error).error.message
                    } else if (searchQuery.isNotEmpty()) {
                        "No movies found for '$searchQuery'"
                    } else {
                        "No movies found"
                    }
                    EmptyStateScreen(
                        icon = Icons.Default.Search,
                        message = errorMsg,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    key(selectedLanguage, selectedSortOrder) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding =
                                PaddingValues(
                                    top = 8.dp + innerPadding.calculateTopPadding(),
                                    bottom = innerPadding.calculateBottomPadding(),
                                ),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
                        ) {
                            // Filter chips
                            item(span = {
                                androidx.compose.foundation.lazy.grid
                                    .GridItemSpan(maxLineSpan)
                            }) {
                                Column {
                                    // Language Filter
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    ) {
                                        items(MovieLanguage.entries.toList()) { language ->
                                            FilterChip(
                                                selected = selectedLanguage == language,
                                                onClick = {
                                                    viewModel.onEvent(MoviesContract.Event.SelectLanguage(language))
                                                },
                                                label = { Text(language.displayName) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                ),
                                            )
                                        }
                                    }
                                    // Sort Order Filter
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    ) {
                                        items(MovieSortOrder.entries.toList()) { sortOrder ->
                                            FilterChip(
                                                selected = selectedSortOrder == sortOrder,
                                                onClick = {
                                                    viewModel.onEvent(MoviesContract.Event.SelectSortOrder(sortOrder))
                                                },
                                                label = { Text(sortOrder.displayName) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                                ),
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic header with animated slide transition
                            item(span = {
                                androidx.compose.foundation.lazy.grid
                                    .GridItemSpan(maxLineSpan)
                            }) {
                                AnimatedContent(
                                    targetState = selectedLanguage to selectedSortOrder,
                                    transitionSpec = {
                                        (slideInVertically(tween(300)) { it } + fadeIn(tween(300)))
                                            .togetherWith(slideOutVertically(tween(300)) { -it } + fadeOut(tween(200)))
                                    },
                                    label = "filterHeader",
                                ) { (language, sort) ->
                                    Text(
                                        text = "${language.displayName} Movies • ${sort.displayName}",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                    )
                                }
                            }

                            val currentItemCount = lazyPagingItems.itemCount
                            val showCarousel = currentItemCount >= 2 && carouselItemsList.isNotEmpty()
                            val totalCount = currentItemCount + (if (showCarousel) 1 else 0)

                            items(
                                count = totalCount,
                                span = { index ->
                                    if (showCarousel && index == 2) {
                                        androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)
                                    } else {
                                        androidx.compose.foundation.lazy.grid.GridItemSpan(1)
                                    }
                                }
                            ) { index ->
                                if (showCarousel && index == 2) {
                                    // Carousel Item
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                        Text(
                                            text = "Upcoming Highlights",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(bottom = 8.dp),
                                        )
                                        key(carouselItemsList) {
                                            val carouselState =
                                                androidx.compose.material3.carousel
                                                    .rememberCarouselState { carouselItemsList.size }
                                            androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel(
                                                state = carouselState,
                                                preferredItemWidth = 180.dp,
                                                itemSpacing = 12.dp,
                                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                            ) { i ->
                                                val carouselItem = carouselItemsList.getOrNull(i) ?: return@HorizontalMultiBrowseCarousel
                                                val isBookmarked = bookmarkedIds.contains(carouselItem.id)
                                                val carouselInteractionSource = remember(carouselItem.id) { MutableInteractionSource() }
                                                val isCarouselPressed by carouselInteractionSource.collectIsPressedAsState()
                                                val carouselItemScale by animateFloatAsState(
                                                    targetValue = if (isCarouselPressed) 0.93f else 1f,
                                                    animationSpec =
                                                        spring(
                                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                                            stiffness = Spring.StiffnessMedium,
                                                        ),
                                                    label = "carouselItemScale",
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .scale(carouselItemScale)
                                                        .maskClip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                                        .clickable(
                                                            interactionSource = carouselInteractionSource,
                                                            indication = null,
                                                            onClick = { selectedItemForDetails = carouselItem },
                                                        )
                                                ) {
                                                    coil.compose.AsyncImage(
                                                        model = "${com.kiran.movie.core.ui.BuildConfig.BASE_IMAGE_URL}${carouselItem.posterPath}",
                                                        contentDescription = carouselItem.title,
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.onEvent(MoviesContract.Event.ToggleBookmark(carouselItem))
                                                        },
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(12.dp)
                                                            .background(
                                                                color = Color.Black.copy(alpha = 0.35f),
                                                                shape = CircleShape
                                                            )
                                                            .size(36.dp)
                                                    ) {
                                                        androidx.compose.material3.Icon(
                                                            painter = painterResource(
                                                                id = if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_un_bookmarked
                                                            ),
                                                            contentDescription = "Bookmark",
                                                            tint = if (isBookmarked) Color(0xFFFFC107) else Color.White,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Paging Item
                                    val pagingIndex = if (showCarousel && index > 2) index - 1 else index
                                    if (pagingIndex >= 0 && pagingIndex < lazyPagingItems.itemCount) {
                                        val item = lazyPagingItems[pagingIndex]
                                        if (item != null) {
                                            val displayItem = item.copy(isBookmarked = bookmarkedIds.contains(item.id))
                                            ItemCard(
                                                item = displayItem,
                                                onBookmarkClick = {
                                                    viewModel.onEvent(MoviesContract.Event.ToggleBookmark(it))
                                                },
                                                onItemClick = { selectedItemForDetails = it },
                                            )
                                        }
                                    }
                                }
                            }

                            lazyPagingItems.apply {
                                when {
                                    loadState.refresh is LoadState.Loading -> {
                                        item(span = {
                                            androidx.compose.foundation.lazy.grid
                                                .GridItemSpan(maxLineSpan)
                                        }) {
                                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(32.dp))
                                            }
                                        }
                                    }

                                    loadState.append is LoadState.Loading -> {
                                        item(span = {
                                            androidx.compose.foundation.lazy.grid
                                                .GridItemSpan(maxLineSpan)
                                        }) {
                                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(32.dp))
                                            }
                                        }
                                    }

                                    loadState.refresh is LoadState.Error -> {
                                        val e = lazyPagingItems.loadState.refresh as LoadState.Error
                                        item { Text(text = e.error.localizedMessage ?: "Error", color = MaterialTheme.colorScheme.error) }
                                    }

                                    loadState.append is LoadState.Error -> {
                                        val e = lazyPagingItems.loadState.append as LoadState.Error
                                        item { Text(text = e.error.localizedMessage ?: "Error", color = MaterialTheme.colorScheme.error) }
                                    }
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
