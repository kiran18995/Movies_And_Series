package com.kiran.movie.core.ui.details

import androidx.compose.material3.Surface
import com.kiran.movie.core.ui.theme.MovieTheme
import com.kiran.movie.core.ui.theme.ThemePreviews
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kiran.movie.core.ui.BuildConfig
import com.kiran.movie.core.ui.webview.MovieWebViewActivity
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.models.ItemDetails
import java.io.ByteArrayInputStream

// ---------------------------------------------------------------------------
// Ad-blocking domain list (common ad/tracker networks)
// ---------------------------------------------------------------------------
private val AD_HOSTS = setOf(
    "doubleclick.net", "googlesyndication.com", "googleadservices.com",
    "adservice.google.com", "adservice.google.co.in",
    "pagead2.googlesyndication.com", "tpc.googlesyndication.com",
    "ads.pubmatic.com", "simage2.pubmatic.com",
    "secure.adnxs.com", "ib.adnxs.com",
    "prebid.io", "prebid.org",
    "taboola.com", "trc.taboola.com",
    "outbrain.com", "widgets.outbrain.com",
    "amazon-adsystem.com", "aax.amazon-adsystem.com",
    "criteo.com", "static.criteo.net",
    "advertising.com", "adtech.com",
    "rubiconproject.com", "ads.rubiconproject.com",
    "openx.net", "openx.com",
    "moatads.com", "z.moatads.com",
    "casalemedia.com", "scdn.cxense.com",
    "ads.yahoo.com", "media.net",
    "scorecardresearch.com", "doubleclick.com",
    "cdn.admanager.com", "ads.exoclick.com", "adx.ads.exoclick.com",
    "popads.net", "popcash.net", "trafficjunky.net",
    "traffichaus.com", "trafficfactory.biz",
    "propellerads.com", "admaven.com",
    "revcontent.com", "adtelligent.com",
    "ads.themoviedb.org",
)

private fun shouldBlockRequest(url: String?): Boolean {
    if (url == null) return false
    return try {
        val host = Uri.parse(url).host?.lowercase() ?: return false
        AD_HOSTS.any { host == it || host.endsWith(".$it") }
    } catch (_: Exception) {
        false
    }
}

private val EMPTY_RESPONSE by lazy {
    WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
}

