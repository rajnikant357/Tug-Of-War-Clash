package com.example.data

import com.example.data.db.AppDatabase
import com.example.data.db.MatchRecord
import com.example.data.db.PlayerStats
import kotlinx.coroutines.flow.Flow

class GameRepository(private val database: AppDatabase) {
    val matchRecords: Flow<List<MatchRecord>> = database.matchRecordDao().getAllRecords()
    val playerStats: Flow<PlayerStats?> = database.playerStatsDao().getStats()

    suspend fun saveMatch(
        mode: String,
        winnerName: String,
        p1Name: String,
        p2Name: String,
        p1Taps: Int,
        p2Taps: Int,
        p1MaxTps: Float,
        p2MaxTps: Float,
        durationSec: Int,
        isP1Winner: Boolean
    ) {
        val record = MatchRecord(
            gameMode = mode,
            winnerName = winnerName,
            p1Name = p1Name,
            p2Name = p2Name,
            p1Taps = p1Taps,
            p2Taps = p2Taps,
            p1MaxTps = p1MaxTps,
            p2MaxTps = p2MaxTps,
            durationSeconds = durationSec
        )
        database.matchRecordDao().insertRecord(record)
        database.matchRecordDao().trimOldRecords()

        val currentStats = database.playerStatsDao().getStatsSync() ?: PlayerStats()
        val highestTps = maxOf(currentStats.highestTpsRecord, p1MaxTps, p2MaxTps)
        val newWins = currentStats.totalWins + (if (isP1Winner) 1 else 0)
        val newLosses = currentStats.totalLosses + (if (!isP1Winner && mode != "TWO_PLAYER_LOCAL") 1 else 0)

        database.playerStatsDao().updateStats(
            currentStats.copy(
                totalMatches = currentStats.totalMatches + 1,
                totalWins = newWins,
                totalLosses = newLosses,
                highestTpsRecord = highestTps,
                totalTapsOverall = currentStats.totalTapsOverall + p1Taps + p2Taps
            )
        )
    }

    suspend fun updateTournamentStage(stageBeaten: Int) {
        val currentStats = database.playerStatsDao().getStatsSync() ?: PlayerStats()
        if (stageBeaten >= currentStats.tournamentMaxStage) {
            database.playerStatsDao().updateStats(
                currentStats.copy(
                tournamentMaxStage = (stageBeaten + 1).coerceAtMost(5)
                )
            )
        }
    }

    suspend fun recordPracticeScore(tps: Float, totalTaps: Int) {
        val currentStats = database.playerStatsDao().getStatsSync() ?: PlayerStats()
        database.playerStatsDao().updateStats(
            currentStats.copy(
                highestTpsRecord = maxOf(currentStats.highestTpsRecord, tps),
                totalTapsOverall = currentStats.totalTapsOverall + totalTaps
            )
        )
    }

    suspend fun clearHistory() {
        database.matchRecordDao().clearAll()
    }

    suspend fun trimStorage() {
        database.matchRecordDao().trimOldRecords()
    }

    suspend fun resetAllData() {
        database.matchRecordDao().clearAll()
        database.playerStatsDao().updateStats(PlayerStats(id = 1))
    }
}
