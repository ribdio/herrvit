package se.techlisbon.mrwhite

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RevealScreen(
    players: List<Player>,
    currentIndex: Int,
    eliminated: List<String>,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    val alivePlayers = players.filter { !eliminated.contains(it.name) }
    val player = alivePlayers.getOrNull(currentIndex) ?: return

    // Key state by both currentIndex AND player.name to ensure full reset
    val stateKey = "${currentIndex}_${player.name}"
    var isRevealing by remember(stateKey) { mutableStateOf(false) }
    var revealStartTime by remember(stateKey) { mutableStateOf(0L) }
    var hasHeldWordVisible by remember(stateKey) { mutableStateOf(false) }
    var buttonEnabled by remember(stateKey) { mutableStateOf(false) }

    // Check if word has been held for 1 second
    LaunchedEffect(isRevealing, stateKey) {
        if (isRevealing && revealStartTime == 0L) {
            revealStartTime = System.currentTimeMillis()
        }

        if (isRevealing) {
            while (isRevealing) {
                kotlinx.coroutines.delay(100)
                val heldDuration = System.currentTimeMillis() - revealStartTime
                if (heldDuration >= 1000 && !hasHeldWordVisible) {
                    hasHeldWordVisible = true
                }
            }
        }
    }

    // Enable button 2 seconds after holding word visible
    LaunchedEffect(hasHeldWordVisible, stateKey) {
        if (hasHeldWordVisible) {
            buttonEnabled = true
        }
    }

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
                text = player.name,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Touch-to-hold card - NO color coding for undercover
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(stateKey) {
                        detectTapGestures(
                            onPress = {
                                isRevealing = true
                                revealStartTime = System.currentTimeMillis()
                                tryAwaitRelease()
                                isRevealing = false
                                revealStartTime = 0L
                            }
                        )
                    },
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        !isRevealing -> MaterialTheme.colorScheme.surface
                        // player.role == Role.MR_WHITE -> Color(0xFFE53935)
                        else -> Color(0xFF1976D2)
                    }
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRevealing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            /*if (player.role == Role.MR_WHITE) {
                                Text(
                                    text = "MR. WHITE",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }*/
                            Text(
                                text = player.word,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "👆 PRESS & HOLD",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Fixed height container to prevent content shift
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (buttonEnabled) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = if (currentIndex < alivePlayers.size - 1) "NEXT PLAYER" else "CONTINUE",
                            fontSize = 18.sp
                        )
                    }
                } else if (hasHeldWordVisible) {
                    Text(
                        text = "Memorizing...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Cancel button in top-right
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