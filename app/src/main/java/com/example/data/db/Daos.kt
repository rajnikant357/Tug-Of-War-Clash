package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchRecordDao {
    @Query("SELECT * FROM match_records ORDER BY timestamp DESC LIMIT 10")
    fun getAllRecords(): Flow<List<MatchRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MatchRecord): Long

    @Query("DELETE FROM match_records WHERE id NOT IN (SELECT id FROM match_records ORDER BY timestamp DESC LIMIT 10)")
    suspend fun trimOldRecords()

    @Query("DELETE FROM match_records")
    suspend fun clearAll()
}

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1")
    fun getStats(): Flow<PlayerStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStats(stats: PlayerStats)

    @Query("SELECT * FROM player_stats WHERE id = 1")
    suspend fun getStatsSync(): PlayerStats?
}
