package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.TugGameSnapshot
import com.example.model.ArenaTheme
import com.example.model.GameMode
import com.example.model.TournamentStage
import com.example.ui.components.RopeArenaCanvas
import com.example.ui.components.TapZone
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun GameScreen(
    snapshot: TugGameSnapshot,
    gameMode: GameMode,
    tournamentStage: TournamentStage? = null,
    arenaTheme: ArenaTheme,
    isPaused: Boolean = false,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onPlayerTap: (Boolean) -> Unit,
    onRestart: () -> Unit,
    onNextStage: () -> Unit = {},
    onExit: () -> Unit
) {
    // Screen shake offset calculation
    val shakeX = remember(snapshot.screenShake) {
        if (snapshot.screenShake > 0.05f) {
            (Random.nextFloat() - 0.5f) * snapshot.screenShake * 30f
        } else 0f
    }
    val shakeY = remember(snapshot.screenShake) {
        if (snapshot.screenShake > 0.05f) {
            (Random.nextFloat() - 0.5f) * snapshot.screenShake * 30f
        } else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .offset { IntOffset(shakeX.roundToInt(), shakeY.roundToInt()) }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP SECTION: Player 2 TapZone or AI Opponent HUD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (gameMode == GameMode.TWO_PLAYER_LOCAL) {
                    TapZone(
                        player = snapshot.player2,
                        isRotated = true,
                        enabled = snapshot.isGameActive && !isPaused,
                        testTag = "tap_button_p2",
                        onTap = { onPlayerTap(false) }
                    )
                } else {
                    AiOpponentCard(
                        player = snapshot.player2,
                        isActive = snapshot.isGameActive && !isPaused
                    )
                }

                // Top-Left Pause Match Button
                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .testTag("pause_game_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0x990F172A),
                        border = BorderStroke(1.5.dp, Color(0xFF334155)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // MIDDLE SECTION: Rope Arena Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                RopeArenaCanvas(
                    snapshot = snapshot,
                    arenaTheme = arenaTheme
                )

                // Match Timer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x88000000),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "⏱ %02d:%02d".format(snapshot.matchDurationSec / 60, snapshot.matchDurationSec % 60),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // BOTTOM SECTION: Player 1 TapZone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                TapZone(
                    player = snapshot.player1,
                    isRotated = false,
                    enabled = snapshot.isGameActive && !isPaused,
                    testTag = "tap_button_p1",
                    onTap = { onPlayerTap(true) }
                )
            }
        }

        // Countdown Overlay (3, 2, 1, PULL!)
        if (snapshot.isCountingDown) {
            CountdownOverlay(countdownNumber = snapshot.countdownNumber)
        }

        // Victory / Defeat Match-Over Dialog
        if (snapshot.winnerSide != 0) {
            GameOverDialog(
                snapshot = snapshot,
                gameMode = gameMode,
                tournamentStage = tournamentStage,
                onRestart = onRestart,
                onNextStage = onNextStage,
                onExit = onExit
            )
        }

        // Pause Menu Dialog Overlay
        if (isPaused && snapshot.winnerSide == 0) {
            PauseDialog(
                onResume = onResume,
                onRestart = onRestart,
                onExit = onExit
            )
        }
    }
}

