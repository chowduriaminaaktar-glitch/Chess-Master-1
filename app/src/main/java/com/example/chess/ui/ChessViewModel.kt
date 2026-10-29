package com.example.chess.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.audio.SoundHapticsManager
import com.example.chess.data.ChessDatabase
import com.example.chess.data.MatchRecord
import com.example.chess.data.MatchRepository
import com.example.chess.model.BoardState
import com.example.chess.model.BoardTheme
import com.example.chess.model.ChessEngine
import com.example.chess.model.FlipMode
import com.example.chess.model.GameStatus
import com.example.chess.model.Move
import com.example.chess.model.MoveRecord
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import com.example.chess.model.TimeControl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ChessUiState(
    val boardState: BoardState = BoardState.initial(),
    val gameStatus: GameStatus = GameStatus.NOT_STARTED,
    val isGameStarted: Boolean = false,
    val winner: PieceColor? = null,
    val selectedSquare: Square? = null,
    val legalMoves: List<Move> = emptyList(),
    val lastMove: MoveRecord? = null,
    val moveHistory: List<MoveRecord> = emptyList(),
    val isCheck: Boolean = false,
    val checkSquare: Square? = null,
    val pendingPromotionMove: Move? = null,

    // Players & Timing
    val whitePlayerName: String = "Player 1",
    val blackPlayerName: String = "Player 2",
    val timeControl: TimeControl = TimeControl.RAPID_10_0,
    val whiteTimeMillis: Long = 10 * 60 * 1000L,
    val blackTimeMillis: Long = 10 * 60 * 1000L,
    val isTimerRunning: Boolean = false,

    // One-Mobile Pass & Play Options
    val flipMode: FlipMode = FlipMode.AUTO_FLIP,
    val isBoardFlipped: Boolean = false, // true = Black perspective at bottom
    val passCurtainVisible: Boolean = false,
    val passCurtainSettingEnabled: Boolean = false,

    // UI Customization & Toggles
    val boardTheme: BoardTheme = BoardTheme.WALNUT,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val highlightLegalMoves: Boolean = true,
    val highlightLastMove: Boolean = true,

    // Material count
    val whiteMaterialScore: Int = 39,
    val blackMaterialScore: Int = 39,

    // Navigation and Modals
    val showNewGameDialog: Boolean = false,
    val showGameOverDialog: Boolean = false,
    val showResignConfirmDialog: Boolean = false,
    val showDrawConfirmDialog: Boolean = false,
    val showMoveHistorySheet: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showMatchHistoryScreen: Boolean = false
) {
    val activeColor: PieceColor get() = boardState.activeColor
    val whiteAdvantage: Int get() = whiteMaterialScore - blackMaterialScore
}

class ChessViewModel(application: Application) : AndroidViewModel(application) {

    private val matchRepository: MatchRepository
    val soundManager: SoundHapticsManager = SoundHapticsManager(application)

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    val matchHistoryFlow get() = matchRepository.allMatches
    val matchStatsFlow get() = matchRepository.matchStatistics

    private var timerJob: Job? = null
    private var gameStartTimeMillis: Long = System.currentTimeMillis()

    init {
        val db = ChessDatabase.getInstance(application)
        matchRepository = MatchRepository(db.matchDao())
        updateScoresAndCheckStatus()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.release()
    }

