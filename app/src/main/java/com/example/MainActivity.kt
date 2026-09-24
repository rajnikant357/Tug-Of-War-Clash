package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AiDifficulty
import com.example.model.GameMode
import com.example.model.TournamentStage
import com.example.ui.Screen
import com.example.ui.TugViewModel
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TournamentScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val notificationBarHeight = rememberNotificationBarHeight()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0B0F19)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Blank top margin with the exact space occupied by the notification bar,
                        // filled with the same theme color as the header (0xFF0F172A).
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(notificationBarHeight)
                                .background(Color(0xFF0F172A))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            TugApp(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun rememberNotificationBarHeight(): Dp {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cutoutPadding = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    return remember(context, density, cutoutPadding) {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val resHeight = if (resourceId > 0) {
            val px = context.resources.getDimensionPixelSize(resourceId)
            with(density) { px.toDp() }
        } else {
            0.dp
        }
        maxOf(resHeight, cutoutPadding, 32.dp)
    }
}

@Composable
fun TugApp(viewModel: TugViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val playerStats by viewModel.playerStats.collectAsStateWithLifecycle()
    val matchRecords by viewModel.matchRecords.collectAsStateWithLifecycle()
    val arenaTheme by viewModel.arenaTheme.collectAsStateWithLifecycle()

    val practiceTimeRemaining by viewModel.practiceTimeRemaining.collectAsStateWithLifecycle()
    val practiceTaps by viewModel.practiceTaps.collectAsStateWithLifecycle()
    val practiceActive by viewModel.practiceActive.collectAsStateWithLifecycle()
    val practiceFinished by viewModel.practiceFinished.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()

    // Back handling - always composed with enabled state so back navigation works smoothly
    BackHandler(enabled = currentScreen !is Screen.Home) {
        if (currentScreen is Screen.Game) {
            if (isPaused || snapshot.winnerSide != 0) {
                viewModel.navigateTo(Screen.Home)
            } else {
                viewModel.pauseGame()
            }
        } else if (currentScreen is Screen.About) {
            viewModel.navigateTo(Screen.Settings)
        } else {
            viewModel.navigateTo(Screen.Home)
        }
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                playerStats = playerStats,
                currentArena = arenaTheme,
                onSelectArena = { viewModel.setArenaTheme(it) },
                onNavigate = { viewModel.navigateTo(it) },
                onStartGame = { mode, diff ->
                    viewModel.startMatch(mode, diff)
                }
            )
        }

        is Screen.Game -> {
            GameScreen(
                snapshot = snapshot,
                gameMode = screen.mode,
                tournamentStage = screen.tournamentStage,
                arenaTheme = arenaTheme,
                isPaused = isPaused,
                onPause = { viewModel.pauseGame() },
                onResume = { viewModel.resumeGame() },
                onPlayerTap = { isP1 -> viewModel.onPlayerTap(isP1) },
                onRestart = { viewModel.restartCurrentMatch() },
                onNextStage = { viewModel.advanceToNextTournamentStage() },
                onExit = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Tournament -> {
            TournamentScreen(
                playerStats = playerStats,
                onStartStage = { stage ->
                    viewModel.startMatch(GameMode.TOURNAMENT, stage.difficulty, stage)
                },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Practice -> {
            PracticeScreen(
                timeRemaining = practiceTimeRemaining,
                tapsCount = practiceTaps,
                isActive = practiceActive,
                isFinished = practiceFinished,
                onStartPractice = { viewModel.startPractice() },
                onTap = { viewModel.registerPracticeTap() },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Stats -> {
            StatsScreen(
                playerStats = playerStats,
                matchRecords = matchRecords,
                onClearHistory = { viewModel.clearMatchHistory() },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Settings -> {
            SettingsScreen(
                currentTheme = arenaTheme,
                isSoundEnabled = viewModel.soundManager.isSoundEnabled,
                isHapticsEnabled = viewModel.soundManager.isHapticsEnabled,
                onToggleSound = { viewModel.toggleSound(it) },
                onToggleHaptics = { viewModel.toggleHaptics(it) },
                onSelectTheme = { viewModel.setArenaTheme(it) },
                onResetAllData = { viewModel.resetAllData() },
                onNavigateAbout = { viewModel.navigateTo(Screen.About) },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.About -> {
            AboutScreen(
                onBack = { viewModel.navigateTo(Screen.Settings) }
            )
        }
    }
}
