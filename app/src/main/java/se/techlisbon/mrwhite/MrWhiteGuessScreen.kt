package se.techlisbon.mrwhite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MrWhiteGuessScreen(
    players: List<Player>,
    eliminated: List<String> = emptyList(),
    onGameOver: (String) -> Unit,
    onContinue: (List<String>) -> Unit,
    onCancel: () -> Unit
) {
    var mrWhiteGuess by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MR. WHITE'S LAST CHANCE!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Guess the Civilian word to win!\n(Hint: Guessing the Undercover word helps them instead)",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = mrWhiteGuess,
                onValueChange = { mrWhiteGuess = it },
                label = { Text("Your Guess") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val result = GameEngine.checkMrWhiteGuess(mrWhiteGuess, players)
                    when (result) {
                        GuessResult.CIVILIAN_WORD -> onGameOver("Mr. White wins!")
                        GuessResult.UNDERCOVER_WORD -> onGameOver("Undercovers win!")
                        GuessResult.WRONG -> {
                            // Check if other Mr. Whites are still alive
                            val alivePlayers = players.filter { !eliminated.contains(it.name) }
                            val mrWhitesAlive = alivePlayers.count { it.role == Role.MR_WHITE }

                            if (mrWhitesAlive == 0) {
                                // No Mr. Whites remain, civilians win
                                onGameOver("Civilians win!")
                            } else {
                                // Other Mr. Whites alive, continue game
                                onContinue(eliminated)
                            }
                        }
                    }
                },
                enabled = mrWhiteGuess.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("SUBMIT GUESS", fontSize = 18.sp)
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