package com.example.chess.model

data class BoardState(
    val squares: List<Piece?>, // 64 elements, index = row * 8 + col
    val activeColor: PieceColor = PieceColor.WHITE,
    val whiteCanCastleKingside: Boolean = true,
    val whiteCanCastleQueenside: Boolean = true,
    val blackCanCastleKingside: Boolean = true,
    val blackCanCastleQueenside: Boolean = true,
    val enPassantTarget: Square? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1,
    val capturedByWhite: List<Piece> = emptyList(),
    val capturedByBlack: List<Piece> = emptyList()
) {
    operator fun get(row: Int, col: Int): Piece? = squares[row * 8 + col]
    operator fun get(square: Square): Piece? = squares[square.row * 8 + square.col]

    fun withPiece(square: Square, piece: Piece?): BoardState {
        val newSquares = squares.toMutableList()
        newSquares[square.row * 8 + square.col] = piece
        return copy(squares = newSquares)
    }

    companion object {
        fun initial(): BoardState {
            val list = MutableList<Piece?>(64) { null }
            // Black major pieces (row 0)
            list[0] = Piece(PieceType.ROOK, PieceColor.BLACK)
            list[1] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
            list[2] = Piece(PieceType.BISHOP, PieceColor.BLACK)
            list[3] = Piece(PieceType.QUEEN, PieceColor.BLACK)
            list[4] = Piece(PieceType.KING, PieceColor.BLACK)
            list[5] = Piece(PieceType.BISHOP, PieceColor.BLACK)
            list[6] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
            list[7] = Piece(PieceType.ROOK, PieceColor.BLACK)
            // Black pawns (row 1)
            for (c in 0..7) list[8 + c] = Piece(PieceType.PAWN, PieceColor.BLACK)

            // White pawns (row 6)
            for (c in 0..7) list[48 + c] = Piece(PieceType.PAWN, PieceColor.WHITE)
            // White major pieces (row 7)
            list[56] = Piece(PieceType.ROOK, PieceColor.WHITE)
            list[57] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
            list[58] = Piece(PieceType.BISHOP, PieceColor.WHITE)
            list[59] = Piece(PieceType.QUEEN, PieceColor.WHITE)
            list[60] = Piece(PieceType.KING, PieceColor.WHITE)
            list[61] = Piece(PieceType.BISHOP, PieceColor.WHITE)
            list[62] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
            list[63] = Piece(PieceType.ROOK, PieceColor.WHITE)

            return BoardState(squares = list)
        }
    }
}

object ChessEngine {

    fun getLegalMoves(state: BoardState, fromSquare: Square? = null): List<Move> {
        val allLegal = mutableListOf<Move>()
        for (r in 0..7) {
            for (c in 0..7) {
                val sq = Square(r, c)
                val piece = state[sq] ?: continue
                if (piece.color != state.activeColor) continue
                if (fromSquare != null && sq != fromSquare) continue

                val pseudoMoves = generatePseudoLegalMoves(state, sq, piece)
                for (m in pseudoMoves) {
                    if (isMoveSafeForKing(state, m)) {
                        allLegal.add(m)
                    }
                }
            }
        }
        return allLegal
    }

    fun isKingInCheck(state: BoardState, color: PieceColor): Boolean {
        val kingSquare = findKing(state, color) ?: return false
        return isSquareAttacked(state, kingSquare, color.opposite())
    }

    fun findKing(state: BoardState, color: PieceColor): Square? {
        for (r in 0..7) {
            for (c in 0..7) {
                val p = state[r, c]
                if (p?.type == PieceType.KING && p.color == color) {
                    return Square(r, c)
                }
            }
        }
        return null
    }

