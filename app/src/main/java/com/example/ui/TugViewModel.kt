package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.GameRepository
import com.example.data.db.AppDatabase
import com.example.data.db.MatchRecord
import com.example.data.db.PlayerStats
import com.example.game.TugEngine
import com.example.game.TugGameSnapshot
import com.example.model.AiDifficulty
import com.example.model.ArenaTheme
import com.example.model.GameMode
import com.example.model.TournamentStage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data class Game(val mode: GameMode, val difficulty: AiDifficulty = AiDifficulty.MEDIUM, val tournamentStage: TournamentStage? = null) : Screen()
    data object Tournament : Screen()
    data object Stats : Screen()
    data object Settings : Screen()
    data object Practice : Screen()
    data object About : Screen()
}

class TugViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = GameRepository(database)
    val soundManager = SoundManager(application)

    val matchRecords: StateFlow<List<MatchRecord>> = repository.matchRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerStats: StateFlow<PlayerStats?> = repository.playerStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _arenaTheme = MutableStateFlow(ArenaTheme.STADIUM)
    val arenaTheme: StateFlow<ArenaTheme> = _arenaTheme.asStateFlow()

    // Practice mode state
    private val _practiceTimeRemaining = MutableStateFlow(10)
    val practiceTimeRemaining: StateFlow<Int> = _practiceTimeRemaining.asStateFlow()

    private val _practiceTaps = MutableStateFlow(0)
    val practiceTaps: StateFlow<Int> = _practiceTaps.asStateFlow()

    private val _practiceActive = MutableStateFlow(false)
    val practiceActive: StateFlow<Boolean> = _practiceActive.asStateFlow()

    private val _practiceFinished = MutableStateFlow(false)
    val practiceFinished: StateFlow<Boolean> = _practiceFinished.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val engine = TugEngine()
    private val _snapshot = MutableStateFlow(engine.snapshot)
    val snapshot: StateFlow<TugGameSnapshot> = _snapshot.asStateFlow()

    private var physicsJob: Job? = null
    private var timerJob: Job? = null
    private var countdownJob: Job? = null

    var currentStage: TournamentStage? = null

    init {
        viewModelScope.launch {
            repository.trimStorage()
        }
    }

    fun navigateTo(screen: Screen) {
        if (screen is Screen.Home) {
            stopGame()
        }
        _isPaused.value = false
        _currentScreen.value = screen
    }

    fun setArenaTheme(theme: ArenaTheme) {
        _arenaTheme.value = theme
    }

    fun toggleSound(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
    }

    fun toggleHaptics(enabled: Boolean) {
        soundManager.isHapticsEnabled = enabled
    }

    fun startMatch(mode: GameMode, difficulty: AiDifficulty = AiDifficulty.MEDIUM, stage: TournamentStage? = null) {
        stopGame()
        _isPaused.value = false
        currentStage = stage
        if (stage != null) {
            _arenaTheme.value = stage.defaultArena
        }
        engine.gameMode = mode
        engine.aiDifficulty = difficulty
        engine.p1Name = "Player 1"
        engine.p2Name = if (mode == GameMode.TWO_PLAYER_LOCAL) "Player 2" else difficulty.displayName

        _snapshot.value = engine.snapshot
        _currentScreen.value = Screen.Game(mode, difficulty, stage)

        // Begin Countdown sequence
        countdownJob = viewModelScope.launch {
            engine.startCountdown()
            _snapshot.value = engine.snapshot

            for (count in 3 downTo 1) {
                engine.setCountdownNumber(count)
                _snapshot.value = engine.snapshot
                soundManager.playCountdownBeep(isFinal = false)
                delay(900)
            }

            // PULL!
            engine.setCountdownNumber(0)
            engine.startGame()
            _snapshot.value = engine.snapshot
            soundManager.playWhistle()

            startPhysicsLoop()
            startMatchTimer()
        }
    }

    fun onPlayerTap(isPlayer1: Boolean) {
        if (_isPaused.value || !engine.snapshot.isGameActive) return
        val didSurge = engine.registerPlayerTap(isPlayer1)
        if (didSurge) {
            soundManager.playSurge()
        } else {
            soundManager.playTap(isPlayer1)
        }
        _snapshot.value = engine.snapshot
    }

    fun pauseGame() {
        if (engine.snapshot.winnerSide != 0) return
        _isPaused.value = true
        physicsJob?.cancel()
        timerJob?.cancel()
        countdownJob?.cancel()
    }

    fun resumeGame() {
        if (!_isPaused.value) return
        _isPaused.value = false
        if (engine.snapshot.isGameActive && engine.snapshot.winnerSide == 0) {
            startPhysicsLoop()
            startMatchTimer()
        } else if (engine.snapshot.isCountingDown) {
            engine.startGame()
            _snapshot.value = engine.snapshot
            soundManager.playWhistle()
            startPhysicsLoop()
            startMatchTimer()
        }
    }

    private fun startPhysicsLoop() {
        physicsJob?.cancel()
        physicsJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            var strainTimer = 0f

            while (isActive && engine.snapshot.isGameActive) {
                val now = System.currentTimeMillis()
                val deltaSec = ((now - lastTime) / 1000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                engine.updatePhysics(deltaSec, now)
                _snapshot.value = engine.snapshot

                strainTimer += deltaSec
                if (strainTimer >= 0.35f) {
                    strainTimer = 0f
                    if (kotlin.math.abs(engine.snapshot.ropeVelocity) > 0.08f) {
                        soundManager.playStrain()
                    }
                }

                if (engine.snapshot.winnerSide != 0) {
                    onMatchFinished()
                    break
                }

                delay(16) // ~60 FPS
            }
        }
    }

    private fun startMatchTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && engine.snapshot.isGameActive) {
                delay(1000)
                engine.incrementMatchSecond()
                _snapshot.value = engine.snapshot
            }
        }
    }

    private fun onMatchFinished() {
        physicsJob?.cancel()
        timerJob?.cancel()
        soundManager.playVictoryFanfare()

        val snap = engine.snapshot
        val isP1Winner = snap.winnerSide == -1
        val winnerName = if (isP1Winner) snap.player1.name else snap.player2.name

        viewModelScope.launch {
            repository.saveMatch(
                mode = engine.gameMode.name,
                winnerName = winnerName,
                p1Name = snap.player1.name,
                p2Name = snap.player2.name,
                p1Taps = snap.player1.tapsCount,
                p2Taps = snap.player2.tapsCount,
                p1MaxTps = snap.player1.peakTps,
                p2MaxTps = snap.player2.peakTps,
                durationSec = snap.matchDurationSec,
                isP1Winner = isP1Winner
            )

            // If tournament match and P1 won, unlock next stage
            if (engine.gameMode == GameMode.TOURNAMENT && isP1Winner && currentStage != null) {
                repository.updateTournamentStage(currentStage!!.stageNumber)
            }
        }
    }

    fun restartCurrentMatch() {
        val current = _currentScreen.value
        if (current is Screen.Game) {
            startMatch(current.mode, current.difficulty, current.tournamentStage)
        }
    }

    fun advanceToNextTournamentStage() {
        val nextStageNumber = (currentStage?.stageNumber ?: 1) + 1
        val nextStage = TournamentStage.entries.find { it.stageNumber == nextStageNumber }
        if (nextStage != null) {
            startMatch(GameMode.TOURNAMENT, nextStage.difficulty, nextStage)
        } else {
            navigateTo(Screen.Tournament)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    fun stopGame() {
        _isPaused.value = false
        physicsJob?.cancel()
        timerJob?.cancel()
        countdownJob?.cancel()
    }

    // Practice Mode (10s Tap Speed Test)
    fun startPractice() {
        _practiceTimeRemaining.value = 10
        _practiceTaps.value = 0
        _practiceFinished.value = false
        _practiceActive.value = true

        viewModelScope.launch {
            soundManager.playWhistle()
            while (_practiceTimeRemaining.value > 0) {
                delay(1000)
                _practiceTimeRemaining.value -= 1
                if (_practiceTimeRemaining.value <= 3 && _practiceTimeRemaining.value > 0) {
                    soundManager.playCountdownBeep(isFinal = false)
                }
            }
            _practiceActive.value = false
            _practiceFinished.value = true
            soundManager.playVictoryFanfare()

            val total = _practiceTaps.value
            val tps = total / 10f
            repository.recordPracticeScore(tps, total)
        }
    }

    fun registerPracticeTap() {
        if (_practiceActive.value) {
            _practiceTaps.value += 1
            soundManager.playTap(isPlayer1 = true)
        }
    }

    fun clearMatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }
}
