package com.kiran.movie.ui.movies

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.kiran.movie.core.ui.components.EmptyStateScreen
import com.kiran.movie.core.ui.components.ItemCard
import com.kiran.movie.core.ui.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import es.dmoral.toasty.Toasty

@Composable
fun MoviesScreen(
    searchQuery: String,
    onListEmptyStateChange: (Boolean) -> Unit,
    innerPadding: PaddingValues,
    viewModel: MoviesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
        val observer = LifecycleEventObserver { _, event ->
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val currentState = state) {
            is MoviesContract.State.Loading -> {
                LaunchedEffect(Unit) { onListEmptyStateChange(true) }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is MoviesContract.State.Error -> {
                LaunchedEffect(Unit) { onListEmptyStateChange(true) }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = currentState.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is MoviesContract.State.Success -> {
                val lazyPagingItems = currentState.pagingDataFlow.collectAsLazyPagingItems()
                
                LaunchedEffect(lazyPagingItems.itemCount) {
                    onListEmptyStateChange(lazyPagingItems.itemCount == 0)
                }
                
                if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh !is LoadState.Loading) {
                    EmptyStateScreen(
                        icon = Icons.Default.Search,
                        message = if (searchQuery.isNotEmpty()) "No movies found for '$searchQuery'" else "No movies found",
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            top = 16.dp + innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        ),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp)
                    ) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(id = R.string.popular_movies),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        items(lazyPagingItems.itemCount) { index ->
                            val item = lazyPagingItems[index]
                            if (item != null) {
                                ItemCard(
                                    item = item,
                                    onBookmarkClick = {
                                        viewModel.onEvent(MoviesContract.Event.ToggleBookmark(it))
                                    }
                                )
                            }
                        }
                        
                        lazyPagingItems.apply {
                            when {
                                loadState.refresh is LoadState.Loading -> {
                                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(32.dp))
                                        }
                                    }
                                }
                                loadState.append is LoadState.Loading -> {
                                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
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
