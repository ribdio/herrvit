package se.techlisbon.mrwhite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun MrWhiteApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            var screen by remember { mutableStateOf<Screen>(Screen.Setup) }
            var showCancelDialog by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val currentScreen = screen) {
                    is Screen.Setup -> SetupScreen(
                        onStart = { players -> screen = Screen.Reveal(players, 0, emptyList()) }
                    )
                    is Screen.Reveal -> {
                        RevealScreen(
                            players = currentScreen.players,
                            currentIndex = currentScreen.index,
                            eliminated = currentScreen.eliminated,
                            onNext = {
                                val alivePlayers = currentScreen.players.filter {
                                    !currentScreen.eliminated.contains(it.name)
                                }
                                screen = if (currentScreen.index < alivePlayers.size - 1) {
                                    Screen.Reveal(currentScreen.players, currentScreen.index + 1, currentScreen.eliminated)
                                } else {
                                    // Shuffle players for speaking order before starting
                                    val shuffledPlayers = weightedShuffle(context, currentScreen.players)
                                    Screen.StartAnnouncement(shuffledPlayers, currentScreen.eliminated)
                                }
                            },
                            onCancel = { showCancelDialog = true }
                        )
                    }
                    is Screen.StartAnnouncement -> StartAnnouncementScreen(
                        players = currentScreen.players,
                        eliminated = currentScreen.eliminated,
                        onContinue = { screen = Screen.Vote(currentScreen.players, currentScreen.eliminated) },
                        onCancel = { showCancelDialog = true }
                    )
                    is Screen.Vote -> VoteScreen(
                        players = currentScreen.players,
                        eliminated = currentScreen.eliminated,
                        onEliminate = { eliminated ->
                            screen = Screen.Result(currentScreen.players, eliminated, currentScreen.eliminated + eliminated.name)
                        },
                        onCancel = { showCancelDialog = true }
                    )
                    is Screen.Result -> ResultScreen(
                        players = currentScreen.players,
                        eliminated = currentScreen.eliminated,
                        onContinue = {
                            // Check if Mr. White was eliminated
                            if (currentScreen.eliminated.role == Role.MR_WHITE) {
                                // Pass eliminated list to MrWhiteGuess so it can check if other Mr. Whites remain
                                screen = Screen.MrWhiteGuess(currentScreen.players, currentScreen.allEliminated)
                            } else {
                                val alivePlayers = currentScreen.players.filter {
                                    !currentScreen.allEliminated.contains(it.name)
                                }
                                val mrWhitesAlive = alivePlayers.count { it.role == Role.MR_WHITE }
                                val aliveImpostors = alivePlayers.count { it.role == Role.UNDERCOVER || it.role == Role.MR_WHITE }
                                val aliveCivilians = alivePlayers.count { it.role == Role.CIVILIAN }

                                screen = when {
                                    mrWhitesAlive == 0 -> Screen.GameOver(currentScreen.players, "Civilians win!")
                                    aliveCivilians < aliveImpostors -> Screen.GameOver(currentScreen.players, "Impostors win!")
                                    alivePlayers.size <= 2 -> Screen.GameOver(currentScreen.players, "Mr. White wins!")
                                    else -> Screen.Vote(currentScreen.players, currentScreen.allEliminated)
                                }
                            }
                        },
                        onCancel = { showCancelDialog = true }
                    )
                    is Screen.MrWhiteGuess -> MrWhiteGuessScreen(
                        players = currentScreen.players,
                        eliminated = currentScreen.eliminated,
                        onGameOver = { winner ->
                            screen = Screen.GameOver(currentScreen.players, winner)
                        },
                        onContinue = { eliminatedList ->
                            // Continue game - check win conditions and go back to vote if game continues
                            val alivePlayers = currentScreen.players.filter {
                                !eliminatedList.contains(it.name)
                            }
                            val mrWhitesAlive = alivePlayers.count { it.role == Role.MR_WHITE }
                            val aliveImpostors = alivePlayers.count { it.role == Role.UNDERCOVER || it.role == Role.MR_WHITE }
                            val aliveCivilians = alivePlayers.count { it.role == Role.CIVILIAN }

                            screen = when {
                                mrWhitesAlive == 0 -> Screen.GameOver(currentScreen.players, "Civilians win!")
                                aliveCivilians < aliveImpostors -> Screen.GameOver(currentScreen.players, "Impostors win!")
                                alivePlayers.size <= 2 -> Screen.GameOver(currentScreen.players, "Mr. White wins!")
                                else -> Screen.Vote(currentScreen.players, eliminatedList)
                            }
                        },
                        onCancel = { showCancelDialog = true }
                    )
                    is Screen.GameOver -> GameOverScreen(
                        players = currentScreen.players,
                        winner = currentScreen.winner,
                        onRestart = { screen = Screen.Setup }
                    )
                }

                // Cancel confirmation dialog
                if (showCancelDialog) {
                    AlertDialog(
                        onDismissRequest = { showCancelDialog = false },
                        title = { Text("Cancel Game?") },
                        text = { Text("Are you sure? You will lose all progress.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showCancelDialog = false
                                screen = Screen.Setup
                            }) {
                                Text("YES, CANCEL")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCancelDialog = false }) {
                                Text("KEEP PLAYING")
                            }
                        }
                    )
                }
            }
        }
    }
}