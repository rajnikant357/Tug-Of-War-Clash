package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    timeRemaining: Int,
    tapsCount: Int,
    isActive: Boolean,
    isFinished: Boolean,
    onStartPractice: () -> Unit,
    onTap: () -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val buttonScale = remember { Animatable(1f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "10s SPEED WORKOUT",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("practice_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0B0F19)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Timer & Stats Bar
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isActive) "$timeRemaining SECONDS LEFT" else if (isFinished) "WORKOUT FINISHED!" else "READY TO TEST YOUR SPEED?",
                        color = if (timeRemaining <= 3 && isActive) Color(0xFFEF4444) else Color(0xFF38BDF8),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (isActive) timeRemaining / 10f else if (isFinished) 0f else 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFF059669),
                        trackColor = Color(0xFF334155)
                    )
                }
            }

            // Big Tap Target or Start Prompt
            if (!isActive && !isFinished) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⚡", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Warm up your fingers!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Tap as many times as you can in 10 seconds. Use multiple fingers for rapid bursts!",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )

                    Button(
                        onClick = onStartPractice,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp)
                            .testTag("start_practice_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("START 10s SPRINT", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            } else if (isActive) {
                // Massive Active Tap Button
                Box(
                    modifier = Modifier
                        .scale(buttonScale.value)
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF34D399), Color(0xFF059669), Color(0xFF064E3B))
                            )
                        )
                        .border(4.dp, Color.White, CircleShape)
                        .shadow(24.dp, CircleShape)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                onTap()
                                coroutineScope.launch {
                                    buttonScale.animateTo(0.92f, spring(stiffness = 800f))
                                    buttonScale.animateTo(1.0f, spring(stiffness = 600f))
                                }
                            }
                        }
                        .testTag("practice_tap_target"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$tapsCount",
                            color = Color.White,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "TAPS",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            } else {
                // Results Card
                val tps = tapsCount / 10f
                val rankTitle = when {
                    tps >= 11f -> "🏆 GOD OF TENSION"
                    tps >= 9f -> "⚡ CABLE SNAPPER"
                    tps >= 7f -> "💪 POWER ROPER"
                    tps >= 5f -> "🔥 GYM CONTENDER"
                    else -> "🌱 TUG RECRUIT"
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFF059669), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⭐", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = rankTitle,
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$tapsCount", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                                Text("Total Taps", color = Color.LightGray, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("%.1f".format(tps), color = Color(0xFFFBBF24), fontSize = 28.sp, fontWeight = FontWeight.Black)
                                Text("Average TPS", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onStartPractice,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.testTag("practice_retry_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try Again", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom tip
            Text(
                text = "Pro-Tip: Drum alternating index and middle fingers for explosive pull power!",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
