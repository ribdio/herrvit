# Mr. White Party Game Moderator App

## Executive Summary

"Herrvit" (Mr. White translated literally to Swedish) is a focused, ad-free, and frictionless Android application designed to moderate the popular social deduction game **Undercover/Mr. White**.

The core concept of the game is simple: **Civilians** share one secret word (**Word A**), the **Undercover** knows a similar but different word (**Word B**), and **Mr. White** has **no word** at all. All players, starting with the first player, must take turns saying a single word clue related to their secret word. The goal is to provide a clue that is specific enough to prove to the Civilans that you know the word, but vague enough so that the Undercover and Mr. White cannot guess it.

The app's unique selling proposition is acting as the **"Game Director"** or moderator. It handles all the complex logic, timing, role distribution, and elimination announcements, ensuring players can focus entirely on lying and deceiving one another.

-----

## Game Concept

The game of Undercover (also known as Mafia/Werewolf with a word-based twist) is a test of subtlety and deduction:

* **Civilian (Majority):** Knows **Word A**. Must identify the Undercover and Mr. White without giving away Word A to the impostors.
* **Undercover (Minority):** Knows a similar **Word B**. Must pretend to know Word A. They win if Mr. White is voted out and correctly guesses **Word B** instead of **Word A**.
* **Mr. White (Single Agent):** Knows **NO WORD**. Must convince everyone they are a Civilian (or Undercover) and survive. If eliminated, they get one chance to guess the Civilians' word to win.

The app intends to manage the entire flow: distributing the secret words/roles, running the discussion timer, handling the vote count, and announcing the final result and the player's true identity.

-----

## Core Features

This initial version focuses on the **Hot Potato Mode**—the core, 1-device, pass-and-play experience.

* **Open Source Word Engine:** The app does not rely on a built-in database. It fetches word pairs from a user-specified raw text file URL (e.g., a GitHub Gist or Pastebin).
    * **Format:** Simple text file where each line is a pair: `Word A, Word B`.
    * **Inversion Logic:** The app randomly assigns one word as the Civilian word and the other as the Undercover word from the pair, ensuring true secrecy.
* **Anti-Cheat "Touch-to-Hold" Reveal:** In Hot Potato mode, a player's role/word is only visible while they keep their finger on the screen. Removing the finger instantly masks the sensitive information.
* **State Keeping:** Can start a new game while keeping the players' names configured.
* **Adaptive Material You Theme:** Simplified for peak integration with the user's device.

-----

## Technical Architecture & Roadmap

### Data Structure (Word File)

The app parses a simple plain text file from a URL. Lines starting with `#` are ignored as comments.

**Example File Content:**

```
# Simple pairs, comma separated
Guitar, Violin
Harry Potter, Gandalf
Apple, Orange
Coffee, Tea
```

### TODO Roadmap

This list represents the immediate next steps to evolve the project beyond the core Hot Potato (1-device) MVP.

* **Connectivity**
    * [ ] Integrate the **Firebase SDK** for real-time state synchronization.
    * [ ] Implement the **"Host Party"** (Room Creation) and **"Join Party"** flows (via PIN/QR Code).
    * [ ] Sync player lists and game settings (e.g., Anonymous Voting toggle) across connected clients.
* **Polish & UX**
    * [ ] Implement **text-to-speech** to replace the need for a host/moderator reading from the screen.
    * [ ] Add **Haptic Feedback** for key events (e.g., successful vote, role reveal).
    * [ ] Implement **Screen Blanking** for non-active players in Connected Mode to save battery and maintain privacy.
