package com.example.chess.model

enum class PieceColor {
    WHITE, BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

enum class PieceType(val symbol: String, val baseValue: Int) {
    PAWN("P", 1),
    KNIGHT("N", 3),
    BISHOP("B", 3),
    ROOK("R", 5),
    QUEEN("Q", 9),
    KING("K", 0)
}

data class Piece(
    val type: PieceType,
    val color: PieceColor
) {
    val unicodeSymbol: String
        get() = when (color) {
            PieceColor.WHITE -> when (type) {
                PieceType.KING -> "♔"
                PieceType.QUEEN -> "♕"
                PieceType.ROOK -> "♖"
                PieceType.BISHOP -> "♗"
                PieceType.KNIGHT -> "♘"
                PieceType.PAWN -> "♙"
            }
            PieceColor.BLACK -> when (type) {
                PieceType.KING -> "♚"
                PieceType.QUEEN -> "♛"
                PieceType.ROOK -> "♜"
                PieceType.BISHOP -> "♝"
                PieceType.KNIGHT -> "♞"
                PieceType.PAWN -> "♟"
            }
        }
}

data class Square(val row: Int, val col: Int) {
    init {
        require(row in 0..7 && col in 0..7) { "Square out of bounds: row=$row, col=$col" }
    }

    val fileChar: Char get() = ('a' + col)
    val rankChar: Char get() = ('8' - row)
    val notation: String get() = "$fileChar$rankChar"

    val isLightSquare: Boolean get() = (row + col) % 2 == 0

    companion object {
        fun fromNotation(not: String): Square? {
            if (not.length != 2) return null
            val col = not[0] - 'a'
            val row = '8' - not[1]
            if (row in 0..7 && col in 0..7) {
                return Square(row, col)
            }
            return null
        }
    }
}

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isCapture: Boolean = false,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false,
    val capturedPiece: Piece? = null
)

data class MoveRecord(
    val moveNumber: Int,
    val piece: Piece,
    val from: Square,
    val to: Square,
    val capturedPiece: Piece? = null,
    val promotion: PieceType? = null,
    val isCastlingKingside: Boolean = false,
    val isCastlingQueenside: Boolean = false,
    val isEnPassant: Boolean = false,
    val isCheck: Boolean = false,
    val isCheckmate: Boolean = false,
    val algebraicNotation: String = ""
)

enum class GameStatus {
    NOT_STARTED,
    IN_PROGRESS,
    CHECKMATE,
    STALEMATE,
    DRAW_FIFTY_MOVES,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_BY_AGREEMENT,
    RESIGNATION,
    TIMEOUT
}

enum class TimeControl(val displayName: String, val initialMinutes: Int, val incrementSeconds: Int) {
    UNLIMITED("Casual (No Timer)", 0, 0),
    BULLET_1_0("Bullet 1 min", 1, 0),
    BULLET_2_1("Bullet 2|1", 2, 1),
    BLITZ_3_0("Blitz 3 min", 3, 0),
    BLITZ_3_2("Blitz 3|2", 3, 2),
    BLITZ_5_0("Blitz 5 min", 5, 0),
    RAPID_10_0("Rapid 10 min", 10, 0),
    RAPID_15_10("Rapid 15|10", 15, 10);

    val hasTimer: Boolean get() = initialMinutes > 0
}

enum class BoardTheme(val displayName: String, val lightHex: Long, val darkHex: Long, val borderHex: Long) {
    WALNUT("Tournament Walnut", 0xFFF0D9B5, 0xFFB58863, 0xFF5D4037),
    MIDNIGHT_SLATE("Midnight Slate", 0xFFCBD5E1, 0xFF475569, 0xFF1E293B),
    EMERALD("Emerald Master", 0xFFFFFFDD, 0xFF86A666, 0xFF2D5A27);
}

enum class FlipMode {
    FIXED_WHITE,
    AUTO_FLIP,
    FACE_TO_FACE
}