    fun isSquareAttacked(state: BoardState, target: Square, byColor: PieceColor): Boolean {
        // 1. Knight attacks
        val knightOffsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        for ((dr, dc) in knightOffsets) {
            val nr = target.row + dr
            val nc = target.col + dc
            if (nr in 0..7 && nc in 0..7) {
                val p = state[nr, nc]
                if (p?.color == byColor && p.type == PieceType.KNIGHT) return true
            }
        }

        // 2. Pawn attacks
        val pawnPusherRow = if (byColor == PieceColor.WHITE) target.row + 1 else target.row - 1
        if (pawnPusherRow in 0..7) {
            for (dc in listOf(-1, 1)) {
                val pc = target.col + dc
                if (pc in 0..7) {
                    val p = state[pawnPusherRow, pc]
                    if (p?.color == byColor && p.type == PieceType.PAWN) return true
                }
            }
        }

        // 3. King adjacent attacks
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = target.row + dr
                val nc = target.col + dc
                if (nr in 0..7 && nc in 0..7) {
                    val p = state[nr, nc]
                    if (p?.color == byColor && p.type == PieceType.KING) return true
                }
            }
        }

        // 4. Straight ray attacks (Rook or Queen)
        val straightDirs = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
        for ((dr, dc) in straightDirs) {
            var currR = target.row + dr
            var currC = target.col + dc
            while (currR in 0..7 && currC in 0..7) {
                val p = state[currR, currC]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break // blocked
                }
                currR += dr
                currC += dc
            }
        }

        // 5. Diagonal ray attacks (Bishop or Queen)
        val diagDirs = listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)
        for ((dr, dc) in diagDirs) {
            var currR = target.row + dr
            var currC = target.col + dc
            while (currR in 0..7 && currC in 0..7) {
                val p = state[currR, currC]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break // blocked
                }
                currR += dr
                currC += dc
            }
        }

        return false
    }

    private fun generatePseudoLegalMoves(state: BoardState, from: Square, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        val color = piece.color
        val opponent = color.opposite()

        when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (color == PieceColor.WHITE) -1 else 1
                val startRow = if (color == PieceColor.WHITE) 6 else 1
                val promoRow = if (color == PieceColor.WHITE) 0 else 7

                // Single square advance
                val oneStepR = from.row + dir
                if (oneStepR in 0..7 && state[oneStepR, from.col] == null) {
                    val toSq = Square(oneStepR, from.col)
                    if (oneStepR == promoRow) {
                        for (pType in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                            moves.add(Move(from, toSq, promotion = pType))
                        }
                    } else {
                        moves.add(Move(from, toSq))
                    }

                    // Double square advance from starting row
                    val twoStepsR = from.row + 2 * dir
                    if (from.row == startRow && state[twoStepsR, from.col] == null) {
                        moves.add(Move(from, Square(twoStepsR, from.col)))
                    }
                }

                // Diagonal captures
                for (dc in listOf(-1, 1)) {
                    val toCol = from.col + dc
                    if (oneStepR in 0..7 && toCol in 0..7) {
                        val targetSq = Square(oneStepR, toCol)
                        val targetPiece = state[targetSq]
                        if (targetPiece?.color == opponent) {
                            if (oneStepR == promoRow) {
                                for (pType in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                                    moves.add(Move(from, targetSq, promotion = pType, isCapture = true, capturedPiece = targetPiece))
                                }
                            } else {
                                moves.add(Move(from, targetSq, isCapture = true, capturedPiece = targetPiece))
                            }
                        } else if (state.enPassantTarget == targetSq) {
                            // En passant capture
                            val capturedPawn = state[from.row, toCol]
                            moves.add(Move(from, targetSq, isCapture = true, isEnPassant = true, capturedPiece = capturedPawn))
                        }
                    }
                }
            }

            PieceType.KNIGHT -> {
                val offsets = listOf(
                    -2 to -1, -2 to 1, -1 to -2, -1 to 2,
                    1 to -2, 1 to 2, 2 to -1, 2 to 1
                )
                for ((dr, dc) in offsets) {
                    val nr = from.row + dr
                    val nc = from.col + dc
                    if (nr in 0..7 && nc in 0..7) {
                        val toSq = Square(nr, nc)
                        val target = state[toSq]
                        if (target == null) {
                            moves.add(Move(from, toSq))
                        } else if (target.color == opponent) {
                            moves.add(Move(from, toSq, isCapture = true, capturedPiece = target))
                        }
                    }
                }
            }

            PieceType.BISHOP -> addRayMoves(moves, state, from, color, listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1))
            PieceType.ROOK -> addRayMoves(moves, state, from, color, listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0))
            PieceType.QUEEN -> {
                addRayMoves(moves, state, from, color, listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1))
                addRayMoves(moves, state, from, color, listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0))
            }

            PieceType.KING -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val nr = from.row + dr
                        val nc = from.col + dc
                        if (nr in 0..7 && nc in 0..7) {
                            val toSq = Square(nr, nc)
                            val target = state[toSq]
                            if (target == null) {
                                moves.add(Move(from, toSq))
                            } else if (target.color == opponent) {
                                moves.add(Move(from, toSq, isCapture = true, capturedPiece = target))
                            }
                        }
                    }
                }

                // Castling
                if (!isKingInCheck(state, color)) {
                    val row = if (color == PieceColor.WHITE) 7 else 0
                    if (from.row == row && from.col == 4) {
                        // Kingside
                        val canKingside = if (color == PieceColor.WHITE) state.whiteCanCastleKingside else state.blackCanCastleKingside
                        if (canKingside) {
                            if (state[row, 5] == null && state[row, 6] == null) {
                                val rook = state[row, 7]
                                if (rook?.type == PieceType.ROOK && rook.color == color) {
                                    if (!isSquareAttacked(state, Square(row, 5), opponent) &&
                                        !isSquareAttacked(state, Square(row, 6), opponent)) {
                                        moves.add(Move(from, Square(row, 6), isCastling = true))
                                    }
                                }
                            }
                        }

                        // Queenside
                        val canQueenside = if (color == PieceColor.WHITE) state.whiteCanCastleQueenside else state.blackCanCastleQueenside
                        if (canQueenside) {
                            if (state[row, 1] == null && state[row, 2] == null && state[row, 3] == null) {
                                val rook = state[row, 0]
                                if (rook?.type == PieceType.ROOK && rook.color == color) {
                                    if (!isSquareAttacked(state, Square(row, 3), opponent) &&
                                        !isSquareAttacked(state, Square(row, 2), opponent)) {
                                        moves.add(Move(from, Square(row, 2), isCastling = true))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return moves
    }

    private fun addRayMoves(
        moves: MutableList<Move>,
        state: BoardState,
        from: Square,
        color: PieceColor,
        directions: List<Pair<Int, Int>>
    ) {
        val opponent = color.opposite()
        for ((dr, dc) in directions) {
            var r = from.row + dr
            var c = from.col + dc
            while (r in 0..7 && c in 0..7) {
                val toSq = Square(r, c)
                val target = state[toSq]
                if (target == null) {
                    moves.add(Move(from, toSq))
                } else {
                    if (target.color == opponent) {
                        moves.add(Move(from, toSq, isCapture = true, capturedPiece = target))
                    }
                    break
                }
                r += dr
                c += dc
            }
        }
    }

    private fun isMoveSafeForKing(state: BoardState, move: Move): Boolean {
        val nextState = applyMoveRaw(state, move)
        return !isKingInCheck(nextState, state.activeColor)
    }

    fun applyMoveRaw(state: BoardState, move: Move): BoardState {
        val movingPiece = state[move.from] ?: return state
        var newSquares = state.squares.toMutableList()

        // Place piece or promoted piece
        val pieceToPlace = if (move.promotion != null) {
            Piece(move.promotion, movingPiece.color)
        } else {
            movingPiece
        }
        newSquares[move.from.row * 8 + move.from.col] = null
        newSquares[move.to.row * 8 + move.to.col] = pieceToPlace

        // Castling rook move
        if (move.isCastling) {
            val r = move.from.row
            if (move.to.col == 6) { // Kingside
                val rook = newSquares[r * 8 + 7]
                newSquares[r * 8 + 7] = null
                newSquares[r * 8 + 5] = rook
            } else if (move.to.col == 2) { // Queenside
                val rook = newSquares[r * 8 + 0]
                newSquares[r * 8 + 0] = null
                newSquares[r * 8 + 3] = rook
            }
        }

        // En passant capture
        if (move.isEnPassant) {
            newSquares[move.from.row * 8 + move.to.col] = null
        }

        return state.copy(squares = newSquares)
    }

    fun makeMove(state: BoardState, move: Move): Pair<BoardState, MoveRecord> {
        val movingPiece = state[move.from]!!
        val captured = if (move.isEnPassant) {
            state[move.from.row, move.to.col]
        } else {
            state[move.to]
        }

        val rawApplied = applyMoveRaw(state, move)

        // Update castling rights
        var wCK = state.whiteCanCastleKingside
        var wCQ = state.whiteCanCastleQueenside
        var bCK = state.blackCanCastleKingside
        var bCQ = state.blackCanCastleQueenside

        if (movingPiece.type == PieceType.KING) {
            if (movingPiece.color == PieceColor.WHITE) {
                wCK = false
                wCQ = false
            } else {
                bCK = false
                bCQ = false
            }
        } else if (movingPiece.type == PieceType.ROOK) {
            if (movingPiece.color == PieceColor.WHITE) {
                if (move.from.row == 7 && move.from.col == 7) wCK = false
                if (move.from.row == 7 && move.from.col == 0) wCQ = false
            } else {
                if (move.from.row == 0 && move.from.col == 7) bCK = false
                if (move.from.row == 0 && move.from.col == 0) bCQ = false
            }
        }

        // If an enemy rook was captured in its corner, clear that castling right
        if (move.to.row == 7 && move.to.col == 7) wCK = false
        if (move.to.row == 7 && move.to.col == 0) wCQ = false
        if (move.to.row == 0 && move.to.col == 7) bCK = false
        if (move.to.row == 0 && move.to.col == 0) bCQ = false

        // En passant target for next move
        val nextEnPassant = if (movingPiece.type == PieceType.PAWN && Math.abs(move.to.row - move.from.row) == 2) {
            Square((move.from.row + move.to.row) / 2, move.from.col)
        } else {
            null
        }

        // Halfmove clock for 50-move rule
        val nextHalfmove = if (movingPiece.type == PieceType.PAWN || captured != null) 0 else state.halfmoveClock + 1
        val nextFullmove = if (state.activeColor == PieceColor.BLACK) state.fullmoveNumber + 1 else state.fullmoveNumber

        // Captured pieces update
        val whiteCaps = state.capturedByWhite.toMutableList()
        val blackCaps = state.capturedByBlack.toMutableList()
        if (captured != null) {
            if (state.activeColor == PieceColor.WHITE) {
                whiteCaps.add(captured)
            } else {
                blackCaps.add(captured)
            }
        }

        val nextColor = state.activeColor.opposite()
        val nextState = rawApplied.copy(
            activeColor = nextColor,
            whiteCanCastleKingside = wCK,
            whiteCanCastleQueenside = wCQ,
            blackCanCastleKingside = bCK,
            blackCanCastleQueenside = bCQ,
            enPassantTarget = nextEnPassant,
            halfmoveClock = nextHalfmove,
            fullmoveNumber = nextFullmove,
            capturedByWhite = whiteCaps,
            capturedByBlack = blackCaps
        )

        val isOpponentInCheck = isKingInCheck(nextState, nextColor)
        val opponentLegalMoves = getLegalMoves(nextState)
        val isOpponentCheckmate = isOpponentInCheck && opponentLegalMoves.isEmpty()

        val isKingsideCastling = move.isCastling && move.to.col == 6
        val isQueensideCastling = move.isCastling && move.to.col == 2

        val notation = formatAlgebraic(
            state = state,
            move = move,
            movingPiece = movingPiece,
            capturedPiece = captured,
            isCheck = isOpponentInCheck,
            isCheckmate = isOpponentCheckmate
        )

        val record = MoveRecord(
            moveNumber = state.fullmoveNumber,
            piece = movingPiece,
            from = move.from,
            to = move.to,
            capturedPiece = captured,
            promotion = move.promotion,
            isCastlingKingside = isKingsideCastling,
            isCastlingQueenside = isQueensideCastling,
            isEnPassant = move.isEnPassant,
            isCheck = isOpponentInCheck,
            isCheckmate = isOpponentCheckmate,
            algebraicNotation = notation
        )

        return Pair(nextState, record)
    }

    private fun formatAlgebraic(
        state: BoardState,
        move: Move,
        movingPiece: Piece,
        capturedPiece: Piece?,
        isCheck: Boolean,
        isCheckmate: Boolean
    ): String {
        if (move.isCastling) {
            val base = if (move.to.col == 6) "O-O" else "O-O-O"
            return if (isCheckmate) "$base#" else if (isCheck) "$base+" else base
        }

        val sb = StringBuilder()
        if (movingPiece.type == PieceType.PAWN) {
            if (capturedPiece != null || move.isEnPassant) {
                sb.append(move.from.fileChar)
                sb.append('x')
            }
            sb.append(move.to.notation)
            if (move.promotion != null) {
                sb.append('=').append(move.promotion.symbol)
            }
        } else {
            sb.append(movingPiece.type.symbol)
            // Disambiguation
            val duplicates = getLegalMoves(state).filter {
                it.to == move.to && it.from != move.from &&
                        state[it.from]?.type == movingPiece.type &&
                        state[it.from]?.color == movingPiece.color
            }
            if (duplicates.isNotEmpty()) {
                val sameFile = duplicates.any { it.from.col == move.from.col }
                val sameRank = duplicates.any { it.from.row == move.from.row }
                if (!sameFile) {
                    sb.append(move.from.fileChar)
                } else if (!sameRank) {
                    sb.append(move.from.rankChar)
                } else {
                    sb.append(move.from.notation)
                }
            }
            if (capturedPiece != null) {
                sb.append('x')
            }
            sb.append(move.to.notation)
        }

        if (isCheckmate) {
            sb.append('#')
        } else if (isCheck) {
            sb.append('+')
        }

        return sb.toString()
    }

    fun isInsufficientMaterial(state: BoardState): Boolean {
        val nonKings = state.squares.filterNotNull().filter { it.type != PieceType.KING }
        if (nonKings.isEmpty()) return true // King vs King

        if (nonKings.size == 1) {
            val p = nonKings[0]
            // King and Bishop vs King or King and Knight vs King
            if (p.type == PieceType.BISHOP || p.type == PieceType.KNIGHT) return true
        }

        if (nonKings.size == 2) {
            val p1 = nonKings[0]
            val p2 = nonKings[1]
            if (p1.color != p2.color && p1.type == PieceType.BISHOP && p2.type == PieceType.BISHOP) {
                // If both are bishops on the same square color
                val b1Sq = findPieceSquare(state, p1)
                val b2Sq = findPieceSquare(state, p2)
                if (b1Sq != null && b2Sq != null && b1Sq.isLightSquare == b2Sq.isLightSquare) {
                    return true
                }
            }
        }

        return false
    }

    private fun findPieceSquare(state: BoardState, piece: Piece): Square? {
        for (r in 0..7) {
            for (c in 0..7) {
                if (state[r, c] == piece) return Square(r, c)
            }
        }
        return null
    }

    fun calculateMaterialAdvantage(state: BoardState): Pair<Int, Int> {
        var whiteVal = 0
        var blackVal = 0
        for (p in state.squares.filterNotNull()) {
            if (p.color == PieceColor.WHITE) {
                whiteVal += p.type.baseValue
            } else {
                blackVal += p.type.baseValue
            }
        }
        return Pair(whiteVal, blackVal)
    }
}
