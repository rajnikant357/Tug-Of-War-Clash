package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerState
import kotlinx.coroutines.launch

data class TapRipple(val id: Long, val offset: Offset, val alpha: Animatable<Float, *>, val radius: Animatable<Float, *>)

@Composable
fun TapZone(
    player: PlayerState,
    isRotated: Boolean = false,
    enabled: Boolean = true,
    testTag: String = "tap_button",
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val buttonScale = remember { Animatable(1f) }
    val ripples = remember { mutableStateListOf<TapRipple>() }

    val mainColor = Color(player.colorHex)
    val lightAccent = if (player.colorHex == 0xFF2563EB) Color(0xFF60A5FA) else Color(0xFFF87171)

    // Surge pulse animation
    val isSurgeReady = player.surgeMeter >= 0.95f
    val surgePulse = remember { Animatable(1f) }
    LaunchedEffect(isSurgeReady) {
        if (isSurgeReady) {
            while (true) {
                surgePulse.animateTo(1.08f, tween(300, easing = LinearEasing))
                surgePulse.animateTo(1.0f, tween(300, easing = LinearEasing))
            }
        } else {
            surgePulse.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .rotate(if (isRotated) 180f else 0f)
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Multi-touch detector surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            mainColor.copy(alpha = 0.25f),
                            Color(0xFF0F172A).copy(alpha = 0.65f)
                        )
                    )
                )
                .border(2.dp, mainColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val touchPos = down.position
                        onTap()

                        // Trigger scale pop
                        coroutineScope.launch {
                            buttonScale.animateTo(0.92f, spring(stiffness = 800f))
                            buttonScale.animateTo(1f, spring(stiffness = 600f))
                        }

                        // Trigger expanding ripple
                        val ripple = TapRipple(
                            id = System.nanoTime(),
                            offset = touchPos,
                            alpha = Animatable(0.7f),
                            radius = Animatable(20f)
                        )
                        ripples.add(ripple)
                        coroutineScope.launch {
                            launch { ripple.radius.animateTo(240f, tween(400)) }
                            launch { ripple.alpha.animateTo(0f, tween(400)) }
                            ripples.remove(ripple)
                        }
                    }
                }
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            // Ripple layer
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                for (r in ripples) {
                    drawCircle(
                        color = lightAccent.copy(alpha = r.alpha.value.coerceIn(0f, 1f)),
                        radius = r.radius.value,
                        center = r.offset
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header info: Player Name and Live TPS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.teamIcon,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = player.name,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // TPS Meter
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            player.currentTps >= 8f -> Color(0xFFDC2626)
                            player.currentTps >= 5f -> Color(0xFFF59E0B)
                            else -> Color(0xFF334155)
                        },
                        modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Speed",
                                tint = Color.Yellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f TPS".format(player.currentTps),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Center Massive TAP BUTTON
                Box(
                    modifier = Modifier
                        .scale(buttonScale.value * surgePulse.value)
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isSurgeReady) {
                                    listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFB45309))
                                } else {
                                    listOf(lightAccent, mainColor, Color(0xFF1E1B4B))
                                }
                            )
                        )
                        .border(
                            width = if (isSurgeReady) 4.dp else 2.dp,
                            color = if (isSurgeReady) Color.Yellow else Color.White.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                        .shadow(16.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isSurgeReady) "SUPER YANK!" else "TAP!",
                            color = Color.White,
                            fontSize = if (isSurgeReady) 16.sp else 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${player.tapsCount}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Bottom Surge / Combo Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isSurgeReady) "💥 READY FOR POWER YANK!" else "⚡ COMBO SURGE",
                            color = if (isSurgeReady) Color(0xFFFCD34D) else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(player.surgeMeter * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { player.surgeMeter },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isSurgeReady) Color(0xFFF59E0B) else lightAccent,
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }
        }
    }
}
