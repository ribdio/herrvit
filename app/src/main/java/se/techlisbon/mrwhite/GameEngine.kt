package se.techlisbon.mrwhite

import android.content.Context
import java.security.SecureRandom
import java.text.Normalizer
import kotlin.math.ceil
import kotlin.math.floor

object GameEngine {
    private val secureRandom = SecureRandom()

    // Helper extension functions for SecureRandom
    private fun IntRange.randomSecure(): Int {
        val size = last - first + 1
        return first + secureRandom.nextInt(size)
    }

    private fun <T> List<T>.randomSecure(): T {
        return this[secureRandom.nextInt(size)]
    }

    private fun <T> List<T>.shuffledSecure(): List<T> {
        val list = this.toMutableList()
        for (i in list.size - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val temp = list[i]
            list[i] = list[j]
            list[j] = temp
        }
        return list
    }

    fun createGame(
        playerNames: List<String>,
        wordPair: Pair<String, String>,
        undercoverCount: Int,
        mrWhiteCount: Int = 1
    ): List<Player> {
        // Calculate undercovers if random (0 means random)
        val actualUndercoverCount = if (undercoverCount == 0) {
            val playerCount = playerNames.size
            // Random between 10-40% of total players
            val minUndercovers = ceil(playerCount * 0.1).toInt()
            val maxUndercovers = floor((playerCount - 2) / 2.0).toInt().coerceAtLeast(1)
            (minUndercovers..maxUndercovers).randomSecure()
        } else {
            undercoverCount
        }

        // Calculate Mr. Whites if random (0 means random)
        // Rule: max 1 Mr. White per 3 undercovers, but at least 1
        val actualMrWhiteCount = if (mrWhiteCount == 0) {
            val maxMrWhites = (actualUndercoverCount / 3).coerceAtLeast(1)
            (1..maxMrWhites).randomSecure()
        } else {
            mrWhiteCount
        }

        // Randomly decide which word goes to which role
        val (civilianWord, undercoverWord) = if (secureRandom.nextBoolean()) {
            wordPair.first to wordPair.second
        } else {
            wordPair.second to wordPair.first
        }

        // Create a list of roles and shuffle them
        val roles = mutableListOf<Pair<Role, String>>()

        // N Mr. Whites
        repeat(actualMrWhiteCount) {
            roles.add(Role.MR_WHITE to "?".repeat(civilianWord.length))
        }

        // N Undercovers
        repeat(actualUndercoverCount) {
            roles.add(Role.UNDERCOVER to undercoverWord)
        }

        // Rest are Civilians
        val civilianCount = playerNames.size - actualUndercoverCount - actualMrWhiteCount
        repeat(civilianCount) {
            roles.add(Role.CIVILIAN to civilianWord)
        }

        // Shuffle roles and assign to players in their original order
        val shuffledRoles = roles.shuffledSecure()
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

/**
 * Weighted shuffle for speaking order.
 * - Mr. White has half the chance of being picked first
 * - Won't pick the same player who started last game
 */
fun weightedShuffle(context: Context, players: List<Player>): List<Player> {
    val secureRandom = SecureRandom()
    val result = MutableList<Player?>(players.size) { null }.toMutableList()
    val pool = players.toMutableList()
    val lastStarter = PrefsManager.getLastStarter(context)

    fun pickWeighted(forFirstPosition: Boolean): Player {
        if (!forFirstPosition) {
            // Normal pick for non-first positions
            val index = secureRandom.nextInt(pool.size)
            return pool.removeAt(index)
        }

        // Weighted pick for first position
        // Mr. White has half weight, and don't pick last starter
        val weighted = mutableListOf<Player>()

        pool.forEach { p ->
            // Skip the last starter if they're still in the pool
            if (p.name == lastStarter) {
                return@forEach
            }

            if (p.role == Role.MR_WHITE) {
                // Half weight → add once
                weighted.add(p)
            } else {
                // Normal weight → add twice
                weighted.add(p)
                weighted.add(p)
            }
        }

        // If everyone except last starter is filtered out, pick normally
        if (weighted.isEmpty()) {
            val index = secureRandom.nextInt(pool.size)
            val chosen = pool.removeAt(index)
            PrefsManager.saveLastStarter(context, chosen.name)
            return chosen
        }

        val chosen = weighted[secureRandom.nextInt(weighted.size)]
        pool.remove(chosen)
        PrefsManager.saveLastStarter(context, chosen.name)
        return chosen
    }

    // First position (weighted - this is who starts the discussion)
    result[0] = pickWeighted(forFirstPosition = true)

    // Remaining positions (normal random)
    for (i in 1 until players.size) {
        result[i] = pickWeighted(forFirstPosition = false)
    }

    return result.filterNotNull()
}
