package se.techlisbon.mrwhite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VoteScreen(
    players: List<Player>,
    eliminated: List<String>,
    onEliminate: (Player) -> Unit,
    onCancel: () -> Unit
) {
    val alivePlayers = players.filter { !eliminated.contains(it.name) }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Text(
                text = "Who has the most votes?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "Host: Discuss and pick the player with most votes",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid layout showing all players (alive and dead)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players.chunked(3)) { rowPlayers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPlayers.forEach { player ->
                            val isDead = eliminated.contains(player.name)
                            Card(
                                onClick = { if (!isDead) selectedPlayer = player },
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isDead -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        selectedPlayer == player -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                ),
                                modifier = Modifier.weight(1f),
                                enabled = !isDead
                            ) {
                                val backgroundColor = when {
                                    isDead -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    selectedPlayer == player -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }

                                val textColor = if (isDead) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                } else {
                                    contentColorFor(backgroundColor)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = player.name,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        textDecoration = if (isDead) TextDecoration.LineThrough else null,
                                        color = textColor
                                    )
                                }
                            }
                        }
                        // Fill empty spaces if row has less than 3 players
                        repeat(3 - rowPlayers.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { selectedPlayer?.let { onEliminate(it) } },
                enabled = selectedPlayer != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "ELIMINATE ${selectedPlayer?.name ?: ""}",
                    fontSize = 18.sp
                )
            }
        }

        // Cancel button
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .statusBarsPadding()
        ) {
            Icon(Icons.Default.Close, "Cancel game", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}