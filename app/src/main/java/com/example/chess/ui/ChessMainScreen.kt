package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.data.MatchStatistics
import com.example.chess.model.FlipMode
import com.example.chess.model.GameStatus
import com.example.chess.model.PieceColor
import com.example.chess.model.TimeControl

@Composable
fun ChessMainScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val matchHistory by viewModel.matchHistoryFlow.collectAsState(initial = emptyList())
    val matchStats by viewModel.matchStatsFlow.collectAsState(initial = MatchStatistics())

    // If viewing Match History screen
    if (uiState.showMatchHistoryScreen) {
        MatchHistoryScreen(
            matches = matchHistory,
            statistics = matchStats,
            onBack = { viewModel.setShowMatchHistoryScreen(false) },
            onDeleteMatch = { id -> viewModel.deleteMatchRecord(id) },
            onClearAll = { viewModel.clearAllMatches() }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1218),
                        Color(0xFF151922),
                        Color(0xFF0B0D12)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Header Bar
            ChessTopBar(
                flipMode = uiState.flipMode,
                onToggleFlip = { viewModel.toggleBoardFlip() },
                onOpenHistorySheet = { viewModel.setShowMoveHistorySheet(true) },
                onOpenMatchHistory = { viewModel.setShowMatchHistoryScreen(true) },
                onOpenSettings = { viewModel.setShowSettingsDialog(true) }
            )

            // 2. Top Player Bar
            val topColor = if (uiState.isBoardFlipped) PieceColor.WHITE else PieceColor.BLACK
            val bottomColor = topColor.opposite()

            val topPlayerName = if (topColor == PieceColor.WHITE) uiState.whitePlayerName else uiState.blackPlayerName
            val topTime = if (topColor == PieceColor.WHITE) uiState.whiteTimeMillis else uiState.blackTimeMillis
            val topCaptured = if (topColor == PieceColor.WHITE) uiState.boardState.capturedByWhite else uiState.boardState.capturedByBlack
            val topAdvantage = if (topColor == PieceColor.WHITE) uiState.whiteAdvantage else -uiState.whiteAdvantage
            val isTopFaceToFace = uiState.flipMode == FlipMode.FACE_TO_FACE

            PlayerBarView(
                playerName = topPlayerName,
                playerColor = topColor,
                isActiveTurn = uiState.activeColor == topColor && uiState.gameStatus == GameStatus.IN_PROGRESS,
                timeMillis = topTime,
                timeControl = uiState.timeControl,
                capturedPieces = topCaptured,
                materialAdvantage = topAdvantage,
                isFaceToFaceRotated = isTopFaceToFace,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 3. Check Alert Banner
            AnimatedVisibility(
                visible = uiState.isCheck && uiState.gameStatus == GameStatus.IN_PROGRESS,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFB91C1C))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.activeColor.name} KING IS IN CHECK!",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // 4. Center: Interactive Chess Board with Start Game overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                ChessBoardView(
                    boardState = uiState.boardState,
                    boardTheme = uiState.boardTheme,
                    selectedSquare = uiState.selectedSquare,
                    legalMoves = uiState.legalMoves,
                    lastMove = uiState.lastMove,
                    isCheck = uiState.isCheck,
                    checkSquare = uiState.checkSquare,
                    isBoardFlipped = uiState.isBoardFlipped,
                    flipMode = uiState.flipMode,
                    highlightLegalMoves = uiState.highlightLegalMoves,
                    highlightLastMove = uiState.highlightLastMove,
                    onSquareClicked = { sq -> viewModel.onSquareClicked(sq) }
                )

                // Start Game 🎮 Card Overlay when game is NOT_STARTED
                if (!uiState.isGameStarted || uiState.gameStatus == GameStatus.NOT_STARTED) {
                    StartGameOverlay(
                        whitePlayerName = uiState.whitePlayerName,
                        blackPlayerName = uiState.blackPlayerName,
                        timeControl = uiState.timeControl,
                        onStartGame = { viewModel.startGame() },
                        onConfigureMatch = { viewModel.setShowNewGameDialog(true) }
                    )
                }
            }

            // 5. Bottom Player Bar
            val bottomPlayerName = if (bottomColor == PieceColor.WHITE) uiState.whitePlayerName else uiState.blackPlayerName
            val bottomTime = if (bottomColor == PieceColor.WHITE) uiState.whiteTimeMillis else uiState.blackTimeMillis
            val bottomCaptured = if (bottomColor == PieceColor.WHITE) uiState.boardState.capturedByWhite else uiState.boardState.capturedByBlack
            val bottomAdvantage = if (bottomColor == PieceColor.WHITE) uiState.whiteAdvantage else -uiState.whiteAdvantage

            PlayerBarView(
                playerName = bottomPlayerName,
                playerColor = bottomColor,
                isActiveTurn = uiState.activeColor == bottomColor && uiState.gameStatus == GameStatus.IN_PROGRESS,
                timeMillis = bottomTime,
                timeControl = uiState.timeControl,
                capturedPieces = bottomCaptured,
                materialAdvantage = bottomAdvantage,
                isFaceToFaceRotated = false,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 6. Bottom Controls Bar
            ChessBottomControls(
                isGameStarted = uiState.isGameStarted && uiState.gameStatus == GameStatus.IN_PROGRESS,
                canUndo = uiState.moveHistory.isNotEmpty() && uiState.gameStatus == GameStatus.IN_PROGRESS,
                onStartGame = { viewModel.startGame() },
                onUndo = { viewModel.undoLastMove() },
                onNewGame = { viewModel.setShowNewGameDialog(true) },
                onFlip = { viewModel.toggleBoardFlip() },
                onOpenHistory = { viewModel.setShowMoveHistorySheet(true) }
            )
        }

        // --- Modals & Overlays ---

        // Pawn Promotion Modal
        uiState.pendingPromotionMove?.let {
            PromotionDialog(
                color = uiState.activeColor,
                onSelectPiece = { pType -> viewModel.onPromotionSelected(pType) },
                onDismiss = { viewModel.cancelPromotion() }
            )
        }

        // Pass Turn Curtain Overlay (if enabled)
        if (uiState.passCurtainVisible) {
            val activeName = if (uiState.activeColor == PieceColor.WHITE) uiState.whitePlayerName else uiState.blackPlayerName
            PassTurnOverlay(
                activeColor = uiState.activeColor,
                activePlayerName = activeName,
                onReadyClicked = { viewModel.dismissPassCurtain() }
            )
        }

        // Game Over Dialog
        if (uiState.showGameOverDialog) {
            GameOverDialog(
                gameStatus = uiState.gameStatus,
                winner = uiState.winner,
                whitePlayerName = uiState.whitePlayerName,
                blackPlayerName = uiState.blackPlayerName,
                totalMoves = uiState.moveHistory.size,
                onNewGame = {
                    viewModel.setShowGameOverDialog(false)
                    viewModel.setShowNewGameDialog(true)
                },
                onViewHistory = {
                    viewModel.setShowGameOverDialog(false)
                    viewModel.setShowMatchHistoryScreen(true)
                },
                onDismiss = { viewModel.setShowGameOverDialog(false) }
            )
        }

        // New Game Dialog
        if (uiState.showNewGameDialog) {
            NewGameDialog(
                initialWhiteName = uiState.whitePlayerName,
                initialBlackName = uiState.blackPlayerName,
                initialTimeControl = uiState.timeControl,
                initialFlipMode = uiState.flipMode,
                initialTheme = uiState.boardTheme,
                onStartGame = { w, b, tc, mode, theme ->
                    viewModel.startNewGame(w, b, tc, mode, theme)
                },
                onDismiss = { viewModel.setShowNewGameDialog(false) }
            )
        }

        // Score Sheet Bottom Sheet
        if (uiState.showMoveHistorySheet) {
            MoveHistoryBottomSheet(
                moveHistory = uiState.moveHistory,
                onDismiss = { viewModel.setShowMoveHistorySheet(false) }
            )
        }

        // Settings Dialog
        if (uiState.showSettingsDialog) {
            SettingsDialog(
                soundEnabled = uiState.soundEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                highlightLegalMoves = uiState.highlightLegalMoves,
                highlightLastMove = uiState.highlightLastMove,
                passCurtainEnabled = uiState.passCurtainSettingEnabled,
                flipMode = uiState.flipMode,
                isGameInProgress = uiState.gameStatus == GameStatus.IN_PROGRESS,
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onToggleHighlightLegalMoves = { viewModel.toggleHighlightLegalMoves() },
                onToggleHighlightLastMove = { viewModel.toggleHighlightLastMove() },
                onTogglePassCurtain = { viewModel.togglePassCurtainSetting() },
                onSelectFlipMode = { m -> viewModel.setFlipMode(m) },
                onOfferDraw = {
                    viewModel.setShowSettingsDialog(false)
                    viewModel.confirmDraw()
                },
                onResign = {
                    viewModel.setShowSettingsDialog(false)
                    viewModel.confirmResignation()
                },
                onDismiss = { viewModel.setShowSettingsDialog(false) }
            )
        }
    }
}

