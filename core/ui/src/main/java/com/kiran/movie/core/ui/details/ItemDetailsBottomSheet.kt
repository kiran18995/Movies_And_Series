package com.kiran.movie.core.ui.details

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", details.voteAverage),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            // Play FAB — launches full-screen WebView Activity
            FloatingActionButton(
                onClick = {
                    if (item.isMovie) {
                        // Movie: launch directly
                        val intent = Intent(context, MovieWebViewActivity::class.java).apply {
                            putExtra(MovieWebViewActivity.EXTRA_URL, watchUrl)
                            putExtra(MovieWebViewActivity.EXTRA_TITLE, details.title ?: "Watch")
                        }
                        context.startActivity(intent)
                    } else {
                        // TV show: pick season/episode first
                        showEpisodeSelector = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .offset(y = 28.dp),
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
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
            val trailer = details.videos?.results?.firstOrNull {
                it.type == "Trailer" && it.site == "YouTube"
            }
            if (trailer != null) {
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://www.youtube.com/watch?v=${trailer.key}".toUri(),
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Watch Trailer")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

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
            numberOfSeasons = details.numberOfSeasons ?: 1,
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
    numberOfSeasons: Int,
    tmdbId: Int,
    title: String?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedSeason by remember { mutableIntStateOf(1) }
    var selectedEpisode by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Season & Episode") },
        text = {
            Column {
                // ── Season picker ─────────────────────────────────────────────
                Text(
                    text = "Season",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(numberOfSeasons) { index ->
                        val season = index + 1
                        FilterChip(
                            selected = selectedSeason == season,
                            onClick = {
                                selectedSeason = season
                                selectedEpisode = 1  // reset episode on season change
                            },
                            label = { Text("S$season") },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Episode picker ────────────────────────────────────────────
                Text(
                    text = "Episode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(50) { index ->
                        val episode = index + 1
                        FilterChip(
                            selected = selectedEpisode == episode,
                            onClick = { selectedEpisode = episode },
                            label = { Text("$episode") },
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
