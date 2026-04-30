package com.kiran.movie.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kiran.movie.core.ui.BuildConfig
import com.kiran.movie.core.ui.R
import com.kiran.movie.data.models.Item

@Composable
fun ItemCard(
    item: Item,
    onBookmarkClick: (Item) -> Unit,
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(4.dp)
            .clickable { onItemClick(item) }
    ) {
        AsyncImage(
            model = "${BuildConfig.BASE_IMAGE_URL}${item.posterPath}",
            contentDescription = item.title ?: "Movie Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        val bookmarkIcon = if (item.isBookmarked) {
            R.drawable.ic_bookmarked
        } else {
            R.drawable.ic_un_bookmarked
        }
        val bookmarkContentDescription = if (item.isBookmarked) {
            stringResource(R.string.bookmark_remove)
        } else {
            stringResource(R.string.bookmark_add)
        }

        // IconButton provides a 48dp touch target and correct semantics (issue #2)
        IconButton(
            onClick = { onBookmarkClick(item) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = bookmarkIcon),
                contentDescription = bookmarkContentDescription
            )
        }
    }
}