@Composable
fun StartGameOverlay(
    whitePlayerName: String,
    blackPlayerName: String,
    timeControl: TimeControl,
    onStartGame: () -> Unit,
    onConfigureMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2141822)),
        modifier = modifier
            .fillMaxWidth(0.92f)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD4AF37), Color(0xFF78350F))),
                RoundedCornerShape(24.dp)
            )
            .testTag("start_game_card")
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Controller / Trophy badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0x66F59E0B),
                                Color(0x22F59E0B),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFFBBF24), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Two Player Chess",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Pass & Play on this phone",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Player matchup badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2430))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚪ $whitePlayerName",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF1F5F9)
                )
                Text(
                    text = "vs",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "⚫ $blackPlayerName",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time control info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = timeControl.displayName,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // START GAME 🎮 BUTTON
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    contentColor = Color(0xFF1E1B18)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .testTag("start_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF1E1B18),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Game 🎮",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1B18),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onConfigureMatch,
                modifier = Modifier.testTag("configure_match_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Configure Players & Time",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ChessTopBar(
    flipMode: FlipMode,
    onToggleFlip: () -> Unit,
    onOpenHistorySheet: () -> Unit,
    onOpenMatchHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App title with mini crown
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "♔",
                fontSize = 22.sp,
                color = Color(0xFFFBBF24)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Chess Duel",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = when (flipMode) {
                        FlipMode.AUTO_FLIP -> "Auto-Flip Mode"
                        FlipMode.FACE_TO_FACE -> "Table Mode"
                        FlipMode.FIXED_WHITE -> "Fixed View"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleFlip,
                modifier = Modifier.testTag("top_flip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ScreenRotation,
                    contentDescription = "Flip Board",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenHistorySheet,
                modifier = Modifier.testTag("top_moves_sheet_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = "Moves",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenMatchHistory,
                modifier = Modifier.testTag("top_match_records_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Match Records",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChessBottomControls(
    isGameStarted: Boolean,
    canUndo: Boolean,
    onStartGame: () -> Unit,
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    onFlip: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141822)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isGameStarted) {
                // Prominent Start Game Button in bottom bar as well!
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("bottom_start_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color(0xFF1E1B18),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Start Game 🎮",
                        color = Color(0xFF1E1B18),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // New Match setup
                TextButton(
                    onClick = onNewGame,
                    modifier = Modifier.testTag("bottom_new_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "New Match",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // Undo button
                TextButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.testTag("bottom_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (canUndo) Color(0xFFFBBF24) else Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Undo",
                        color = if (canUndo) Color(0xFFFBBF24) else Color(0xFF475569),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // New Game button
                TextButton(
                    onClick = onNewGame,
                    modifier = Modifier.testTag("bottom_new_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "New Match",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Flip perspective
            TextButton(
                onClick = onFlip,
                modifier = Modifier.testTag("bottom_flip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ScreenRotation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Flip",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Moves log
            TextButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("bottom_moves_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Moves",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
