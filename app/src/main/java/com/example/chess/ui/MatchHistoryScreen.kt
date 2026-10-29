package com.example.chess.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chess.data.MatchRecord
import com.example.chess.data.MatchStatistics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryScreen(
    matches: List<MatchRecord>,
    statistics: MatchStatistics,
    onBack: () -> Unit,
    onDeleteMatch: (Long) -> Unit,
    onClearAll: () -> Unit
) {
    BackHandler { onBack() }

    var selectedMatchForDetail by remember { mutableStateOf<MatchRecord?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F141C),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Match Records & Stats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_from_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (matches.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All",
                                tint = Color(0xFFF87171)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F141C))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Statistics Overview Cards
            item {
                StatsOverviewCard(stats = statistics)
            }

            item {
                Text(
                    text = "LOCAL MATCH HISTORY (${matches.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            if (matches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matches played yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Complete a pass-and-play match to view records here.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            } else {
                items(matches, key = { it.id }) { match ->
                    MatchItemCard(
                        match = match,
                        onClick = { selectedMatchForDetail = match },
                        onDelete = { onDeleteMatch(match.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Detail dialog
    selectedMatchForDetail?.let { match ->
        MatchDetailDialog(
            match = match,
            onDismiss = { selectedMatchForDetail = null }
        )
    }

    // Clear confirmation dialog
    if (showClearConfirm) {
        Dialog(onDismissRequest = { showClearConfirm = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242B))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Clear All History?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will permanently delete all saved match records and reset stats.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showClearConfirm = false }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            onClearAll()
                            showClearConfirm = false
                        }) {
                            Text("Delete All", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsOverviewCard(stats: MatchStatistics) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161C26)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Player Head-to-Head Stats",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24)
                )
                Text(
                    text = "${stats.totalMatches} Matches",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    title = "White Wins",
                    count = stats.whiteWins,
                    percent = stats.whiteWinRate,
                    color = Color(0xFFF59E0B)
                )
                StatColumn(
                    title = "Black Wins",
                    count = stats.blackWins,
                    percent = stats.blackWinRate,
                    color = Color(0xFF38BDF8)
                )
                StatColumn(
                    title = "Draws",
                    count = stats.draws,
                    percent = stats.drawRate,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun StatColumn(title: String, count: Int, percent: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = String.format(Locale.getDefault(), "%.1f%%", percent),
            fontSize = 10.sp,
            color = color.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MatchItemCard(
    match: MatchRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(match.playedAt) {
        SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(match.playedAt))
    }

    val durationMin = match.durationSeconds / 60
    val durationSec = match.durationSeconds % 60

    val isWhiteWin = match.winner.equals("WHITE", ignoreCase = true)
    val isBlackWin = match.winner.equals("BLACK", ignoreCase = true)
    val isDraw = match.winner.equals("DRAW", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141922)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("match_card_${match.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Players and Winner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${match.whitePlayer} (W)",
                        fontSize = 14.sp,
                        fontWeight = if (isWhiteWin) FontWeight.Bold else FontWeight.Medium,
                        color = if (isWhiteWin) Color(0xFFFBBF24) else Color(0xFFE2E8F0)
                    )
                    Text(
                        text = " vs ",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${match.blackPlayer} (B)",
                        fontSize = 14.sp,
                        fontWeight = if (isBlackWin) FontWeight.Bold else FontWeight.Medium,
                        color = if (isBlackWin) Color(0xFF38BDF8) else Color(0xFFE2E8F0)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Result Badge & Reason
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val resultText = when {
                        isWhiteWin -> "White Won"
                        isBlackWin -> "Black Won"
                        else -> "Draw"
                    }
                    val badgeColor = when {
                        isWhiteWin -> Color(0xFFF59E0B)
                        isBlackWin -> Color(0xFF38BDF8)
                        else -> Color(0xFF94A3B8)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = resultText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "(${match.winReason.lowercase().replace("_", " ")})",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Metadata: Time control, moves, duration, date
                Text(
                    text = "${match.timeControlName} · ${match.totalMoves} moves · ${durationMin}m ${durationSec}s · $dateStr",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp).testTag("delete_match_${match.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MatchDetailDialog(
    match: MatchRecord,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C26)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Match Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${match.whitePlayer} vs ${match.blackPlayer}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "NOTATION (PGN)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F141C))
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (match.pgnMoves.isNotBlank()) match.pgnMoves else "No notation logged.",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = Color(0xFFFBBF24))
                }
            }
        }
    }
}
