package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_records")
data class MatchRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameMode: String,
    val winnerName: String,
    val p1Name: String,
    val p2Name: String,
    val p1Taps: Int,
    val p2Taps: Int,
    val p1MaxTps: Float,
    val p2MaxTps: Float,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_stats")
data class PlayerStats(
    @PrimaryKey val id: Int = 1,
    val totalMatches: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val tournamentMaxStage: Int = 1,
    val highestTpsRecord: Float = 0f,
    val totalTapsOverall: Long = 0L
)
