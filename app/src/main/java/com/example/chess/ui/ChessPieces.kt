package com.example.chess.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType

@Composable
fun ChessPieceIcon(
    piece: Piece,
    modifier: Modifier = Modifier,
    isRotated: Boolean = false
) {
    Box(
        modifier = modifier.graphicsLayer {
            if (isRotated) {
                rotationZ = 180f
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.85f)) {
            val isWhite = piece.color == PieceColor.WHITE
            val fillColor = if (isWhite) Color(0xFFFBFBFB) else Color(0xFF1E2229)
            val strokeColor = if (isWhite) Color(0xFF2C2523) else Color(0xFFCBD5E1)
            val accentColor = if (isWhite) Color(0xFFD4AF37) else Color(0xFF64748B)

            when (piece.type) {
                PieceType.PAWN -> drawPawn(fillColor, strokeColor, accentColor)
                PieceType.KNIGHT -> drawKnight(fillColor, strokeColor, accentColor, isWhite)
                PieceType.BISHOP -> drawBishop(fillColor, strokeColor, accentColor)
                PieceType.ROOK -> drawRook(fillColor, strokeColor, accentColor)
                PieceType.QUEEN -> drawQueen(fillColor, strokeColor, accentColor)
                PieceType.KING -> drawKing(fillColor, strokeColor, accentColor)
            }
        }
    }
}

private fun DrawScope.drawPawn(fill: Color, stroke: Color, accent: Color) {
    val w = size.width
    val h = size.height
    val sw = (w * 0.045f).coerceAtLeast(1.5f)

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.20f, h * 0.90f)
        lineTo(w * 0.80f, h * 0.90f)
        lineTo(w * 0.74f, h * 0.78f)
        lineTo(w * 0.26f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.32f, h * 0.78f)
        quadraticBezierTo(w * 0.38f, h * 0.58f, w * 0.40f, h * 0.48f)
        lineTo(w * 0.60f, h * 0.48f)
        quadraticBezierTo(w * 0.62f, h * 0.58f, w * 0.68f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill, style = Fill)
    drawPath(bodyPath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Collar
    val collarPath = Path().apply {
        moveTo(w * 0.35f, h * 0.48f)
        lineTo(w * 0.65f, h * 0.48f)
        lineTo(w * 0.62f, h * 0.42f)
        lineTo(w * 0.38f, h * 0.42f)
        close()
    }
    drawPath(collarPath, accent, style = Fill)
    drawPath(collarPath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Head circle
    drawCircle(color = fill, radius = w * 0.17f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.28f))
    drawCircle(color = stroke, radius = w * 0.17f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.28f), style = Stroke(width = sw))

    // Highlight dot on head
    drawCircle(color = accent.copy(alpha = 0.6f), radius = w * 0.05f, center = androidx.compose.ui.geometry.Offset(w * 0.46f, h * 0.24f))
}

private fun DrawScope.drawKnight(fill: Color, stroke: Color, accent: Color, isWhite: Boolean) {
    val w = size.width
    val h = size.height
    val sw = (w * 0.045f).coerceAtLeast(1.5f)

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.90f)
        lineTo(w * 0.82f, h * 0.90f)
        lineTo(w * 0.75f, h * 0.80f)
        lineTo(w * 0.25f, h * 0.80f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Sculpted Knight Body & Head
    val knightPath = Path().apply {
        moveTo(w * 0.28f, h * 0.80f) // bottom left of neck
        lineTo(w * 0.70f, h * 0.80f) // bottom right of back
        quadraticBezierTo(w * 0.78f, h * 0.65f, w * 0.72f, h * 0.40f) // arched mane
        lineTo(w * 0.68f, h * 0.22f) // back of ear
        lineTo(w * 0.60f, h * 0.14f) // ear tip
        lineTo(w * 0.54f, h * 0.25f) // forehead
        quadraticBezierTo(w * 0.42f, h * 0.32f, w * 0.22f, h * 0.44f) // muzzle top
        quadraticBezierTo(w * 0.18f, h * 0.50f, w * 0.24f, h * 0.56f) // muzzle bottom / mouth
        lineTo(w * 0.38f, h * 0.54f) // jaw
        quadraticBezierTo(w * 0.46f, h * 0.64f, w * 0.30f, h * 0.78f) // breast/chest
        close()
    }
    drawPath(knightPath, fill, style = Fill)
    drawPath(knightPath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round, cap = StrokeCap.Round))

    // Eye
    val eyeCenter = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.34f)
    drawCircle(color = stroke, radius = w * 0.035f, center = eyeCenter)

    // Nostril
    drawCircle(color = stroke, radius = w * 0.02f, center = androidx.compose.ui.geometry.Offset(w * 0.26f, h * 0.49f))

    // Mane accent lines
    val manePath = Path().apply {
        moveTo(w * 0.68f, h * 0.35f)
        lineTo(w * 0.60f, h * 0.42f)
        moveTo(w * 0.72f, h * 0.48f)
        lineTo(w * 0.64f, h * 0.55f)
        moveTo(w * 0.73f, h * 0.62f)
        lineTo(w * 0.66f, h * 0.68f)
    }
    drawPath(manePath, accent, style = Stroke(width = sw * 0.8f, cap = StrokeCap.Round))
}

