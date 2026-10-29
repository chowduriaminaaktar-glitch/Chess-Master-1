package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chess.model.BoardTheme
import com.example.chess.model.FlipMode
import com.example.chess.model.TimeControl

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewGameDialog(
    initialWhiteName: String,
    initialBlackName: String,
    initialTimeControl: TimeControl,
    initialFlipMode: FlipMode,
    initialTheme: BoardTheme,
    onStartGame: (white: String, black: String, tc: TimeControl, mode: FlipMode, theme: BoardTheme) -> Unit,
    onDismiss: () -> Unit
) {
    var whiteName by remember { mutableStateOf(initialWhiteName) }
    var blackName by remember { mutableStateOf(initialBlackName) }
    var selectedTimeControl by remember { mutableStateOf(initialTimeControl) }
    var selectedFlipMode by remember { mutableStateOf(initialFlipMode) }
    var selectedTheme by remember { mutableStateOf(initialTheme) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(24.dp))
                .testTag("new_game_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "New Chess Match",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24)
                )
                Text(
                    text = "Configure local two-player game",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Player Names
                Text(
                    text = "PLAYERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = whiteName,
                    onValueChange = { whiteName = it },
                    label = { Text("Player 1 (White)") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedLabelColor = Color(0xFFFBBF24),
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedIndicatorColor = Color(0xFFFBBF24)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("white_player_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = blackName,
                    onValueChange = { blackName = it },
                    label = { Text("Player 2 (Black)") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedIndicatorColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("black_player_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Time Control
                Text(
                    text = "TIME CONTROL (CHESS CLOCK)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        TimeControl.UNLIMITED,
                        TimeControl.BLITZ_3_0,
                        TimeControl.BLITZ_5_0,
                        TimeControl.RAPID_10_0,
                        TimeControl.RAPID_15_10
                    )
                    for (tc in presets) {
                        val isSelected = selectedTimeControl == tc
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFF59E0B) else Color(0xFF212836))
                                .clickable { selectedTimeControl = tc }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("time_control_${tc.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tc.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF1E1B18) else Color(0xFFE2E8F0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Mobile Pass & Play View Mode
                Text(
                    text = "ONE MOBILE PASS-AND-PLAY VIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val flipModes = listOf(
                    FlipMode.AUTO_FLIP to "Auto-Flip on Turn",
                    FlipMode.FACE_TO_FACE to "Face-to-Face (Table Mode)",
                    FlipMode.FIXED_WHITE to "Fixed Perspective"
                )

                for ((mode, desc) in flipModes) {
                    val isSelected = selectedFlipMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x33F59E0B) else Color(0xFF1E242B))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFF59E0B) else Color(0x11FFFFFF),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFlipMode = mode }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("mode_${mode.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFF59E0B) else Color(0x33FFFFFF))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = desc,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Board Theme
                Text(
                    text = "BOARD THEME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (th in BoardTheme.values()) {
                        val isSelected = selectedTheme == th
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x33F59E0B) else Color(0xFF1E242B))
                                .border(
                                    1.5.dp,
                                    if (isSelected) Color(0xFFF59E0B) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedTheme = th }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Mini square swatch
                            Row(modifier = Modifier.size(32.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(th.lightHex)))
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(th.darkHex)))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(th.darkHex)))
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(th.lightHex)))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = th.displayName.split(" ").first(),
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Start button
                Button(
                    onClick = {
                        onStartGame(
                            whiteName,
                            blackName,
                            selectedTimeControl,
                            selectedFlipMode,
                            selectedTheme
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_match_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF1E1B18)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Start Match",
                        color = Color(0xFF1E1B18),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}
