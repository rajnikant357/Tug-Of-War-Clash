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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.PlayerStats
import com.example.model.AiDifficulty
import com.example.model.ArenaTheme
import com.example.model.GameMode
import com.example.ui.Screen
import com.example.ui.components.PrometrionFooter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    playerStats: PlayerStats?,
    currentArena: ArenaTheme,
    onSelectArena: (ArenaTheme) -> Unit,
    onNavigate: (Screen) -> Unit,
    onStartGame: (GameMode, AiDifficulty) -> Unit
) {
    var showAiDifficultyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Tug of War App Icon",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("header_app_icon")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "TUG OF WAR CLASH",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate(Screen.Stats) },
                        modifier = Modifier.testTag("stats_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Stats",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { onNavigate(Screen.Settings) },
                        modifier = Modifier.testTag("settings_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Player Highlights Banner (Stored on Device)
            item {
                StatsBanner(playerStats = playerStats)
            }

            // Arena Environment Selector Card
            item {
                ArenaSelectorCard(
                    currentArena = currentArena,
                    onSelectArena = onSelectArena
                )
            }

            // Mode 1: 2-Player Local Duel
            item {
                ModeCard(
                    title = "2-PLAYER SPLIT DUEL",
                    subtitle = "Face-to-face battle on 1 device. Who taps fastest?",
                    icon = Icons.Default.Group,
                    gradient = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
                    tag = "mode_two_player",
                    onClick = { onStartGame(GameMode.TWO_PLAYER_LOCAL, AiDifficulty.MEDIUM) }
                )
            }

            // Mode 2: Solo vs AI
            item {
                ModeCard(
                    title = "SOLO VS AI BOT",
                    subtitle = "Face off against Sloths, Cyborgs, and Beast Champions.",
                    icon = Icons.Default.SmartToy,
                    gradient = listOf(Color(0xFFDC2626), Color(0xFF991B1B)),
                    tag = "mode_vs_ai",
                    onClick = { showAiDifficultyDialog = true }
                )
            }

            // AI Difficulty Selection Popup
            if (showAiDifficultyDialog) {
                item {
                    AiDifficultySelector(
                        onSelectDifficulty = { diff ->
                            showAiDifficultyDialog = false
                            onStartGame(GameMode.SOLO_VS_AI, diff)
                        },
                        onDismiss = { showAiDifficultyDialog = false }
                    )
                }
            }

            // Mode 3: Tournament Mode
            item {
                ModeCard(
                    title = "TOURNAMENT GAUNTLET",
                    subtitle = "Conquer 5 stages of opponents to claim the Gold Trophy!",
                    icon = Icons.Default.EmojiEvents,
                    gradient = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                    tag = "mode_tournament",
                    onClick = { onNavigate(Screen.Tournament) }
                )
            }

            // Mode 4: 10s Speed Practice
            item {
                ModeCard(
                    title = "10s TAP SPEED PRACTICE",
                    subtitle = "Measure your maximum Taps Per Second (TPS) power rating!",
                    icon = Icons.Default.Speed,
                    gradient = listOf(Color(0xFF059669), Color(0xFF047857)),
                    tag = "mode_practice",
                    onClick = { onNavigate(Screen.Practice) }
                )
            }

            // Footer
            item {
                PrometrionFooter()
            }
        }
    }
}

@Composable
private fun ArenaSelectorCard(
    currentArena: ArenaTheme,
    onSelectArena: (ArenaTheme) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ARENA ENVIRONMENT",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = currentArena.displayName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArenaTheme.entries.forEach { arena ->
                    val isSelected = arena == currentArena
                    val arenaBorderColor = if (isSelected) Color(arena.themeColorHex) else Color(0xFF334155)
                    val arenaBg = if (isSelected) Color(arena.themeColorHex).copy(alpha = 0.2f) else Color(0xFF0F172A)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(arenaBg)
                            .border(if (isSelected) 2.dp else 1.dp, arenaBorderColor, RoundedCornerShape(12.dp))
                            .clickable { onSelectArena(arena) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = arena.icon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = arena.displayName.replace(" ", "\n"),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsBanner(playerStats: PlayerStats?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
            .shadow(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${playerStats?.totalWins ?: 0}",
                    color = Color(0xFF60A5FA),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Victories",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color(0xFF334155))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "TPS",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "%.1f".format(playerStats?.highestTpsRecord ?: 0f),
                        color = Color(0xFFFBBF24),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "Max TPS Record",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color(0xFF334155))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Stage ${playerStats?.tournamentMaxStage ?: 1}/5",
                    color = Color(0xFF34D399),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Trophy Run",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, gradient.first().copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
            .shadow(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AiDifficultySelector(
    onSelectDifficulty: (AiDifficulty) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFFDC2626), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT AI OPPONENT",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text(
                    text = "✕",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            AiDifficulty.entries.forEach { diff ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectDifficulty(diff) }
                        .testTag("ai_diff_${diff.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = diff.icon, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = diff.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = diff.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "%.1f TPS".format(diff.baseTps),
                                color = Color(0xFFF87171),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
