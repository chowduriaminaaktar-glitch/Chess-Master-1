package com.example

import com.example.chess.model.BoardState
import com.example.chess.model.ChessEngine
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testInitialBoardSetupAndMoveCount() {
        val initial = BoardState.initial()
        assertEquals(PieceColor.WHITE, initial.activeColor)

        // White has 20 possible starting legal moves (16 pawn moves + 4 knight moves)
        val legalMoves = ChessEngine.getLegalMoves(initial)
        assertEquals(20, legalMoves.size)

        val (wScore, bScore) = ChessEngine.calculateMaterialAdvantage(initial)
        assertEquals(39, wScore)
        assertEquals(39, bScore)

        assertFalse(ChessEngine.isKingInCheck(initial, PieceColor.WHITE))
        assertFalse(ChessEngine.isKingInCheck(initial, PieceColor.BLACK))
    }

    @Test
    fun testFoolsMateCheckmate() {
        // 1. f3 e5 2. g4 Qh4#
        var state = BoardState.initial()

        // 1. f3 (f2 -> f3: row 6 col 5 -> row 5 col 5)
        val m1 = Move(Square(6, 5), Square(5, 5))
        state = ChessEngine.makeMove(state, m1).first

        // 1... e5 (e7 -> e5: row 1 col 4 -> row 3 col 4)
        val m2 = Move(Square(1, 4), Square(3, 4))
        state = ChessEngine.makeMove(state, m2).first

        // 2. g4 (g2 -> g4: row 6 col 6 -> row 4 col 6)
        val m3 = Move(Square(6, 6), Square(4, 6))
        state = ChessEngine.makeMove(state, m3).first

        // 2... Qh4# (d8 -> h4: row 0 col 3 -> row 4 col 7)
        val m4 = Move(Square(0, 3), Square(4, 7))
        val (finalState, record) = ChessEngine.makeMove(state, m4)

        assertTrue("White king must be in check", record.isCheck)
        assertTrue("White must be checkmated", record.isCheckmate)
        assertEquals("Qh4#", record.algebraicNotation)
        assertTrue(ChessEngine.getLegalMoves(finalState).isEmpty())
    }

    @Test
    fun testInsufficientMaterial() {
        // King vs King
        val squares = MutableList<Piece?>(64) { null }
        squares[4] = Piece(PieceType.KING, PieceColor.BLACK)
        squares[60] = Piece(PieceType.KING, PieceColor.WHITE)
        val state = BoardState(squares = squares)

        assertTrue(ChessEngine.isInsufficientMaterial(state))
    }
}
