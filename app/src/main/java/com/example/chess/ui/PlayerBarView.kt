package com.example.chess.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.TimeControl

@Composable
fun PlayerBarView(
    playerName: String,
    playerColor: PieceColor,
    isActiveTurn: Boolean,
    timeMillis: Long,
    timeControl: TimeControl,
    capturedPieces: List<Piece>,
    materialAdvantage: Int, // e.g. +3 or 0
    isFaceToFaceRotated: Boolean,
    modifier: Modifier = Modifier
) {
    val isWhite = playerColor == PieceColor.WHITE

    val cardBg by animateColorAsState(
        targetValue = if (isActiveTurn) {
            if (isWhite) Color(0xFF2C241D) else Color(0xFF1E242B)
        } else {
            Color(0xFF14171C)
        },
        animationSpec = tween(300),
        label = "player_card_bg"
    )

    val activeBorderColor by animateColorAsState(
        targetValue = if (isActiveTurn) {
            if (isWhite) Color(0xFFF59E0B) else Color(0xFF38BDF8)
        } else {
            Color(0x22FFFFFF)
        },
        animationSpec = tween(300),
        label = "player_border"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                if (isFaceToFaceRotated) {
                    rotationZ = 180f
                }
            }
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isActiveTurn) 2.dp else 1.dp,
                color = activeBorderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("player_bar_${playerColor.name.lowercase()}"),
        color = cardBg,
        tonalElevation = if (isActiveTurn) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player info: avatar + name + status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color Badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isWhite) Color(0xFFF3F4F6) else Color(0xFF111827))
                        .border(
                            1.5.dp,
                            if (isWhite) Color(0xFFD4AF37) else Color(0xFF64748B),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isWhite) "♔" else "♚",
                        fontSize = 18.sp,
                        color = if (isWhite) Color(0xFF1F2937) else Color(0xFFF9FAFB)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = playerName,
                            fontWeight = if (isActiveTurn) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (isActiveTurn) Color.White else Color(0xFF94A3B8)
                        )
                        if (materialAdvantage > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33F59E0B))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "+$materialAdvantage",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24)
                                )
                            }
                        }
                    }

                    // Captured pieces row
                    if (capturedPieces.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        CapturedPiecesTray(capturedPieces = capturedPieces)
                    }
                }
            }

            // Digital Chess Clock
            if (timeControl.hasTimer) {
                ChessClockDigital(
                    timeMillis = timeMillis,
                    isActive = isActiveTurn
                )
            } else {
                // Turn Indicator chip for casual games
                if (isActiveTurn) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isWhite) Color(0x33F59E0B) else Color(0x3338BDF8))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TURN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWhite) Color(0xFFFBBF24) else Color(0xFF38BDF8),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChessClockDigital(
    timeMillis: Long,
    isActive: Boolean
) {
    val totalSeconds = (timeMillis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val tenths = (timeMillis % 1000) / 100

    val isLowTime = totalSeconds < 30
    val isCritical = totalSeconds < 10

    val clockBg = when {
        isCritical && isActive -> Color(0xFF7F1D1D)
        isLowTime && isActive -> Color(0xFF78350F)
        isActive -> Color(0xFF1E293B)
        else -> Color(0xFF0F172A)
    }

    val textColor = when {
        isCritical -> Color(0xFFF87171)
        isLowTime -> Color(0xFFFBBF24)
        isActive -> Color(0xFFF8FAFC)
        else -> Color(0xFF64748B)
    }

    val timeText = if (minutes == 0L && totalSeconds < 20) {
        String.format("%02d.%d", seconds, tenths)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(clockBg)
            .border(
                width = 1.dp,
                color = if (isActive) textColor.copy(alpha = 0.5f) else Color(0x22FFFFFF),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = textColor
        )
    }
}

@Composable
fun CapturedPiecesTray(capturedPieces: List<Piece>) {
    // Group captured pieces by type in standard chess order: Q, R, B, N, P
    val order = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT, PieceType.PAWN)
    val sorted = capturedPieces.sortedBy { order.indexOf(it.type) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        for (piece in sorted.take(12)) {
            Text(
                text = piece.unicodeSymbol,
                fontSize = 13.sp,
                color = if (piece.color == PieceColor.WHITE) Color(0xFFE2E8F0) else Color(0xFF94A3B8)
            )
        }
        if (sorted.size > 12) {
            Text(
                text = "+${sorted.size - 12}",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
