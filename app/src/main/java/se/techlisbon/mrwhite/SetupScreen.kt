package se.techlisbon.mrwhite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max

@Composable
fun SetupScreen(onStart: (List<Player>) -> Unit) {
    val context = LocalContext.current

    var names by remember { mutableStateOf(PrefsManager.getPlayers(context)) }
    var newName by remember { mutableStateOf("") }
    var wordUrl by remember { mutableStateOf(PrefsManager.getWordUrl(context)) }
    var loadedWords by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var wordStatus by remember { mutableStateOf<String?>(null) }
    var showInstructions by remember { mutableStateOf(false) }

    val maxUndercovers = floor((names.size - 2) / 2.0).toInt().coerceAtLeast(1)
    var undercoverCount by remember { mutableIntStateOf(0) } // 0 = random

    val scope = rememberCoroutineScope()
    val wordLoader = remember { WordLoader() }

    // Load words when URL changes
    LaunchedEffect(wordUrl) {
        if (wordUrl.isNotBlank()) {
            loading = true
            wordStatus = "Loading..."
            scope.launch {
                val result = wordLoader.loadWords(wordUrl)
                result.fold(
                    onSuccess = { words ->
                        loadedWords = words
                        wordStatus = "✓ ${words.size} word pairs loaded"
                        loading = false
                    },
                    onFailure = { error ->
                        loadedWords = null
                        wordStatus = "✗ ${error.message}"
                        loading = false
                    }
                )
            }
        } else {
            loadedWords = null
            wordStatus = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding() // Add padding for status bar
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MR. WHITE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showInstructions = true }) {
                Icon(Icons.Default.Info, "Instructions")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Word URL input with live validation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = wordUrl,
                onValueChange = { wordUrl = it },
                label = { Text("Custom Word List URL") },
                placeholder = { Text("Optional - uses defaults if empty") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = wordStatus?.startsWith("✗") == true
            )
        }

        wordStatus?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = if (it.startsWith("✓")) Color.Green else if (it.startsWith("✗")) Color.Red else Color.Gray,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick add player
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Player name") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = newName.isNotBlank() && names.any { it.equals(newName.trim(), ignoreCase = true) }
            )

            IconButton(
                onClick = {
                    if (newName.isNotBlank() && !names.any { it.equals(newName.trim(), ignoreCase = true) }) {
                        names = names + newName.trim()
                        newName = ""
                    }
                },
                enabled = newName.isNotBlank() && !names.any { it.equals(newName.trim(), ignoreCase = true) }
            ) {
                Icon(Icons.Default.Add, "Add")
            }
        }

        if (newName.isNotBlank() && names.any { it.equals(newName.trim(), ignoreCase = true) }) {
            Text(
                text = "Player name already exists",
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Undercover count slider
        if (names.size >= 4) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (undercoverCount == 0) {
                            "Undercovers: Random (10-40%)"
                        } else {
                            "Undercovers: $undercoverCount"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = undercoverCount.toFloat(),
                        onValueChange = { undercoverCount = it.toInt() },
                        valueRange = 0f..maxUndercovers.toFloat(),
                        steps = maxUndercovers - 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = if (undercoverCount == 0) {
                            "Civilians: ${names.size - 1} (varies)"
                        } else {
                            "Civilians: ${names.size - undercoverCount - 1} | Mr. White: 1"
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Player list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(names) { index, name ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}. $name")
                        IconButton(onClick = {
                            names = names.filterIndexed { i, _ -> i != index }
                            undercoverCount = minOf(undercoverCount, maxOf(0, floor((names.size / 2.0)).toInt() - 1))
                        }) {
                            Icon(Icons.Default.Delete, "Remove")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start button
        Button(
            onClick = {
                scope.launch {
                    // Save preferences
                    PrefsManager.saveWordUrl(context, wordUrl)
                    PrefsManager.savePlayers(context, names)

                    val words = loadedWords ?: wordLoader.loadWords("").getOrNull()!!
                    val wordPair = words.random()
                    val players = GameEngine.createGame(names, wordPair, undercoverCount)
                    onStart(players)
                }
            },
            enabled = names.size >= 4 && !loading && (wordUrl.isBlank() || loadedWords != null),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("START GAME (${names.size} players)")
        }

        if (names.size < 4) {
            Text(
                text = "Need at least 4 players",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (showInstructions) {
        InstructionsDialog(onDismiss = { showInstructions = false })
    }
}