private fun DrawScope.drawBishop(fill: Color, stroke: Color, accent: Color) {
    val w = size.width
    val h = size.height
    val sw = (w * 0.045f).coerceAtLeast(1.5f)

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.90f)
        lineTo(w * 0.82f, h * 0.90f)
        lineTo(w * 0.75f, h * 0.78f)
        lineTo(w * 0.25f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Lower collar
    drawRect(color = accent, topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.72f), size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.06f))
    drawRect(color = stroke, topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.72f), size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.06f), style = Stroke(width = sw))

    // Mitre body
    val mitrePath = Path().apply {
        moveTo(w * 0.36f, h * 0.72f)
        quadraticBezierTo(w * 0.22f, h * 0.46f, w * 0.48f, h * 0.22f)
        lineTo(w * 0.52f, h * 0.22f)
        quadraticBezierTo(w * 0.78f, h * 0.46f, w * 0.64f, h * 0.72f)
        close()
    }
    drawPath(mitrePath, fill, style = Fill)
    drawPath(mitrePath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Slash / Cleft in Bishop's hat
    val slashPath = Path().apply {
        moveTo(w * 0.55f, h * 0.32f)
        lineTo(w * 0.36f, h * 0.50f)
    }
    drawPath(slashPath, stroke, style = Stroke(width = sw * 1.2f, cap = StrokeCap.Round))

    // Cross or small circle on top
    drawCircle(color = accent, radius = w * 0.06f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.16f))
    drawCircle(color = stroke, radius = w * 0.06f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.16f), style = Stroke(width = sw))
}

private fun DrawScope.drawRook(fill: Color, stroke: Color, accent: Color) {
    val w = size.width
    val h = size.height
    val sw = (w * 0.045f).coerceAtLeast(1.5f)

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.90f)
        lineTo(w * 0.82f, h * 0.90f)
        lineTo(w * 0.76f, h * 0.78f)
        lineTo(w * 0.24f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Waist tower
    val towerPath = Path().apply {
        moveTo(w * 0.28f, h * 0.78f)
        lineTo(w * 0.32f, h * 0.42f)
        lineTo(w * 0.68f, h * 0.42f)
        lineTo(w * 0.72f, h * 0.78f)
        close()
    }
    drawPath(towerPath, fill, style = Fill)
    drawPath(towerPath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Corbel cornice
    drawRect(color = accent, topLeft = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.36f), size = androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.06f))
    drawRect(color = stroke, topLeft = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.36f), size = androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.06f), style = Stroke(width = sw))

    // Battlements (3 merlons, 2 embrasures)
    val battlementsPath = Path().apply {
        moveTo(w * 0.24f, h * 0.36f)
        lineTo(w * 0.24f, h * 0.20f)
        lineTo(w * 0.34f, h * 0.20f)
        lineTo(w * 0.34f, h * 0.28f)
        lineTo(w * 0.44f, h * 0.28f)
        lineTo(w * 0.44f, h * 0.20f)
        lineTo(w * 0.56f, h * 0.20f)
        lineTo(w * 0.56f, h * 0.28f)
        lineTo(w * 0.66f, h * 0.28f)
        lineTo(w * 0.66f, h * 0.20f)
        lineTo(w * 0.76f, h * 0.20f)
        lineTo(w * 0.76f, h * 0.36f)
        close()
    }
    drawPath(battlementsPath, fill, style = Fill)
    drawPath(battlementsPath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))
}