// ---------------------------------------------------------------------------
// Composables
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsBottomSheet(
    item: Item,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    viewModel: ItemDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(item) {
        viewModel.fetchDetails(item.id, item.isMovie)
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetState()
            onDismissRequest()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        when (val currentState = state) {
            is DetailsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is DetailsState.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = currentState.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is DetailsState.Success -> {
                DetailsContent(details = currentState.details, item = item)
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsContent(
    details: ItemDetails,
    item: Item,
) {
    val context = LocalContext.current
    val watchUrl = if (item.isMovie) {
        "https://vidsrc.to/embed/movie/${item.id}"
    } else {
        "https://vidsrc.to/embed/tv/${item.id}"
    }

    var showEpisodeSelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Backdrop + Poster + Title ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            AsyncImage(
                model = "${BuildConfig.BASE_IMAGE_URL}${details.backdropPath ?: details.posterPath}",
                contentDescription = "Backdrop",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                            startY = 100f,
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp)
                    .offset(y = 20.dp),
            ) {
                AsyncImage(
                    model = "${BuildConfig.BASE_IMAGE_URL}${details.posterPath}",
                    contentDescription = "Poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.Bottom),
                ) {
                    Text(
                        text = details.title ?: "Unknown",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ImdbRatingBadge(rating = details.voteAverage)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ── Play Now + Watch Trailer side by side ─────────────────────────────
        val trailer = details.videos?.results?.firstOrNull {
            it.type == "Trailer" && it.site == "YouTube"
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Play Now
            Button(
                onClick = {
                    if (item.isMovie) {
                        val intent = Intent(context, MovieWebViewActivity::class.java).apply {
                            putExtra(MovieWebViewActivity.EXTRA_URL, watchUrl)
                            putExtra(MovieWebViewActivity.EXTRA_TITLE, details.title ?: "Watch")
                        }
                        context.startActivity(intent)
                    } else {
                        showEpisodeSelector = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Play Now",
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            // Watch Trailer (only shown if trailer exists)
            if (trailer != null) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://www.youtube.com/watch?v=${trailer.key}".toUri(),
                            ),
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Trailer",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Metadata ─────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (!details.tagline.isNullOrEmpty()) {
                Text(
                    text = "\"${details.tagline}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val metadata = mutableListOf<String>()
            details.releaseDate?.takeIf { it.isNotEmpty() }?.let { metadata.add(it.take(4)) }
            val runtime = details.runtime
            if (runtime != null && runtime > 0) metadata.add("$runtime min")
            val seasons = details.numberOfSeasons
            if (seasons != null && seasons > 0) {
                val eps = details.numberOfEpisodes
                val epString = if (eps != null && eps > 0) " ($eps eps)" else ""
                metadata.add("$seasons Seasons$epString")
            }
            val status = details.status
            if (!status.isNullOrEmpty()) metadata.add(status)
            if (metadata.isNotEmpty()) {
                Text(
                    text = metadata.joinToString(" • "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val genres = details.genres
            if (!genres.isNullOrEmpty()) {
                Text(
                    text = genres.joinToString { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Trailer + Overview + Cast ─────────────────────────────────────────
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = details.overview ?: "No overview available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!details.credits?.cast.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(details.credits!!.cast!!.take(10)) { cast ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp),
                        ) {
                            AsyncImage(
                                model = if (cast.profilePath != null)
                                    "${BuildConfig.BASE_IMAGE_URL}${cast.profilePath}"
                                else null,
                                contentDescription = cast.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cast.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Season/Episode picker for TV shows
    if (showEpisodeSelector) {
        EpisodeSelectorDialog(
            seasons = details.seasons
                ?.filter { it.seasonNumber > 0 }  // exclude Specials (season 0)
                ?.sortedBy { it.seasonNumber }
                ?.ifEmpty { null },
            fallbackSeasonCount = details.numberOfSeasons ?: 1,
            tmdbId = item.id,
            title = details.title,
            onDismiss = { showEpisodeSelector = false },
        )
    }
}

// ---------------------------------------------------------------------------
// Season / Episode selector
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeSelectorDialog(
    seasons: List<com.kiran.movie.data.models.SeasonInfo>?,  // null = fallback
    fallbackSeasonCount: Int,
    tmdbId: Int,
    title: String?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    // Build a season list: prefer real API data, fall back to a numbered list
    val seasonList = seasons
        ?: List(fallbackSeasonCount) { i ->
            com.kiran.movie.data.models.SeasonInfo(
                seasonNumber = i + 1,
                episodeCount = 20,  // conservative fallback
                name = "Season ${i + 1}"
            )
        }

    var selectedSeason by remember { mutableIntStateOf(seasonList.first().seasonNumber) }
    var selectedEpisode by remember { mutableIntStateOf(1) }

    // Episode count for the currently selected season
    val episodeCount = seasonList
        .firstOrNull { it.seasonNumber == selectedSeason }
        ?.episodeCount
        ?.coerceAtLeast(1)
        ?: 1

    // Reset episode to 1 whenever season changes
    LaunchedEffect(selectedSeason) { selectedEpisode = 1 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Season & Episode") },
        text = {
            Column {
                // ── Season picker ────────────────────────────────────────────
                Text(
                    text = "Season",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(seasonList.size) { index ->
                        val s = seasonList[index]
                        FilterChip(
                            selected = selectedSeason == s.seasonNumber,
                            onClick = { selectedSeason = s.seasonNumber },
                            label = { Text("S${s.seasonNumber}") },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Episode picker (exact count from API) ──────────────────────
                Text(
                    text = "Episode  ($episodeCount total)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(episodeCount) { index ->
                        val ep = index + 1
                        FilterChip(
                            selected = selectedEpisode == ep,
                            onClick = { selectedEpisode = ep },
                            label = { Text("$ep") },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val url = "https://vidsrc.to/embed/tv/$tmdbId/$selectedSeason/$selectedEpisode"
                    val intent = Intent(context, MovieWebViewActivity::class.java).apply {
                        putExtra(MovieWebViewActivity.EXTRA_URL, url)
                        putExtra(
                            MovieWebViewActivity.EXTRA_TITLE,
                            "${title ?: "Watch"} — S${selectedSeason}E${selectedEpisode}",
                        )
                    }
                    context.startActivity(intent)
                    onDismiss()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play S${selectedSeason}E${selectedEpisode}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ---------------------------------------------------------------------------
// IMDb Rating Badge — logo pill + score + arc indicator
// ---------------------------------------------------------------------------
@Composable
private fun ImdbRatingBadge(rating: Double) {
    val imdbYellow = Color(0xFFF5C518)
    val trackColor = imdbYellow.copy(alpha = 0.20f)
    val fraction = (rating / 10f).coerceIn(0.0, 1.0)
    val arcColor = when {
        rating >= 7.5 -> Color(0xFF4CAF50)   // green — great
        rating >= 6.0 -> Color(0xFFFFC107)   // amber — good
        else          -> Color(0xFFF44336)   // red   — poor
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // IMDb logo pill
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(imdbYellow, RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp),
        ) {
            Text(
                text = "IMDb",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                ),
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Arc indicator + rating number stacked
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(44.dp)) {
                val strokeWidth = 5.dp.toPx()
                val inset = strokeWidth / 2f
                val arcRect = androidx.compose.ui.geometry.Rect(
                    left = inset, top = inset,
                    right = size.width - inset, bottom = size.height - inset,
                )
                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = arcRect.topLeft,
                    size = arcRect.size,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                // Filled arc
                drawArc(
                    color = arcColor,
                    startAngle = 135f,
                    sweepAngle = (270f * fraction).toFloat(),
                    useCenter = false,
                    topLeft = arcRect.topLeft,
                    size = arcRect.size,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.1f", rating),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "/10",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ImdbRatingBadgePreview() {
    MovieTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ImdbRatingBadge(rating = 8.5)
        }
    }
}
