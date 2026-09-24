package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlayerStats
import com.example.model.AiDifficulty
import com.example.model.GameMode
import com.example.model.TournamentStage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentScreen(
    playerStats: PlayerStats?,
    onStartStage: (TournamentStage) -> Unit,
    onBack: () -> Unit
) {
    val unlockedStage = playerStats?.tournamentMaxStage ?: 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TOURNAMENT TOWER",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tournament_back_button")) {
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
            // Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFFFBBF24), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👑", fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Path to the Crown",
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Defeat 5 consecutive tug legends to prove yourself as the greatest puller on Earth!",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Stages List
            items(TournamentStage.entries) { stage ->
                val isUnlocked = stage.stageNumber <= unlockedStage
                val isCurrent = stage.stageNumber == unlockedStage
                val isBeaten = stage.stageNumber < unlockedStage

                StageCard(
                    stage = stage,
                    isUnlocked = isUnlocked,
                    isCurrent = isCurrent,
                    isBeaten = isBeaten,
                    onPlay = { if (isUnlocked) onStartStage(stage) }
                )
            }
        }
    }
}

@Composable
private fun StageCard(
    stage: TournamentStage,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    isBeaten: Boolean,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrent -> Color(0xFF1E2A44)
                isUnlocked -> Color(0xFF161F30)
                else -> Color(0xFF0F141F)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = when {
                    isCurrent -> Color(0xFF38BDF8)
                    isBeaten -> Color(0xFF22C55E)
                    else -> Color(0xFF334155)
                },
                shape = RoundedCornerShape(20.dp)
            )
            .shadow(if (isCurrent) 8.dp else 2.dp)
            .testTag("tournament_stage_${stage.stageNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stage Number / Boss Avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Color(0xFF334155) else Color(0xFF1E293B)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(text = stage.difficulty.icon, fontSize = 28.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "STAGE ${stage.stageNumber}",
                        color = if (isCurrent) Color(0xFF38BDF8) else if (isBeaten) Color(0xFF22C55E) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (isBeaten) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "✓ CLEARED", color = Color(0xFF22C55E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = stage.bossName,
                    color = if (isUnlocked) Color.White else Color.Gray,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${stage.arenaTitle} • %.1f TPS".format(stage.difficulty.baseTps),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            if (isUnlocked) {
                Button(
                    onClick = onPlay,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrent) Color(0xFF2563EB) else Color(0xFF334155)
                    ),
                    modifier = Modifier.testTag("play_stage_${stage.stageNumber}")
                ) {
                    Text(if (isBeaten) "Replay" else "BATTLE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}
