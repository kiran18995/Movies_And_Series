package com.kiran.movie.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kiran.movie.core.ui.BuildConfig
import com.kiran.movie.core.ui.R
import com.kiran.movie.data.models.Item
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun ItemCard(
    item: Item,
    onBookmarkClick: (Item) -> Unit,
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardInteractionSource = remember { MutableInteractionSource() }
    val isPressed by cardInteractionSource.collectIsPressedAsState()

    // Dramatic press scale
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cardScale"
    )

    // White flash overlay on press
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.2f else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "overlayAlpha"
    )

    // Bookmark: 360° spin + pop bounce when toggled
    val bookmarkRotation = remember { Animatable(0f) }
    val bookmarkAnimScale = remember { Animatable(1f) }
    var isFirstRender by remember { mutableStateOf(true) }

    LaunchedEffect(item.isBookmarked) {
        if (isFirstRender) { isFirstRender = false; return@LaunchedEffect }
        coroutineScope {
            launch {
                bookmarkRotation.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                )
                bookmarkRotation.snapTo(0f)
            }
            launch {
                bookmarkAnimScale.animateTo(0.3f, animationSpec = tween(durationMillis = 90))
                bookmarkAnimScale.animateTo(
                    targetValue = 1.6f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                bookmarkAnimScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(4.dp)
            .scale(cardScale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = cardInteractionSource,
                indication = null,
                onClick = { onItemClick(item) }
            )
    ) {
        AsyncImage(
            model = "${BuildConfig.BASE_IMAGE_URL}${item.posterPath}",
            contentDescription = item.title ?: "Movie Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        // Flash overlay on press
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = overlayAlpha))
        )

        val bookmarkIcon = if (item.isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_un_bookmarked
        val bookmarkDesc = if (item.isBookmarked) {
            stringResource(R.string.bookmark_remove)
        } else {
            stringResource(R.string.bookmark_add)
        }

        IconButton(
            onClick = { onBookmarkClick(item) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
                .graphicsLayer {
                    rotationZ = bookmarkRotation.value
                    scaleX = bookmarkAnimScale.value
                    scaleY = bookmarkAnimScale.value
                }
        ) {
            Icon(
                painter = painterResource(id = bookmarkIcon),
                contentDescription = bookmarkDesc
            )
        }
    }
}
