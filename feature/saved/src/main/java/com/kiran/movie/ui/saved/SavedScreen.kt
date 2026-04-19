package com.kiran.movie.ui.saved

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiran.movie.core.ui.MainViewModel
import com.kiran.movie.core.ui.components.ItemCard
import com.kiran.movie.core.ui.R
import es.dmoral.toasty.Toasty

@Composable
fun SavedScreen(
    mainViewModel: MainViewModel,
    viewModel: SavedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isMovieTab by viewModel.isMovieTab.collectAsState()
    val context = LocalContext.current
    val searchQuery by mainViewModel.searchQuery.collectAsState()

    LaunchedEffect(searchQuery) {
        viewModel.onEvent(SavedContract.Event.Search(searchQuery))
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.movies),
                color = if (isMovieTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { viewModel.onEvent(SavedContract.Event.ChangeTab(true)) }
            )
            Text(
                text = stringResource(id = R.string.tv_shows),
                color = if (!isMovieTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { viewModel.onEvent(SavedContract.Event.ChangeTab(false)) }
            )
        }

        when (val currentState = state) {
            is SavedContract.State.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is SavedContract.State.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = currentState.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is SavedContract.State.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp)
                ) {
                    items(currentState.items) { item ->
                        ItemCard(
                            item = item,
                            onBookmarkClick = {
                                viewModel.onEvent(SavedContract.Event.ToggleBookmark(it))
                            }
                        )
                    }
                }
            }
        }
    }
}