    private fun startTimer() {
        timerJob?.cancel()
        if (!_uiState.value.timeControl.hasTimer) return

        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            var lastTickTime = System.currentTimeMillis()
            while (isActive && _uiState.value.gameStatus == GameStatus.IN_PROGRESS) {
                delay(100)
                val now = System.currentTimeMillis()
                val delta = now - lastTickTime
                lastTickTime = now

                val current = _uiState.value
                if (!current.isTimerRunning) continue

                if (current.activeColor == PieceColor.WHITE) {
                    val remaining = (current.whiteTimeMillis - delta).coerceAtLeast(0L)
                    if (remaining <= 0L) {
                        onTimeout(PieceColor.WHITE)
                        break
                    }
                    _uiState.update { it.copy(whiteTimeMillis = remaining) }
                } else {
                    val remaining = (current.blackTimeMillis - delta).coerceAtLeast(0L)
                    if (remaining <= 0L) {
                        onTimeout(PieceColor.BLACK)
                        break
                    }
                    _uiState.update { it.copy(blackTimeMillis = remaining) }
                }
            }
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    private fun onTimeout(timedOutColor: PieceColor) {
        val winner = timedOutColor.opposite()
        soundManager.playCheckmateSound(_uiState.value.soundEnabled)
        soundManager.vibrateCheck(_uiState.value.hapticsEnabled)

        _uiState.update {
            it.copy(
                gameStatus = GameStatus.TIMEOUT,
                winner = winner,
                isTimerRunning = false,
                showGameOverDialog = true
            )
        }
        saveCurrentMatchRecord(winner = winner.name, reason = "TIMEOUT")
    }

    fun startGame() {
        if (_uiState.value.isGameStarted && _uiState.value.gameStatus == GameStatus.IN_PROGRESS) return

        soundManager.playGameStartSound(_uiState.value.soundEnabled)
        soundManager.vibrateGameStart(_uiState.value.hapticsEnabled)
        gameStartTimeMillis = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                gameStatus = GameStatus.IN_PROGRESS,
                isGameStarted = true
            )
        }

