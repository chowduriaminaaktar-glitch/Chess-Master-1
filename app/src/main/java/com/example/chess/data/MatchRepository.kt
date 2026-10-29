package com.example.chess.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class MatchStatistics(
    val totalMatches: Int = 0,
    val whiteWins: Int = 0,
    val blackWins: Int = 0,
    val draws: Int = 0,
    val whiteWinRate: Float = 0f,
    val blackWinRate: Float = 0f,
    val drawRate: Float = 0f
)

class MatchRepository(private val matchDao: MatchDao) {

    val allMatches: Flow<List<MatchRecord>> = matchDao.getAllMatches()

    val matchStatistics: Flow<MatchStatistics> = allMatches.map { list ->
        val total = list.size
        if (total == 0) {
            MatchStatistics()
        } else {
            val white = list.count { it.winner.equals("WHITE", ignoreCase = true) }
            val black = list.count { it.winner.equals("BLACK", ignoreCase = true) }
            val draws = list.count { it.winner.equals("DRAW", ignoreCase = true) }
            MatchStatistics(
                totalMatches = total,
                whiteWins = white,
                blackWins = black,
                draws = draws,
                whiteWinRate = (white.toFloat() / total) * 100f,
                blackWinRate = (black.toFloat() / total) * 100f,
                drawRate = (draws.toFloat() / total) * 100f
            )
        }
    }

    suspend fun saveMatch(match: MatchRecord): Long {
        return matchDao.insertMatch(match)
    }

    suspend fun deleteMatch(id: Long) {
        matchDao.deleteMatch(id)
    }

    suspend fun clearHistory() {
        matchDao.clearAll()
    }
}
