package se.techlisbon.mrwhite

import java.text.Normalizer
import kotlin.math.ceil
import kotlin.math.floor

object GameEngine {
    fun createGame(
        playerNames: List<String>,
        wordPair: Pair<String, String>,
        undercoverCount: Int
    ): List<Player> {
        // Calculate undercovers if random (0 means random)
        val actualUndercoverCount = if (undercoverCount == 0) {
            val playerCount = playerNames.size
            // Random between 10-40% of total players
            val minUndercovers = ceil(playerCount * 0.1).toInt()
            val maxUndercovers = floor((playerCount - 2) / 2.0).toInt().coerceAtLeast(1)
            (minUndercovers..maxUndercovers).random()
        } else {
            undercoverCount
        }

        // Randomly decide which word goes to which role
        val (civilianWord, undercoverWord) = if (kotlin.random.Random.nextBoolean()) {
            wordPair.first to wordPair.second
        } else {
            wordPair.second to wordPair.first
        }

        // Create a list of roles and shuffle them
        val roles = mutableListOf<Pair<Role, String>>()

        // 1 Mr. White
        roles.add(Role.MR_WHITE to "")

        // N Undercovers
        repeat(actualUndercoverCount) {
            roles.add(Role.UNDERCOVER to undercoverWord)
        }

        // Rest are Civilians
        val civilianCount = playerNames.size - actualUndercoverCount - 1
        repeat(civilianCount) {
            roles.add(Role.CIVILIAN to civilianWord)
        }

        // Shuffle roles and assign to players in their original order
        val shuffledRoles = roles.shuffled()
        val players = playerNames.mapIndexed { index, name ->
            val (role, word) = shuffledRoles[index]
            Player(name, role, word)
        }

        // Return players in original order (no weighted shuffle here)
        return players
    }

    // Extension function on String to perform the normalization
    private val String.normalized: String
        get() {
            return Normalizer.normalize(this.trim().lowercase(), Normalizer.Form.NFD)
                .replace("\\p{Mn}+".toRegex(), "")
        }

    fun checkMrWhiteGuess(guess: String, players: List<Player>): GuessResult {
        val civilianWord = players.firstOrNull { it.role == Role.CIVILIAN }?.word ?: return GuessResult.WRONG
        val undercoverWord = players.firstOrNull { it.role == Role.UNDERCOVER }?.word ?: return GuessResult.WRONG

        val normGuess = guess.trim().normalized

        return when {
            normGuess.equals(civilianWord.trim().normalized, ignoreCase = true) -> GuessResult.CIVILIAN_WORD
            normGuess.equals(undercoverWord.trim().normalized, ignoreCase = true) -> GuessResult.UNDERCOVER_WORD
            else -> GuessResult.WRONG
        }
    }
}

// Weighted shuffle where Mr. White has half the chance of landing first or last
fun weightedShuffle(players: List<Player>): List<Player> {
    val result = MutableList<Player?>(players.size) { null }.toMutableList()
    val pool = players.toMutableList()

    fun pickWeighted(forEdge: Boolean): Player {
        if (!forEdge) {
            // Normal pick for middle positions
            return pool.removeAt((0 until pool.size).random())
        }

        // Weighted pick for first/last index
        val weighted = mutableListOf<Player>()

        pool.forEach { p ->
            if (p.role == Role.MR_WHITE) {
                // Half weight → add once
                weighted.add(p)
            } else {
                // Normal weight → add twice
                weighted.add(p)
                weighted.add(p)
            }
        }

        val chosen = weighted.random()
        pool.remove(chosen)
        return chosen
    }

    val lastIndex = players.size - 1

    // First position (weighted)
    result[0] = pickWeighted(forEdge = true)

    // Last position (weighted)
    if (players.size > 1) {
        result[lastIndex] = pickWeighted(forEdge = true)
    }

    // Middle positions (normal random)
    for (i in 1 until lastIndex) {
        result[i] = pickWeighted(forEdge = false)
    }

    return result.filterNotNull()
}
