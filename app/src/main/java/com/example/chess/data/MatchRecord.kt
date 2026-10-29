package com.example.chess.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_records")
data class MatchRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playedAt: Long = System.currentTimeMillis(),
    val whitePlayer: String = "Player 1",
    val blackPlayer: String = "Player 2",
    val winner: String, // "WHITE", "BLACK", "DRAW"
    val winReason: String, // "CHECKMATE", "TIMEOUT", "RESIGNATION", "STALEMATE", "50_MOVES", "INSUFFICIENT_MATERIAL"
    val timeControlName: String = "Casual",
    val totalMoves: Int = 0,
    val durationSeconds: Long = 0L,
    val pgnMoves: String = ""
)