@Composable
private fun AiOpponentCard(
    player: com.example.model.PlayerState,
    isActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFDC2626).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .shadow(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = player.teamIcon,
                    fontSize = 42.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = player.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.1f TPS".format(player.currentTps),
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Current Speed",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${player.tapsCount}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Total Yanks",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownOverlay(countdownNumber: Int) {
    val scale = remember(countdownNumber) { Animatable(0.4f) }

    LaunchedEffect(countdownNumber) {
        scale.animateTo(1.2f, tween(300, easing = FastOutSlowInEasing))
        scale.animateTo(1.0f, tween(150))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xBB000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (countdownNumber > 0) "$countdownNumber" else "PULL!",
                color = if (countdownNumber > 0) Color(0xFFFBBF24) else Color(0xFF22C55E),
                fontSize = if (countdownNumber > 0) 100.sp else 72.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale.value)
            )
            Text(
                text = if (countdownNumber > 0) "GET READY TO TAP!" else "TUG FOR VICTORY!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun GameOverDialog(
    snapshot: TugGameSnapshot,
    gameMode: GameMode,
    tournamentStage: TournamentStage? = null,
    onRestart: () -> Unit,
    onNextStage: () -> Unit,
    onExit: () -> Unit
) {
    val isP1Winner = snapshot.winnerSide == -1
    val winnerName = if (isP1Winner) snapshot.player1.name else snapshot.player2.name
    val winnerIcon = if (isP1Winner) snapshot.player1.teamIcon else snapshot.player2.teamIcon
    val isSoloWon = gameMode != GameMode.TWO_PLAYER_LOCAL && isP1Winner
    val isSoloLost = gameMode != GameMode.TWO_PLAYER_LOCAL && !isP1Winner

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD0B0F19)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(
                    width = 2.dp,
                    color = if (isSoloLost) Color(0xFFDC2626) else Color(0xFFFBBF24),
                    shape = RoundedCornerShape(28.dp)
                )
                .shadow(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy / Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                if (isSoloLost) listOf(Color(0xFFEF4444), Color(0xFF7F1D1D))
                                else listOf(Color(0xFFFBBF24), Color(0xFFB45309))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSoloLost) "💀" else "🏆",
                        fontSize = 38.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (gameMode == GameMode.TWO_PLAYER_LOCAL) "$winnerName WINS!"
                    else if (isSoloWon) "VICTORY!" else "DEFEAT!",
                    color = if (isSoloLost) Color(0xFFF87171) else Color(0xFFFBBF24),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = if (gameMode == GameMode.TWO_PLAYER_LOCAL) "The champion pulled the rope past the line!"
                    else if (isSoloWon) "You overpowered the opponent!"
                    else "The opponent dragged you into the hazard!",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // Match Statistics Grid
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Match Time:", color = Color.Gray, fontSize = 13.sp)
                            Text(
                                "%02d:%02d".format(snapshot.matchDurationSec / 60, snapshot.matchDurationSec % 60),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${snapshot.player1.name} Taps:", color = Color(0xFF60A5FA), fontSize = 13.sp)
                            Text(
                                "${snapshot.player1.tapsCount} (${snapshot.player1.peakTps.toInt()} Max TPS)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${snapshot.player2.name} Taps:", color = Color(0xFFF87171), fontSize = 13.sp)
                            Text(
                                "${snapshot.player2.tapsCount} (${snapshot.player2.peakTps.toInt()} Max TPS)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Hamburger Menu Button, Restart/New Game, and Next (tournament only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hamburger Menu Button (replaces 'Menu' text)
                    OutlinedButton(
                        onClick = onExit,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF475569)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF0F172A)
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("match_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    val restartButtonLabel = when (gameMode) {
                        GameMode.TWO_PLAYER_LOCAL -> "New Game"
                        GameMode.TOURNAMENT -> "Restart"
                        else -> "Rematch"
                    }

                    Button(
                        onClick = onRestart,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gameMode == GameMode.TOURNAMENT && isP1Winner) Color(0xFF334155) else Color(0xFF2563EB)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("match_rematch_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = restartButtonLabel,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = restartButtonLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Tournament only: Add 'Next' button beside Restart button
                    if (gameMode == GameMode.TOURNAMENT) {
                        Button(
                            onClick = onNextStage,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("match_next_stage_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Next",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC050811))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(2.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .padding(16.dp)
                .testTag("pause_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pause Icon Badge
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0F172A),
                    border = BorderStroke(2.dp, Color(0xFF38BDF8)),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Paused",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "MATCH PAUSED",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Take a breath! What would you like to do?",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Option 1: Resume Button (resumes the game where it was)
                Button(
                    onClick = onResume,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pause_resume_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume Game",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resume Game",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: Restart Button (restarts the game)
                Button(
                    onClick = onRestart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pause_restart_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Match",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Restart Match",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 3: Quit Button (quits the game to home menu screen)
                OutlinedButton(
                    onClick = onExit,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pause_quit_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Quit to Home",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quit to Home",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFF87171)
                        )
                    }
                }
            }
        }
    }
}
