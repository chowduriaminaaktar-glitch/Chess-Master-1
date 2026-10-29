package com.example.chess.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.BoardState
import com.example.chess.model.BoardTheme
import com.example.chess.model.FlipMode
import com.example.chess.model.Move
import com.example.chess.model.MoveRecord
import com.example.chess.model.PieceColor
import com.example.chess.model.Square

@Composable
fun ChessBoardView(
    boardState: BoardState,
    boardTheme: BoardTheme,
    selectedSquare: Square?,
    legalMoves: List<Move>,
    lastMove: MoveRecord?,
    isCheck: Boolean,
    checkSquare: Square?,
    isBoardFlipped: Boolean,
    flipMode: FlipMode,
    highlightLegalMoves: Boolean,
    highlightLastMove: Boolean,
    onSquareClicked: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    val lightSquareColor = Color(boardTheme.lightHex)
    val darkSquareColor = Color(boardTheme.darkHex)
    val borderColor = Color(boardTheme.borderHex)

    val infiniteTransition = rememberInfiniteTransition(label = "check_pulse")
    val checkPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Outer frame with realistic shadow and bevel
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.88f)
                    )
                )
            )
            .padding(6.dp)
            .testTag("chess_board_frame")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
        ) {
            // Rows: if board is flipped (Black perspective at bottom), row index goes 0..7
            // If normal (White perspective at bottom), row index goes 7 down to 0
            val rowIndices = if (isBoardFlipped) (0..7).toList() else (7 downTo 0).toList()
            val colIndices = if (isBoardFlipped) (7 downTo 0).toList() else (0..7).toList()

            for (r in rowIndices) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in colIndices) {
                        val currentSquare = Square(r, c)
                        val isLight = currentSquare.isLightSquare
                        val squareBg = if (isLight) lightSquareColor else darkSquareColor

                        val isSelected = selectedSquare == currentSquare
                        val isLegalDest = highlightLegalMoves && legalMoves.any { it.to == currentSquare }
                        val isCaptureDest = isLegalDest && legalMoves.any { it.to == currentSquare && (it.isCapture || it.isEnPassant) }
                        val isLastMoveSquare = highlightLastMove && (lastMove?.from == currentSquare || lastMove?.to == currentSquare)
                        val isInCheckKingSquare = isCheck && checkSquare == currentSquare

                        val piece = boardState[currentSquare]

                        // In Face-to-Face mode, rotate Black pieces (or top pieces) by 180°
                        val isPieceRotated = when (flipMode) {
                            FlipMode.FACE_TO_FACE -> {
                                if (isBoardFlipped) piece?.color == PieceColor.WHITE else piece?.color == PieceColor.BLACK
                            }
                            else -> false
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(squareBg)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onSquareClicked(currentSquare)
                                }
                                .testTag("square_${currentSquare.notation}"),
                            contentAlignment = Alignment.Center
                        ) {
                            // 1. Highlight last move
                            if (isLastMoveSquare) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0x66F59E0B))
                                )
                            }

                            // 2. Highlight selected square
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0x99FBBF24))
                                )
                            }

                            // 3. Highlight King in check
                            if (isInCheckKingSquare) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    Color(0xFFFF1744).copy(alpha = checkPulseAlpha),
                                                    Color(0xFFD50000).copy(alpha = checkPulseAlpha * 0.7f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            // Coordinate label for bottom-left / rank & file indicators
                            val showRankLabel = c == colIndices.first()
                            val showFileLabel = r == rowIndices.last()

                            if (showRankLabel) {
                                Text(
                                    text = currentSquare.rankChar.toString(),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 2.dp, top = 1.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) darkSquareColor.copy(alpha = 0.85f) else lightSquareColor.copy(alpha = 0.85f)
                                )
                            }
                            if (showFileLabel) {
                                Text(
                                    text = currentSquare.fileChar.toString(),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 2.dp, bottom = 1.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) darkSquareColor.copy(alpha = 0.85f) else lightSquareColor.copy(alpha = 0.85f)
                                )
                            }

                            // 4. Render piece
                            if (piece != null) {
                                ChessPieceIcon(
                                    piece = piece,
                                    modifier = Modifier.fillMaxSize(),
                                    isRotated = isPieceRotated
                                )
                            }

                            // 5. Destination marker
                            if (isLegalDest) {
                                if (isCaptureDest) {
                                    // Circular target ring for captures
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.85f)
                                            .border(3.dp, Color(0xCCE11D48), CircleShape)
                                    )
                                } else {
                                    // Soft indicator dot for quiet moves
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isLight) Color(0x661E293B) else Color(0x88FFFFFF)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
