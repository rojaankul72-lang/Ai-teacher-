package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.AudioPlaybackState
import com.example.data.model.AppLanguage
import com.example.data.model.SceneVisualType
import com.example.data.model.StoryScene
import com.example.ui.theme.fontFamilyForLanguage

@Composable
fun MeeraTeacherPortrait(
    isSpeaking: Boolean,
    lightweightMode: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
    showBadge: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "meera_motion")
    val breathScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (lightweightMode) 1f else if (isSpeaking) 1.035f else 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 550 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    val headTilt by transition.animateFloat(
        initialValue = -1.2f,
        targetValue = if (lightweightMode) 0f else if (isSpeaking) 1.8f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 900 else 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSpeaking) {
                Box(
                    modifier = Modifier
                        .size(size + 14.dp)
                        .scale(breathScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                )
            }
            Image(
                painter = painterResource(id = R.drawable.img_teacher_meera),
                contentDescription = "Meera Didi, AI Teacher",
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = breathScale
                        scaleY = breathScale
                        rotationZ = headTilt
                    }
                    .clip(CircleShape)
                    .border(
                        width = if (isSpeaking) 3.dp else 2.dp,
                        color = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
            if (isSpeaking) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Speaking",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        if (showBadge) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "AI Teacher • Meera Didi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StoryStageCanvas(
    scene: StoryScene,
    tappedLamps: Set<Int>,
    onLampTap: (Int) -> Unit,
    lightweightMode: Boolean,
    highResolutionMode: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "stage_anim")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(235.dp),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp,
        color = Color(0xFF1E293B)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Optional photographic story backdrop
            scene.drawableResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = scene.beatTag,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = if (scene.visualType == SceneVisualType.HOLOGRAPHIC_WATER_CYCLE) 0.35f else 0.78f
                )
            }

            // Interactive visual overlay depending on scene beat
            when (scene.visualType) {
                SceneVisualType.LAMPS_COURTYARD_TWO_PLUS_ONE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xCC29170B))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DiyaLampItem(index = 0, label = "1", lit = true, glowPulse = pulse, onClick = {})
                        DiyaLampItem(index = 1, label = "2", lit = true, glowPulse = pulse, onClick = {})
                        DiyaLampItem(index = 2, label = "+1", lit = true, glowPulse = pulse, isNew = true, onClick = {})
                    }
                }

                SceneVisualType.LAMPS_TAP_COUNTING_GUIDED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF3E220D), Color(0xFF231307))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tap each diya ONCE to hear its soft note (${tappedLamps.size}/3)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFFF3CD)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(22.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (0..2).forEach { idx ->
                                val isLit = idx in tappedLamps
                                DiyaLampItem(
                                    index = idx,
                                    label = if (isLit) "${idx + 1}" else "Tap",
                                    lit = isLit,
                                    glowPulse = if (isLit) pulse else 0.2f,
                                    onClick = { onLampTap(idx) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                SceneVisualType.LAMPS_REARRANGED_CHECK -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF3B200C), Color(0xFF1F1006))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rearranged Triangle Pattern",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFDE68A)
                        )
                        DiyaLampItem(index = 0, label = "★", lit = true, glowPulse = pulse, onClick = {})
                        Row(horizontalArrangement = Arrangement.spacedBy(56.dp)) {
                            DiyaLampItem(index = 1, label = "★", lit = true, glowPulse = pulse, onClick = {})
                            DiyaLampItem(index = 2, label = "★", lit = true, glowPulse = pulse, onClick = {})
                        }
                    }
                }

                SceneVisualType.LAMPS_NEXT_DAY_MARIGOLDS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Next-Day Fresh Check • Courtyard Mat",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF7C2D12)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(26.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) {
                                MarigoldFlowerCanvas()
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                SceneVisualType.JEYPORE_PUDDLE_HOOK -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xB30F172A))
                                )
                            )
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val lift = if (lightweightMode) 0f else pulse * 18f
                        drawOval(
                            color = Color(0x8838BDF8),
                            topLeft = Offset(size.width * 0.28f, size.height * 0.72f),
                            size = Size(size.width * 0.44f, size.height * 0.18f)
                        )
                        // Rising invisible vapour shimmer lines
                        listOf(0.38f, 0.50f, 0.62f).forEach { xFrac ->
                            val x = size.width * xFrac
                            drawLine(
                                color = Color(0xCCBAE6FD),
                                start = Offset(x, size.height * 0.68f - lift),
                                end = Offset(x, size.height * 0.46f - lift),
                                strokeWidth = 4f
                            )
                        }
                    }
                }

                SceneVisualType.METAL_PLATE_EXPERIMENT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFEF9C3), Color(0xFFE0F2FE))
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Covered Steel Plate", style = MaterialTheme.typography.labelLarge, color = Color(0xFF0F172A))
                                Spacer(Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 56.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF64748B))
                                        .border(3.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Water Still Full", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Open Sunny Plate ☀", style = MaterialTheme.typography.labelLarge, color = Color(0xFFB45309))
                                Spacer(Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 56.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.35f))
                                        .border(3.dp, Color(0xFF94A3B8), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Vapour Rising ↑", style = MaterialTheme.typography.labelMedium, color = Color(0xFF0C4A6E))
                                }
                            }
                        }
                    }
                }

                SceneVisualType.HOLOGRAPHIC_WATER_CYCLE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xE6082F49), Color(0xE60C4A6E))
                                )
                            )
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val offsetAnim = if (lightweightMode) 0f else pulse * 12f
                        // Cloud (Condensation - visible droplets)
                        drawCircle(Color(0xFFE0F2FE), radius = 34f, center = Offset(w * 0.50f, h * 0.25f))
                        drawCircle(Color(0xFFE0F2FE), radius = 26f, center = Offset(w * 0.42f, h * 0.28f))
                        drawCircle(Color(0xFFE0F2FE), radius = 26f, center = Offset(w * 0.58f, h * 0.28f))

                        // Puddle below
                        drawOval(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(w * 0.32f, h * 0.78f),
                            size = Size(w * 0.36f, h * 0.12f)
                        )
                        // Evaporation arrow (left)
                        drawLine(
                            color = Color(0xFFFDE047),
                            start = Offset(w * 0.26f, h * 0.72f - offsetAnim),
                            end = Offset(w * 0.26f, h * 0.35f - offsetAnim),
                            strokeWidth = 6f
                        )
                        // Raindrops (right - Precipitation)
                        listOf(0.68f, 0.74f, 0.80f).forEach { rx ->
                            drawCircle(
                                color = Color(0xFF7DD3FC),
                                radius = 7f,
                                center = Offset(w * rx, h * 0.48f + offsetAnim)
                            )
                            drawCircle(
                                color = Color(0xFF7DD3FC),
                                radius = 7f,
                                center = Offset(w * rx, h * 0.65f + offsetAnim)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        HologramTag("↑ Invisible Vapour\n(Evaporation)", Color(0xFFFEF08A))
                        HologramTag("☁ Visible Droplets\n(Condensation)", Color(0xFFE0F2FE))
                        HologramTag("↓ Rain Returns\n(Precipitation)", Color(0xFF7DD3FC))
                    }
                }

                SceneVisualType.SUNNY_TERRACE_CLOTHES_CHECK,
                SceneVisualType.NEXT_DAY_BOWL_RECALL,
                SceneVisualType.GENERAL_STORY_STAGE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0E7490), Color(0xFF155E75))
                                )
                            )
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = scene.beatTag,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Quality / Resolution indicator badge
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = when {
                        lightweightMode -> "Lightweight Scene"
                        highResolutionMode -> "Animated 1080p Stage"
                        else -> "Adaptive Scene"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun HologramTag(text: String, color: Color) {
    Surface(
        color = Color(0x99021C2E),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DiyaLampItem(
    index: Int,
    label: String,
    lit: Boolean,
    glowPulse: Float,
    isNew: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
            .testTag("story_lamp_$index")
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (lit) {
                    drawCircle(
                        color = Color(0xFFFBBF24).copy(alpha = 0.25f + glowPulse * 0.25f),
                        radius = size.minDimension * 0.48f
                    )
                    // Flame teardrop
                    val flame = Path().apply {
                        moveTo(center.x, size.height * 0.14f)
                        quadraticTo(center.x + 18f, size.height * 0.38f, center.x, size.height * 0.50f)
                        quadraticTo(center.x - 18f, size.height * 0.38f, center.x, size.height * 0.14f)
                        close()
                    }
                    drawPath(flame, color = Color(0xFFF59E0B))
                }
                // Clay diya bowl
                drawArc(
                    color = if (isNew) Color(0xFFEA580C) else Color(0xFFB45309),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(size.width * 0.15f, size.height * 0.35f),
                    size = Size(size.width * 0.70f, size.height * 0.52f)
                )
            }
        }
        Surface(
            color = if (lit) Color(0xFFF59E0B) else Color(0xFF78350F),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun MarigoldFlowerCanvas() {
    Canvas(modifier = Modifier.size(62.dp)) {
        val petalColor = Color(0xFFDC2626)
        val innerColor = Color(0xFFF59E0B)
        repeat(8) { i ->
            val angle = i * (Math.PI / 4.0)
            val px = center.x + (kotlin.math.cos(angle) * 18f).toFloat()
            val py = center.y + (kotlin.math.sin(angle) * 18f).toFloat()
            drawCircle(petalColor, radius = 14f, center = Offset(px, py))
        }
        drawCircle(innerColor, radius = 14f, center = center)
        drawCircle(Color(0xFF991B1B), radius = 16f, center = center, style = Stroke(width = 2f))
    }
}

@Composable
fun AudioAndCaptionBar(
    audioState: AudioPlaybackState,
    language: AppLanguage,
    captionsEnabled: Boolean,
    speechSpeed: Double,
    onReplay: () -> Unit,
    onStopSpeech: () -> Unit,
    onToggleMusic: () -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleCaptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = audioState.activeVoiceEngine.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onReplay,
                        modifier = Modifier.testTag("replay_sentence_button")
                    ) {
                        Icon(Icons.Rounded.Replay, contentDescription = "Replay sentence")
                    }
                    if (audioState.isSpeaking) {
                        IconButton(
                            onClick = onStopSpeech,
                            modifier = Modifier.testTag("stop_narration_button")
                        ) {
                            Icon(Icons.Rounded.Stop, contentDescription = "Stop speaking")
                        }
                    }
                    IconButton(
                        onClick = onToggleMusic,
                        modifier = Modifier.testTag("toggle_music_button")
                    ) {
                        Icon(
                            imageVector = if (audioState.isMusicPlaying) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff,
                            contentDescription = "Toggle background music",
                            tint = if (audioState.isMusicPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onToggleCaptions,
                        modifier = Modifier.testTag("toggle_captions_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Subtitles,
                            contentDescription = "Toggle captions",
                            tint = if (captionsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = onCycleSpeed,
                        label = { Text("${speechSpeed}x") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.testTag("cycle_speed_button")
                    )
                }
            }

            AnimatedVisibility(visible = captionsEnabled && audioState.lastSpokenCaption.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("synchronized_caption_box")
                ) {
                    Text(
                        text = audioState.lastSpokenCaption,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = fontFamilyForLanguage(language)
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
