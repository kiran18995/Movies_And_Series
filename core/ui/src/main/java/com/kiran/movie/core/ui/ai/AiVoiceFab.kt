package com.kiran.movie.core.ui.ai

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import com.kiran.movie.core.ui.R
import com.kiran.movie.shared.SharedAiSearchViewModel
import org.koin.compose.koinInject

private val BgColor  = Color(0xFF0D0D0D)
private val DimWhite = Color.White.copy(alpha = 0.38f)

// ─────────────────────────────────────────────────────────────────────────────
// Public FAB
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AiVoiceFab(
    modifier: Modifier = Modifier,
    viewModel: SharedAiSearchViewModel = koinInject()
) {
    val speechState by viewModel.speechState.collectAsState()
    val aiText      by viewModel.aiInterpretation.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()
    val results     by viewModel.results.collectAsState()

    val view = LocalView.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                view.context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var overlayOpen by remember { mutableStateOf(false) }

    // ── FAB ──────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A1A1A))
            .clickable {
                when {
                    overlayOpen  -> { /* already open */ }
                    hasPermission -> { overlayOpen = true; viewModel.startListening() }
                    else          -> launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Dot badge when results are waiting
        if (results.isNotEmpty() && !overlayOpen) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4A90E2))
                    .align(Alignment.TopEnd)
                    .padding(end = 10.dp, top = 10.dp)
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_gemini_star),
            contentDescription = "AI Search",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }

    // ── Full-screen overlay ───────────────────────────────────────────────────
    if (overlayOpen) {
        Dialog(
            onDismissRequest = {
                overlayOpen = false
                viewModel.stopListening()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows  = false
            )
        ) {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            SideEffect {
                dialogWindow?.apply {
                    setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                    setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                }
            }

            VoiceOverlay(
                speechText   = speechState.text,
                isListening  = speechState.isListening,
                isLoading    = isLoading,
                aiResponse   = aiText,
                resultCount  = results.size,
                onViewResults = { overlayOpen = false },
                onClose = {
                    overlayOpen = false
                    viewModel.stopListening()
                    viewModel.clearResults()
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overlay
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun VoiceOverlay(
    speechText   : String,
    isListening  : Boolean,
    isLoading    : Boolean,
    aiResponse   : String,
    resultCount  : Int,
    onViewResults: () -> Unit,
    onClose      : () -> Unit,
) {
    // Fade-in on open
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(220), label = "fade")
    LaunchedEffect(Unit) { visible = true }

    // Gentle icon breathe
    val inf = rememberInfiniteTransition(label = "breath")
    val breatheAlpha by inf.animateFloat(
        0.55f, 1f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    // 5 waveform bars
    val barAnims = (0 until 5).map { i ->
        inf.animateFloat(
            0.15f, 1f,
            infiniteRepeatable(
                tween(280 + i * 55, easing = FastOutSlowInEasing),
                RepeatMode.Reverse,
                StartOffset(i * 70)
            ),
            label = "bar$i"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .background(BgColor)
    ) {
        // ── Main content (centered) ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon or spinner
            if (isLoading) {
                CircularProgressIndicator(
                    color       = Color.White.copy(0.6f),
                    strokeWidth = 1.5.dp,
                    modifier    = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_gemini_star),
                    contentDescription = null,
                    tint     = Color.White.copy(if (isListening) breatheAlpha else 0.9f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(36.dp))

            // Status text
            AnimatedContent(
                targetState = when {
                    isListening && speechText.isNotBlank() -> speechText
                    isListening                            -> "Listening"
                    isLoading                              -> "Thinking…"
                    aiResponse.isNotBlank()                -> aiResponse
                    else                                   -> "Tap to speak"
                },
                transitionSpec = {
                    (slideInVertically { 16 } + fadeIn(tween(200))) togetherWith
                        (slideOutVertically { -16 } + fadeOut(tween(150)))
                },
                label = "statusText"
            ) { text ->
                Text(
                    text      = text,
                    color     = Color.White,
                    style     = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Light
                    ),
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.titleLarge.lineHeight
                )
            }

            Spacer(Modifier.height(48.dp))

            // Waveform bars (only while listening)
            AnimatedVisibility(
                visible = isListening,
                enter   = fadeIn(tween(200)),
                exit    = fadeOut(tween(150))
            ) {
                Row(
                    modifier              = Modifier.height(36.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    barAnims.forEach { anim ->
                        val h by anim
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(((h * 28).dp).coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(0.75f))
                        )
                    }
                }
            }

            // View results action
            AnimatedVisibility(
                visible = resultCount > 0 && !isListening && !isLoading,
                enter   = fadeIn(tween(300, delayMillis = 100)),
                exit    = fadeOut(tween(150))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text  = "$resultCount results",
                        color = DimWhite,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(0.08f))
                            .clickable(onClick = onViewResults)
                            .padding(horizontal = 28.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "View Results",
                            color      = Color.White,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ── Close button (top-right) ──────────────────────────────────────────
        IconButton(
            onClick  = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 12.dp)
        ) {
            Icon(
                imageVector  = Icons.Filled.Close,
                contentDescription = "Close",
                tint         = DimWhite,
                modifier     = Modifier.size(22.dp)
            )
        }

        // ── Powered-by caption ────────────────────────────────────────────────
        Text(
            text     = "Gemini AI",
            color    = DimWhite.copy(0.5f),
            style    = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}
