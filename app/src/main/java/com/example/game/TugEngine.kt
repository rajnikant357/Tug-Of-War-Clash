package com.example.game

import com.example.model.AiDifficulty
import com.example.model.GameMode
import com.example.model.PlayerState
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class TugGameSnapshot(
    val ropePosition: Float = 0f, // -1.0 (P1 win) to +1.0 (P2/AI win)
    val ropeVelocity: Float = 0f,
    val player1: PlayerState = PlayerState(name = "Player 1", colorHex = 0xFF2563EB, teamIcon = "🔷"),
    val player2: PlayerState = PlayerState(name = "Player 2", colorHex = 0xFFDC2626, teamIcon = "🔶"),
    val isGameActive: Boolean = false,
    val isCountingDown: Boolean = false,
    val countdownNumber: Int = 3,
    val winnerSide: Int = 0, // -1 for P1, 1 for P2, 0 for ongoing
    val matchDurationSec: Int = 0,
    val suddenDeath: Boolean = false,
    val screenShake: Float = 0f, // 0.0 to 1.0 decay
    val lastBigPullSide: Int = 0 // -1 or 1 for visual banner
)

class TugEngine(
    var gameMode: GameMode = GameMode.TWO_PLAYER_LOCAL,
    var aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
    var p1Name: String = "Player 1",
    var p2Name: String = "Player 2"
) {
    var snapshot = TugGameSnapshot()
        private set

    private val p1TapTimestamps = mutableListOf<Long>()
    private val p2TapTimestamps = mutableListOf<Long>()

    private var lastAiTapTime = 0L
    private var aiNextIntervalMs = 200L

    companion object {
        const val WIN_THRESHOLD = 0.88f
        const val BASE_TAP_IMPULSE = 0.038f
        const val SURGE_IMPULSE = 0.12f
        const val FRICTION = 0.88f
    }

    fun startCountdown() {
        p1TapTimestamps.clear()
        p2TapTimestamps.clear()
        val p2DisplayName = if (gameMode == GameMode.TWO_PLAYER_LOCAL) p2Name else aiDifficulty.displayName
        val p2Icon = if (gameMode == GameMode.TWO_PLAYER_LOCAL) "🔶" else aiDifficulty.icon

        snapshot = TugGameSnapshot(
            ropePosition = 0f,
            ropeVelocity = 0f,
            player1 = PlayerState(name = p1Name, colorHex = 0xFF2563EB, teamIcon = "🔷"),
            player2 = PlayerState(name = p2DisplayName, colorHex = 0xFFDC2626, teamIcon = p2Icon),
            isGameActive = false,
            isCountingDown = true,
            countdownNumber = 3,
            winnerSide = 0,
            matchDurationSec = 0
        )
    }

    fun setCountdownNumber(num: Int) {
        snapshot = snapshot.copy(countdownNumber = num)
    }

    fun startGame() {
        snapshot = snapshot.copy(
            isGameActive = true,
            isCountingDown = false,
            countdownNumber = 0
        )
    }

    fun registerPlayerTap(isPlayer1: Boolean, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        if (!snapshot.isGameActive || snapshot.winnerSide != 0) return false

        val tapsList = if (isPlayer1) p1TapTimestamps else p2TapTimestamps
        tapsList.add(currentTimeMs)
        // Keep only taps in the last 1000ms
        tapsList.removeAll { currentTimeMs - it > 1000L }
        val currentTps = tapsList.size.toFloat()

        val player = if (isPlayer1) snapshot.player1 else snapshot.player2
        val newPeakTps = max(player.peakTps, currentTps)

        // Surge meter buildup: fast tapping increases surge
        var surge = player.surgeMeter + 0.15f
        var didSurge = false

        var impulse = BASE_TAP_IMPULSE

        // Comeback bonus: if player is losing, their pulls get up to 25% extra grip!
        val isLosing = if (isPlayer1) snapshot.ropePosition > 0.2f else snapshot.ropePosition < -0.2f
        if (isLosing) {
            val lossDist = abs(snapshot.ropePosition)
            impulse *= (1.0f + lossDist * 0.35f)
        }

        if (surge >= 1.0f) {
            // Trigger SUPER YANK!
            surge = 0f
            didSurge = true
            impulse += SURGE_IMPULSE
        }

        val updatedPlayer = player.copy(
            tapsCount = player.tapsCount + 1,
            currentTps = currentTps,
            peakTps = newPeakTps,
            surgeMeter = surge.coerceIn(0f, 1f),
            isSurging = didSurge,
            lastTapTime = currentTimeMs
        )

        // Velocity adjustment: P1 pulls negative, P2 pulls positive
        val dir = if (isPlayer1) -1f else 1f
        val newVelocity = snapshot.ropeVelocity + (dir * impulse)
        val shake = if (didSurge) 1.0f else (snapshot.screenShake + 0.15f).coerceAtMost(0.6f)

        snapshot = snapshot.copy(
            player1 = if (isPlayer1) updatedPlayer else snapshot.player1,
            player2 = if (!isPlayer1) updatedPlayer else snapshot.player2,
            ropeVelocity = newVelocity,
            screenShake = shake,
            lastBigPullSide = if (didSurge) dir.toInt() else snapshot.lastBigPullSide
        )

        return didSurge
    }

    fun updatePhysics(deltaSec: Float, currentTimeMs: Long = System.currentTimeMillis()) {
        if (!snapshot.isGameActive || snapshot.winnerSide != 0) return

        // AI Tapping simulation when in AI or Tournament mode
        if (gameMode == GameMode.SOLO_VS_AI || gameMode == GameMode.TOURNAMENT) {
            simulateAiTap(currentTimeMs)
        }

        // Clean up tap window for TPS decay
        p1TapTimestamps.removeAll { currentTimeMs - it > 1000L }
        p2TapTimestamps.removeAll { currentTimeMs - it > 1000L }

        // Natural surge meter decay over time if idle
        val p1SurgeDecay = (snapshot.player1.surgeMeter - deltaSec * 0.25f).coerceAtLeast(0f)
        val p2SurgeDecay = (snapshot.player2.surgeMeter - deltaSec * 0.25f).coerceAtLeast(0f)

        val updatedP1 = snapshot.player1.copy(
            currentTps = p1TapTimestamps.size.toFloat(),
            surgeMeter = p1SurgeDecay,
            isSurging = false
        )
        val updatedP2 = snapshot.player2.copy(
            currentTps = p2TapTimestamps.size.toFloat(),
            surgeMeter = p2SurgeDecay,
            isSurging = false
        )

        // Apply velocity to position
        val newPos = (snapshot.ropePosition + snapshot.ropeVelocity * deltaSec * 8f).coerceIn(-1.0f, 1.0f)
        // Apply friction
        val newVel = snapshot.ropeVelocity * (1f - (1f - FRICTION) * deltaSec * 30f)
        // Screen shake decay
        val newShake = (snapshot.screenShake - deltaSec * 2.5f).coerceAtLeast(0f)

        var winner = 0
        if (newPos <= -WIN_THRESHOLD) {
            winner = -1 // Player 1 wins!
        } else if (newPos >= WIN_THRESHOLD) {
            winner = 1 // Player 2 / AI wins!
        }

        snapshot = snapshot.copy(
            ropePosition = newPos,
            ropeVelocity = newVel,
            screenShake = newShake,
            player1 = updatedP1,
            player2 = updatedP2,
            winnerSide = winner,
            isGameActive = winner == 0
        )
    }

    private fun simulateAiTap(currentTimeMs: Long) {
        if (currentTimeMs - lastAiTapTime < aiNextIntervalMs) return

        // Calculate dynamic AI speed
        // If AI is losing (rope is pulled towards P1, negative pos), AI gets desperate/rages!
        val isLosing = snapshot.ropePosition < -0.15f
        val rageBoost = if (isLosing) 1.25f else 1.0f

        val effectiveTps = aiDifficulty.baseTps * rageBoost
        val meanIntervalMs = (1000f / effectiveTps).toLong()
        val jitter = (meanIntervalMs * 0.25f).toLong()
        aiNextIntervalMs = (meanIntervalMs + Random.nextLong(-jitter, jitter + 1)).coerceAtLeast(60L)

        // Burst probability
        if (Random.nextFloat() < aiDifficulty.burstProbability) {
            aiNextIntervalMs = (aiNextIntervalMs * 0.5f).toLong().coerceAtLeast(50L)
        }

        lastAiTapTime = currentTimeMs
        registerPlayerTap(isPlayer1 = false, currentTimeMs = currentTimeMs)
    }

    fun incrementMatchSecond() {
        if (snapshot.isGameActive) {
            snapshot = snapshot.copy(matchDurationSec = snapshot.matchDurationSec + 1)
        }
    }
}
