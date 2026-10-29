package com.example.chess.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchRecord): Long

    @Query("SELECT * FROM match_records ORDER BY playedAt DESC")
    fun getAllMatches(): Flow<List<MatchRecord>>

    @Query("DELETE FROM match_records WHERE id = :id")
    suspend fun deleteMatch(id: Long)

    @Query("DELETE FROM match_records")
    suspend fun clearAll()
}
