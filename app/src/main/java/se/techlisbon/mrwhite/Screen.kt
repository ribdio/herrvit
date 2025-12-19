package se.techlisbon.mrwhite

sealed class Screen {
    object Setup : Screen()
    data class Reveal(val players: List<Player>, val index: Int, val eliminated: List<String> = emptyList()) : Screen()
    data class StartAnnouncement(val players: List<Player>, val eliminated: List<String>) : Screen()
    data class Vote(val players: List<Player>, val eliminated: List<String>) : Screen()
    data class Result(val players: List<Player>, val eliminated: Player, val allEliminated: List<String>) : Screen()
    data class MrWhiteGuess(val players: List<Player>, val eliminated: List<String> = emptyList()) : Screen()
    data class GameOver(val players: List<Player>, val winner: String) : Screen()
}