private fun DrawScope.drawQueen(fill: Color, stroke: Color, accent: Color) {
    val w = size.width
    val h = size.height
    val sw = (w * 0.045f).coerceAtLeast(1.5f)

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.16f, h * 0.90f)
        lineTo(w * 0.84f, h * 0.90f)
        lineTo(w * 0.76f, h * 0.78f)
        lineTo(w * 0.24f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Middle waist
    val waistPath = Path().apply {
        moveTo(w * 0.30f, h * 0.78f)
        quadraticBezierTo(w * 0.36f, h * 0.58f, w * 0.38f, h * 0.50f)
        lineTo(w * 0.62f, h * 0.50f)
        quadraticBezierTo(w * 0.64f, h * 0.58f, w * 0.70f, h * 0.78f)
        close()
    }
    drawPath(waistPath, fill, style = Fill)
    drawPath(waistPath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Coronet / Crown (5 points)
    val crownPath = Path().apply {
        moveTo(w * 0.24f, h * 0.50f)
        lineTo(w * 0.18f, h * 0.28f) // left peak
        lineTo(w * 0.32f, h * 0.40f)
        lineTo(w * 0.38f, h * 0.22f) // mid-left peak
        lineTo(w * 0.50f, h * 0.38f)
        lineTo(w * 0.62f, h * 0.22f) // mid-right peak
        lineTo(w * 0.68f, h * 0.40f)
        lineTo(w * 0.82f, h * 0.28f) // right peak
        lineTo(w * 0.76f, h * 0.50f)
        close()
    }
    drawPath(crownPath, fill, style = Fill)
    drawPath(crownPath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Jewels on crown points
    val pointCenters = listOf(
        androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.26f),
        androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.20f),
        androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.16f), // central pearl
        androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.20f),
        androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.26f)
    )
    for (pt in pointCenters) {
        drawCircle(color = accent, radius = w * 0.04f, center = pt)
        drawCircle(color = stroke, radius = w * 0.04f, center = pt, style = Stroke(width = sw))
    }
}

private fun DrawScope.drawKing(fill: Color, stroke: Color, accent: Color) {
    val w = size.width
    val h = size.height
    val sw = (w * 0.045f).coerceAtLeast(1.5f)

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.16f, h * 0.90f)
        lineTo(w * 0.84f, h * 0.90f)
        lineTo(w * 0.76f, h * 0.78f)
        lineTo(w * 0.24f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Waist body
    val waistPath = Path().apply {
        moveTo(w * 0.28f, h * 0.78f)
        quadraticBezierTo(w * 0.34f, h * 0.58f, w * 0.36f, h * 0.46f)
        lineTo(w * 0.64f, h * 0.46f)
        quadraticBezierTo(w * 0.66f, h * 0.58f, w * 0.72f, h * 0.78f)
        close()
    }
    drawPath(waistPath, fill, style = Fill)
    drawPath(waistPath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Royal dome crown
    val domePath = Path().apply {
        moveTo(w * 0.26f, h * 0.46f)
        quadraticBezierTo(w * 0.20f, h * 0.32f, w * 0.36f, h * 0.26f)
        lineTo(w * 0.50f, h * 0.32f)
        lineTo(w * 0.64f, h * 0.26f)
        quadraticBezierTo(w * 0.80f, h * 0.32f, w * 0.74f, h * 0.46f)
        close()
    }
    drawPath(domePath, fill, style = Fill)
    drawPath(domePath, stroke, style = Stroke(width = sw, join = StrokeJoin.Round))

    // Golden cross on top
    val crossH = Path().apply {
        // Vertical post
        moveTo(w * 0.50f, h * 0.12f)
        lineTo(w * 0.50f, h * 0.28f)
        // Horizontal bar
        moveTo(w * 0.42f, h * 0.18f)
        lineTo(w * 0.58f, h * 0.18f)
    }
    drawPath(crossH, accent, style = Stroke(width = sw * 2.2f, cap = StrokeCap.Square))
    drawPath(crossH, stroke, style = Stroke(width = sw * 1.0f, cap = StrokeCap.Square))
}
