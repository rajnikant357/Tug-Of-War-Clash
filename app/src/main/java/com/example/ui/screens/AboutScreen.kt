package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PrometrionFooter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ABOUT THIS GAME",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("about_back_button")) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Game Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.5f), Color(0xFF1E293B))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Tug of War Logo",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(18.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tug of War Clash",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                            Text(
                                text = "High-Intensity Tapping Rope Battle",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Version 1.0.0 (Release Build 1)",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Developer & Studio Info (Prometrion)
            item {
                AboutSectionCard(
                    title = "PRODUCT & DEVELOPER",
                    icon = Icons.Default.Code,
                    accentColor = Color(0xFF38BDF8)
                ) {
                    InfoRow(label = "Product of", value = "Prometrion")
                    InfoRow(label = "Developer", value = "Rajnikant Garuav")
                    InfoRow(label = "Publisher", value = "Prometrion")
                    InfoRow(label = "Engine", value = "Jetpack Compose Canvas 60FPS+")
                    InfoRow(label = "Architecture", value = "Clean MVVM + Kotlin Coroutines")

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .clickable {
                                try {
                                    uriHandler.openUri("https://www.prometrion.com")
                                } catch (_: Exception) {}
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Official Website", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("www.prometrion.com", color = Color(0xFF38BDF8), fontSize = 12.sp, textDecoration = TextDecoration.Underline)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Website",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .clickable {
                                try {
                                    uriHandler.openUri("https://www.rajnikantg.in")
                                } catch (_: Exception) {}
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Developer Portfolio", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("www.rajnikantg.in", color = Color(0xFF38BDF8), fontSize = 12.sp, textDecoration = TextDecoration.Underline)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Developer Portfolio",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Game Tactics & Techniques
            item {
                AboutSectionCard(
                    title = "TACTICS, TECHNIQUES & MECHANICS",
                    icon = Icons.Default.MilitaryTech,
                    accentColor = Color(0xFFFBBF24)
                ) {
                    TacticPoint(
                        emoji = "⚡",
                        name = "Multi-Finger Drumming",
                        description = "Alternating 2 or 3 fingers on the tap zone drastically raises your Taps Per Second (TPS) compared to single-thumb tapping."
                    )
                    TacticPoint(
                        emoji = "💥",
                        name = "Super Yank / Surge Meter",
                        description = "Tapping in rapid cadence charges your Surge Gauge to 100%. The instant it fills, your next tap delivers an explosive 4x pull force!"
                    )
                    TacticPoint(
                        emoji = "🛡️",
                        name = "Comeback Desperation Traction",
                        description = "When pulled past 60% into the hazard zone, your team receives desperation footing giving 20% bonus torque. Don't give up!"
                    )
                    TacticPoint(
                        emoji = "👥",
                        name = "Table Duel Head-to-Head",
                        description = "Place your mobile device flat on a table between two players. Player 2's tap zone and character face opposite for ergonomic competitive battles."
                    )
                    TacticPoint(
                        emoji = "🏟️",
                        name = "Hazard Awareness",
                        description = "4 unique arena hazards (Stadium Mud, Cyberpunk Lasers, Volcano Lava, Snow Glacier). Pulling the opponent into the center hazard triggers instant victory!"
                    )
                    TacticPoint(
                        emoji = "⏱️",
                        name = "Burst Rhythm vs Endurance",
                        description = "Pacing taps in 3-second sprints prevents finger lockup during intense extra-time tug matches."
                    )
                }
            }

            // Supported Devices & Platform Compatibility
            item {
                AboutSectionCard(
                    title = "DEVICE & PLATFORM SUPPORT",
                    icon = Icons.Default.Devices,
                    accentColor = Color(0xFF22C55E)
                ) {
                    InfoRow(label = "Minimum OS", value = "Android 7.0 (Nougat, API 24)")
                    InfoRow(label = "Target OS", value = "Android 14, 15 & 16 (API 36)")
                    InfoRow(label = "Form Factors", value = "Phones, Foldables, Tablets, DeX")
                    InfoRow(label = "Display Support", value = "60Hz, 90Hz, 120Hz & 144Hz")
                    InfoRow(label = "Orientation", value = "Optimized for Portrait & Flat Tabletop")
                    InfoRow(label = "RAM Recommended", value = "2 GB RAM minimum")
                    InfoRow(label = "Hardware Sensors", value = "Haptic Vibrator & Multitouch")
                }
            }

            // App Size & Storage Saver
            item {
                AboutSectionCard(
                    title = "SIZE & STORAGE OPTIMIZATIONS",
                    icon = Icons.Default.Save,
                    accentColor = Color(0xFFA855F7)
                ) {
                    InfoRow(label = "Download Size", value = "~22 MB (Ultra Lightweight)")
                    InfoRow(label = "Match Retention", value = "Max 10 matches stored (Auto-Trimmed)")
                    InfoRow(label = "Audio Architecture", value = "0 MB assets (Procedural PCM Audio)")
                    InfoRow(label = "Graphics Assets", value = "100% Vector Canvas (Zero Image Bloat)")
                    InfoRow(label = "Storage Footprint", value = "Kept minimal to protect phone disk space")
                }
            }

            // Privacy & Features Summary
            item {
                AboutSectionCard(
                    title = "PRIVACY & GAME SPECIFICATIONS",
                    icon = Icons.Default.Security,
                    accentColor = Color(0xFFEC4899)
                ) {
                    InfoRow(label = "Offline Play", value = "100% Offline (No Internet Required)")
                    InfoRow(label = "Accounts / Sign-in", value = "None Required (Instant Play)")
                    InfoRow(label = "Data Tracking", value = "Zero Tracking / Telemetry")
                    InfoRow(label = "Game Modes", value = "2-Player Duel, Solo AI, Tournament, Practice")
                    InfoRow(label = "AI Difficulties", value = "Easy, Medium, Hard, Insane")
                    InfoRow(label = "Arenas Available", value = "Stadium, Cyberpunk, Volcano, Glacier")
                }
            }

            // Footer
            item {
                PrometrionFooter()
            }
        }
    }
}

@Composable
private fun AboutSectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            lineHeight = 17.sp,
            modifier = Modifier
                .weight(0.42f)
                .padding(end = 10.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            lineHeight = 17.sp,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun TacticPoint(emoji: String, name: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = description, color = Color(0xFFCBD5E1), fontSize = 12.sp, lineHeight = 16.sp)
    }
}
