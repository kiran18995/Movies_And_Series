package com.kiran.movie.core.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.launch

@Composable
fun ItemCard(
    item: Item,
    onBookmarkClick: (Item) -> Unit,
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardInteractionSource = remember(item.id) { MutableInteractionSource() }
    val isPressed by cardInteractionSource.collectIsPressedAsState()

    // Card press scale
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    // Subtle flash overlay on press
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "overlayAlpha"
    )

    // ── Bookmark animation ─────────────────────────────────────────────────
    // Professional: smooth scale-pop (no wild rotation)
    val bookmarkScale = remember(item.id) { Animatable(1f) }
    // Ripple background pulse
    val rippleScale = remember(item.id) { Animatable(0f) }
    val rippleAlpha = remember(item.id) { Animatable(0f) }
    var isFirstRender by remember(item.id) { mutableStateOf(true) }

    LaunchedEffect(item.isBookmarked) {
        if (isFirstRender) { isFirstRender = false; return@LaunchedEffect }
        // Scale pop: compress → overshoot → settle
        launch {
            bookmarkScale.animateTo(0.6f, animationSpec = tween(durationMillis = 100))
            bookmarkScale.animateTo(
                targetValue = 1.25f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            bookmarkScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        // Ripple burst: expand + fade out
        launch {
            rippleAlpha.snapTo(0.55f)
            rippleScale.snapTo(0.3f)
            rippleScale.animateTo(
                targetValue = 2.2f,
                animationSpec = tween(durationMillis = 380)
            )
            rippleAlpha.animateTo(0f, animationSpec = tween(durationMillis = 300))
            rippleScale.snapTo(0f)
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

        // Flash overlay on card press
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = overlayAlpha))
        )

        // ── Bookmark button ────────────────────────────────────────────────
        val isBookmarked = item.isBookmarked
        val bookmarkIcon = if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_un_bookmarked
        val bookmarkDesc = if (isBookmarked) {
            stringResource(R.string.bookmark_remove)
        } else {
            stringResource(R.string.bookmark_add)
        }
        // Tint: filled = warm gold, unfilled = white (always visible over any poster)
        val iconTint = if (isBookmarked) Color(0xFFFFC107) else Color.White

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 6.dp, end = 6.dp)
        ) {
            // Ripple burst circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .scale(rippleScale.value)
                    .background(
                        color = if (isBookmarked) Color(0xFFFFC107).copy(alpha = rippleAlpha.value)
                                else Color.White.copy(alpha = rippleAlpha.value),
                        shape = CircleShape,
                    )
            )

            // Semi-transparent backdrop for visibility
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )

            // Bookmark icon with pop animation
            IconButton(
                onClick = { onBookmarkClick(item) },
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        scaleX = bookmarkScale.value
                        scaleY = bookmarkScale.value
                    }
            ) {
                Icon(
                    painter = painterResource(id = bookmarkIcon),
                    contentDescription = bookmarkDesc,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
