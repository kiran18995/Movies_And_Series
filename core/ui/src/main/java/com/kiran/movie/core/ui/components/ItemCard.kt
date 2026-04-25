package com.kiran.movie.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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

        Image(
            painter = painterResource(id = bookmarkIcon),
            contentDescription = "Bookmark Icon",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
                .padding(start = 16.dp, bottom = 16.dp)
                .size(24.dp)
                .clickable { onBookmarkClick(item) }
        )
    }
}