        if (_uiState.value.timeControl.hasTimer) {
            startTimer()
        }
    }

    fun onSquareClicked(square: Square) {
        val state = _uiState.value
        if (state.passCurtainVisible) return
        if (state.gameStatus != GameStatus.IN_PROGRESS && state.gameStatus != GameStatus.NOT_STARTED) return

        // Auto-start game if tapping on active player's piece while in NOT_STARTED state
        if (!state.isGameStarted || state.gameStatus == GameStatus.NOT_STARTED) {
            val piece = state.boardState[square]
            if (piece != null && piece.color == state.activeColor) {
                startGame()
                val legalMoves = ChessEngine.getLegalMoves(_uiState.value.boardState, square)
                _uiState.update {
                    it.copy(
                        selectedSquare = square,
                        legalMoves = legalMoves
                    )
                }
            }
            return
        }

        val selected = state.selectedSquare

        // 1. If no square selected yet, select if it contains player's piece
        if (selected == null) {
            val piece = state.boardState[square]
            if (piece != null && piece.color == state.activeColor) {
                val legalMoves = ChessEngine.getLegalMoves(state.boardState, square)
                _uiState.update {
                    it.copy(
                        selectedSquare = square,
                        legalMoves = legalMoves
                    )
                }
            }
            return
        }

        // 2. If clicking on the same selected square, unselect
        if (selected == square) {
            _uiState.update { it.copy(selectedSquare = null, legalMoves = emptyList()) }
            return
        }

        // 3. If clicking on another piece of own color, switch selection
        val pieceAtTarget = state.boardState[square]
        if (pieceAtTarget != null && pieceAtTarget.color == state.activeColor) {
            val legalMoves = ChessEngine.getLegalMoves(state.boardState, square)
            _uiState.update {
                it.copy(
                    selectedSquare = square,
                    legalMoves = legalMoves
                )
            }
            return
        }

        // 4. Try making move to this target square
        val matchingMoves = state.legalMoves.filter { it.to == square }
        if (matchingMoves.isEmpty()) {
            // Not a legal destination, unselect
            _uiState.update { it.copy(selectedSquare = null, legalMoves = emptyList()) }
            return
        }

        // Check if move is a pawn promotion (multiple matching promotion variants)
        val isPromotion = matchingMoves.any { it.promotion != null }
        if (isPromotion) {
            _uiState.update {
                it.copy(
                    pendingPromotionMove = matchingMoves.first(),
                    selectedSquare = null,
                    legalMoves = emptyList()
                )
            }
            return
        }

        // Normal move
        executeMove(matchingMoves.first())
    }

    fun onPromotionSelected(promotionType: PieceType) {
        val pending = _uiState.value.pendingPromotionMove ?: return
        val finalMove = pending.copy(promotion = promotionType)
        _uiState.update { it.copy(pendingPromotionMove = null) }
        executeMove(finalMove)
    }

    fun cancelPromotion() {
        _uiState.update { it.copy(pendingPromotionMove = null) }
    }

    private fun executeMove(move: Move) {
        val state = _uiState.value
        val (nextBoard, record) = ChessEngine.makeMove(state.boardState, move)
        val newHistory = state.moveHistory + record

        // Add increment to current player if configured
        val incrementMillis = state.timeControl.incrementSeconds * 1000L
        val updatedWhiteTime = if (state.activeColor == PieceColor.WHITE) {
            state.whiteTimeMillis + incrementMillis
        } else state.whiteTimeMillis

        val updatedBlackTime = if (state.activeColor == PieceColor.BLACK) {
            state.blackTimeMillis + incrementMillis
        } else state.blackTimeMillis

        // Sounds & Haptics
        if (record.isCheckmate) {
            soundManager.playCheckmateSound(state.soundEnabled)
            soundManager.vibrateCheck(state.hapticsEnabled)
        } else if (record.isCheck) {
            soundManager.playCheckSound(state.soundEnabled)
            soundManager.vibrateCheck(state.hapticsEnabled)
        } else if (record.capturedPiece != null || record.isEnPassant) {
            soundManager.playCaptureSound(state.soundEnabled)
            soundManager.vibrateCapture(state.hapticsEnabled)
        } else {
            soundManager.playMoveSound(state.soundEnabled)
            soundManager.vibrateMove(state.hapticsEnabled)
        }

        // Check game termination conditions
        val nextLegalMoves = ChessEngine.getLegalMoves(nextBoard)
        var status = GameStatus.IN_PROGRESS
        var winner: PieceColor? = null

        if (record.isCheckmate) {
            status = GameStatus.CHECKMATE
            winner = state.activeColor
        } else if (nextLegalMoves.isEmpty()) {
            status = GameStatus.STALEMATE
        } else if (nextBoard.halfmoveClock >= 100) {
            status = GameStatus.DRAW_FIFTY_MOVES
        } else if (ChessEngine.isInsufficientMaterial(nextBoard)) {
            status = GameStatus.DRAW_INSUFFICIENT_MATERIAL
        }

        // Calculate board flip
        val shouldFlip = when (state.flipMode) {
            FlipMode.AUTO_FLIP -> nextBoard.activeColor == PieceColor.BLACK
            FlipMode.FIXED_WHITE -> false
            FlipMode.FACE_TO_FACE -> false
        }

        val showCurtain = state.passCurtainSettingEnabled && status == GameStatus.IN_PROGRESS

        _uiState.update {
            it.copy(
                boardState = nextBoard,
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMove = record,
                moveHistory = newHistory,
                whiteTimeMillis = updatedWhiteTime,
                blackTimeMillis = updatedBlackTime,
                gameStatus = status,
                winner = winner,
                isBoardFlipped = shouldFlip,
                passCurtainVisible = showCurtain,
                showGameOverDialog = (status != GameStatus.IN_PROGRESS)
            )
        }

        updateScoresAndCheckStatus()

        // Start timer if not running already
        if (!state.isTimerRunning && state.timeControl.hasTimer && status == GameStatus.IN_PROGRESS) {
            startTimer()
        }

        if (status != GameStatus.IN_PROGRESS) {
            pauseTimer()
            val winReasonStr = when (status) {
                GameStatus.CHECKMATE -> "CHECKMATE"
                GameStatus.STALEMATE -> "STALEMATE"
                GameStatus.DRAW_FIFTY_MOVES -> "50_MOVES"
                GameStatus.DRAW_INSUFFICIENT_MATERIAL -> "INSUFFICIENT_MATERIAL"
                else -> status.name
            }
            saveCurrentMatchRecord(winner = winner?.name ?: "DRAW", reason = winReasonStr)
        }
    }

    private fun updateScoresAndCheckStatus() {
        val currState = _uiState.value.boardState
        val (wScore, bScore) = ChessEngine.calculateMaterialAdvantage(currState)
        val inCheck = ChessEngine.isKingInCheck(currState, currState.activeColor)
        val kingSq = if (inCheck) ChessEngine.findKing(currState, currState.activeColor) else null

        _uiState.update {
            it.copy(
                whiteMaterialScore = wScore,
                blackMaterialScore = bScore,
                isCheck = inCheck,
                checkSquare = kingSq
            )
        }
    }

    fun dismissPassCurtain() {
        _uiState.update { it.copy(passCurtainVisible = false) }
    }

    fun toggleBoardFlip() {
        _uiState.update { it.copy(isBoardFlipped = !it.isBoardFlipped) }
    }

    fun setFlipMode(mode: FlipMode) {
        _uiState.update {
            it.copy(
                flipMode = mode,
                isBoardFlipped = when (mode) {
                    FlipMode.AUTO_FLIP -> it.activeColor == PieceColor.BLACK
                    FlipMode.FIXED_WHITE -> false
                    FlipMode.FACE_TO_FACE -> false
                }
            )
        }
    }

    fun setBoardTheme(theme: BoardTheme) {
        _uiState.update { it.copy(boardTheme = theme) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleHaptics() {
        _uiState.update { it.copy(hapticsEnabled = !it.hapticsEnabled) }
    }

    fun togglePassCurtainSetting() {
        _uiState.update { it.copy(passCurtainSettingEnabled = !it.passCurtainSettingEnabled) }
    }

    fun toggleHighlightLegalMoves() {
        _uiState.update { it.copy(highlightLegalMoves = !it.highlightLegalMoves) }
    }

    fun toggleHighlightLastMove() {
        _uiState.update { it.copy(highlightLastMove = !it.highlightLastMove) }
    }

    fun undoLastMove() {
        val state = _uiState.value
        if (state.moveHistory.isEmpty() || state.gameStatus != GameStatus.IN_PROGRESS) return

        // Reconstruct game from scratch up to N-1
        var replayBoard = BoardState.initial()
        val historyToReplay = state.moveHistory.dropLast(1)
        var lastRecord: MoveRecord? = null

        for (rec in historyToReplay) {
            val move = Move(
                from = rec.from,
                to = rec.to,
                promotion = rec.promotion,
                isCapture = rec.capturedPiece != null,
                isCastling = rec.isCastlingKingside || rec.isCastlingQueenside,
                isEnPassant = rec.isEnPassant,
                capturedPiece = rec.capturedPiece
            )
            val (next, r) = ChessEngine.makeMove(replayBoard, move)
            replayBoard = next
            lastRecord = r
        }

        _uiState.update {
            it.copy(
                boardState = replayBoard,
                moveHistory = historyToReplay,
                lastMove = lastRecord,
                selectedSquare = null,
                legalMoves = emptyList(),
                isBoardFlipped = when (it.flipMode) {
                    FlipMode.AUTO_FLIP -> replayBoard.activeColor == PieceColor.BLACK
                    else -> it.isBoardFlipped
                }
            )
        }
        updateScoresAndCheckStatus()
    }

    fun confirmResignation() {
        val state = _uiState.value
        val resigningColor = state.activeColor
        val winner = resigningColor.opposite()

        _uiState.update {
            it.copy(
                gameStatus = GameStatus.RESIGNATION,
                winner = winner,
                isTimerRunning = false,
                showResignConfirmDialog = false,
                showGameOverDialog = true
            )
        }
        pauseTimer()
        saveCurrentMatchRecord(winner = winner.name, reason = "RESIGNATION")
    }

    fun confirmDraw() {
        _uiState.update {
            it.copy(
                gameStatus = GameStatus.DRAW_BY_AGREEMENT,
                winner = null,
                isTimerRunning = false,
                showDrawConfirmDialog = false,
                showGameOverDialog = true
            )
        }
        pauseTimer()
        saveCurrentMatchRecord(winner = "DRAW", reason = "DRAW_BY_AGREEMENT")
    }

    fun startNewGame(
        whiteName: String = _uiState.value.whitePlayerName,
        blackName: String = _uiState.value.blackPlayerName,
        timeControl: TimeControl = _uiState.value.timeControl,
        flipMode: FlipMode = _uiState.value.flipMode,
        theme: BoardTheme = _uiState.value.boardTheme
    ) {
        timerJob?.cancel()
        gameStartTimeMillis = System.currentTimeMillis()
        val initialTimeMillis = (timeControl.initialMinutes * 60 * 1000L).coerceAtLeast(0L)

        _uiState.update {
            it.copy(
                boardState = BoardState.initial(),
                gameStatus = GameStatus.NOT_STARTED,
                isGameStarted = false,
                winner = null,
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMove = null,
                moveHistory = emptyList(),
                isCheck = false,
                checkSquare = null,
                pendingPromotionMove = null,
                whitePlayerName = whiteName.ifBlank { "Player 1" },
                blackPlayerName = blackName.ifBlank { "Player 2" },
                timeControl = timeControl,
                whiteTimeMillis = initialTimeMillis,
                blackTimeMillis = initialTimeMillis,
                isTimerRunning = false,
                flipMode = flipMode,
                isBoardFlipped = false,
                passCurtainVisible = false,
                boardTheme = theme,
                showNewGameDialog = false,
                showGameOverDialog = false,
                showResignConfirmDialog = false,
                showDrawConfirmDialog = false
            )
        }
        updateScoresAndCheckStatus()
    }

    private fun saveCurrentMatchRecord(winner: String, reason: String) {
        val state = _uiState.value
        val durationSec = ((System.currentTimeMillis() - gameStartTimeMillis) / 1000L).coerceAtLeast(0L)

        // Build PGN moves string
        val sb = StringBuilder()
        for ((index, rec) in state.moveHistory.withIndex()) {
            if (index % 2 == 0) {
                sb.append("${(index / 2) + 1}. ")
            }
            sb.append(rec.algebraicNotation).append(" ")
        }

        val record = MatchRecord(
            playedAt = System.currentTimeMillis(),
            whitePlayer = state.whitePlayerName,
            blackPlayer = state.blackPlayerName,
            winner = winner,
            winReason = reason,
            timeControlName = state.timeControl.displayName,
            totalMoves = state.moveHistory.size,
            durationSeconds = durationSec,
            pgnMoves = sb.toString().trim()
        )

        viewModelScope.launch {
            matchRepository.saveMatch(record)
        }
    }

    fun deleteMatchRecord(id: Long) {
        viewModelScope.launch {
            matchRepository.deleteMatch(id)
        }
    }

    fun clearAllMatches() {
        viewModelScope.launch {
            matchRepository.clearHistory()
        }
    }

    fun setShowNewGameDialog(show: Boolean) = _uiState.update { it.copy(showNewGameDialog = show) }
    fun setShowGameOverDialog(show: Boolean) = _uiState.update { it.copy(showGameOverDialog = show) }
    fun setShowResignConfirm(show: Boolean) = _uiState.update { it.copy(showResignConfirmDialog = show) }
    fun setShowDrawConfirm(show: Boolean) = _uiState.update { it.copy(showDrawConfirmDialog = show) }
    fun setShowMoveHistorySheet(show: Boolean) = _uiState.update { it.copy(showMoveHistorySheet = show) }
    fun setShowSettingsDialog(show: Boolean) = _uiState.update { it.copy(showSettingsDialog = show) }
    fun setShowMatchHistoryScreen(show: Boolean) = _uiState.update { it.copy(showMatchHistoryScreen = show) }
}
